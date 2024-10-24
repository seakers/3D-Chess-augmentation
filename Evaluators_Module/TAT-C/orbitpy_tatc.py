import warnings

import tempfile
import os, shutil
import csv


import json
from datetime import datetime, timedelta, timezone
from astropy.time import Time as AstroPy_Time
from shapely.geometry import box, mapping
from scipy.stats import hmean

from scipy.spatial.transform import Rotation as Scipy_Rotation

import pandas as pd

from pydantic import AwareDatetime

from tatc.analysis import (
    aggregate_observations,
    reduce_observations,
)

from orbitpy.util import (
    OrbitState as OrbitPy_OrbitState,
    Spacecraft as OrbitPy_Spacecraft,
    SpacecraftBus as OrbitPy_SpacecraftBus,
)
from orbitpy.propagator import (
    J2AnalyticalPropagator as OrbitPy_J2AnalyticalPropagator,
    SGP4Propagator as OrbitPy_SGP4Propagator,
)
from orbitpy.coveragecalculator import (
    GridCoverage as OrbitPy_GridCoverage,
    find_access_intervals as OrbitPy_find_access_intervals,
)
from orbitpy.grid import Grid as OrbitPy_Grid

from eose.orbits import GeneralPerturbationsOrbitState, Propagator
from eose.satellites import Satellite, SatelliteBus
from eose.utils import (
    CartesianReferenceFrame,
    PlanetaryCoordinateReferenceSystem,
    Quaternion,
    FixedOrientation,
)
from eose.geometry import Position

from eose.access import (
    AccessSample,
    AccessRecord,
    AccessRequest,
    AccessResponse,
)
from eose.grids import UniformAngularGrid

from instrupy import Instrument as InstruPy_Instrument

from eose.instruments import CircularGeometry, RectangularGeometry, BasicSensor


from orbitpy.util import (
    OrbitState as OrbitPy_OrbitState,
    Spacecraft as OrbitPy_Spacecraft,
    SpacecraftBus as OrbitPy_SpacecraftBus,
)
from orbitpy.propagator import (
    J2AnalyticalPropagator as OrbitPy_J2AnalyticalPropagator,
    SGP4Propagator as OrbitPy_SGP4Propagator,
)
from orbitpy.coveragecalculator import (
    GridCoverage as OrbitPy_GridCoverage,
    find_access_intervals as OrbitPy_find_access_intervals,
)
from orbitpy.grid import Grid as OrbitPy_Grid

from eose.orbits import GeneralPerturbationsOrbitState, Propagator
from eose.satellites import Satellite, SatelliteBus
from eose.utils import (
    CartesianReferenceFrame,
    PlanetaryCoordinateReferenceSystem,
    Quaternion,
    FixedOrientation,
)
from eose.geometry import Position

from eose.access import (
    AccessSample,
    AccessRecord,
    AccessRequest,
    AccessResponse,
)
from eose.grids import UniformAngularGrid

from instrupy import Instrument as InstruPy_Instrument

from eose.instruments import CircularGeometry, RectangularGeometry, BasicSensor
# define the orbit and the instrument
iss_omm_str = '[{"OBJECT_NAME":"ISS (ZARYA)","OBJECT_ID":"1998-067A","EPOCH":"2024-06-07T09:53:34.728000","MEAN_MOTION":15.50975122,"ECCENTRICITY":0.0005669,"INCLINATION":51.6419,"RA_OF_ASC_NODE":3.7199,"ARG_OF_PERICENTER":284.672,"MEAN_ANOMALY":139.0837,"EPHEMERIS_TYPE":0,"CLASSIFICATION_TYPE":"U","NORAD_CAT_ID":25544,"ELEMENT_SET_NO":999,"REV_AT_EPOCH":45703,"BSTAR":0.00033759,"MEAN_MOTION_DOT":0.00019541,"MEAN_MOTION_DDOT":0}]'
iss_omm = json.loads(iss_omm_str)[0]

basic_sensor = BasicSensor(
    id="Atom",
    mass=100.5,
    volume=0.75,
    power=150.0,
    field_of_view=RectangularGeometry(
        angle_height=60.0, angle_width=30
    ),  # CircularGeometry(diameter=60.0)
    orientation=list([0, 0.258819, 0, 0.9659258]),  # +30 deg roll about x-axis (roll)
    data_rate=10.5,
    bits_per_pixel=16,
)

satellites = [
    Satellite(
        id="ISS",
        orbit=GeneralPerturbationsOrbitState.from_omm(iss_omm),
        payloads=[basic_sensor],
        satellite_bus=SatelliteBus(
            id="BlueCanyon XB16", orientation=FixedOrientation.NADIR_GEOCENTRIC
        ),
    )
]

targets = UniformAngularGrid(
    delta_latitude=20, delta_longitude=20, region=mapping(box(-180, -50, 180, 50))
).as_targets()

mission_start = datetime(2024, 1, 1, tzinfo=timezone.utc)
mission_duration = timedelta(days=7)
propagate_time_step = timedelta(minutes=0.1)


def access_orbitpy(request: AccessRequest) -> AccessResponse:

    # create a temporary directory to hold temporary files
    script_directory = os.path.dirname(os.path.abspath("__file__"))
    temp_dir = os.path.join(script_directory, "temp")
    os.makedirs(temp_dir, exist_ok=True)

    #### Enumerate and convert from EOSE-API satellites to OrbitPy satellite objects. ####
    # (Enumeration generates distinct orbit-instrument pairs for satellites equipped with multiple instruments.)
    OrbitPy_Satellites = []
    for satellite in request.satellites:
        for instru in satellite.payloads:
            if instru.id in request.payload_ids:
                instru_type = instru.type
                if instru_type == "BasicSensor":

                    if instru.field_of_view.type == "CircularGeometry":
                        instupy_fov_geom = {
                            "shape": "CIRCULAR",
                            "diameter": instru.field_of_view.diameter,
                        }
                    elif instru.field_of_view.type == "RectangularGeometry":
                        instupy_fov_geom = {
                            "shape": "RECTANGULAR",
                            "angleHeight": instru.field_of_view.angle_height,
                            "angleWidth": instru.field_of_view.angle_width,
                        }
                    else:
                        raise ValueError(
                            f"Only Circular and Rectangular geometries are supported and not {instru.field_of_view.type}"
                        )

                    # Convert orientation in Quaternion to Euler rotations
                    r = Scipy_Rotation.from_quat(list(instru.orientation))
                    (x, y, z) = r.as_euler(
                        "XYZ", degrees=True
                    )  # Conventions 'XYZ' are for intrinsic rotations (used by OrbitPy), while 'xyz' are for extrinsic rotations.

                    instrupy_sensor = InstruPy_Instrument.from_dict(
                        {
                            "@type": "Basic Sensor",
                            "orientation": {
                                "referenceFrame": "SC_BODY_FIXED",
                                "convention": "REF_FRAME_ALIGNED",
                            },
                            "fieldOfViewGeometry": instupy_fov_geom,
                            "orientation": {
                                "referenceFrame": "NADIR_POINTING",
                                "convention": "XYZ",
                                "xRotation": x,
                                "yRotation": y,
                                "zRotation": z,
                            },
                            "@id": instru.id,
                        }
                    )
                else:
                    raise ValueError(
                        f"{instru_type} instrument type is not supported. Only 'BasicSensor' instrument type is supported."
                    )

                tle = satellite.orbit.to_tle()

                orbit_state = OrbitPy_OrbitState.from_dict(
                    {
                        "tle": {
                            "tle_line0": "Unknown",
                            "tle_line1": tle[0],
                            "tle_line2": tle[1],
                        }
                    }
                )

                if (
                    hasattr(satellite, "satellite_bus") is False
                    or satellite.satellite_bus.orientation
                    == FixedOrientation.NADIR_GEOCENTRIC
                ):
                    orbitpy_sat_bus = OrbitPy_SpacecraftBus.from_dict(
                        {
                            "orientation": {
                                "referenceFrame": "Nadir_pointing",
                                "convention": "REF_FRAME_ALIGNED",
                            }
                        }
                    )
                else:
                    warnings.warn(
                        "OrbitPy only processes spacecraft-bus orientation aligned with the NADIR_GEOCENTRIC frame. To account for off-naidr instrument viewing, please specify the instrument orientation relative to the NADIR_GEOCENTRIC frame.",
                        UserWarning,
                    )

                sat = OrbitPy_Spacecraft(
                    _id=satellite.id,
                    orbitState=orbit_state,
                    spacecraftBus=orbitpy_sat_bus,
                    instrument=[instrupy_sensor],
                )

                OrbitPy_Satellites.append(sat)

    #### Format the Target points into OrbitPy Grid object ####
    lon = []
    lat = []
    target_id = []
    # iterate through the Target points
    for tp in request.targets:
        if tp.crs == PlanetaryCoordinateReferenceSystem.EPSG_4326 or tp.crs is None:
            lon.append(tp.position[0])
            lat.append(tp.position[1])
            target_id.append(tp.id)
        else:
            raise ValueError(
                f"{tp.crs} CRS is not supported by OrbitPy. Only 'EPSG_4326' CRS is supported."
            )

    row_to_target_id = (
        {}
    )  # Dictionary to map row numbers ('GP index' in OrbitPy) to target_id
    orbitpy_custom_grid = None
    with tempfile.NamedTemporaryFile(
        mode="w+t", delete=False, dir=temp_dir
    ) as grid_file:
        writer = csv.writer(grid_file)
        writer.writerow(["lat [deg]", "lon [deg]", "id"])

        for row_num, (lat_val, lon_val, target_id_val) in enumerate(
            zip(lat, lon, target_id)
        ):
            writer.writerow([lat_val, lon_val, target_id_val])
            row_to_target_id[row_num] = target_id_val

    orbitpy_custom_grid = OrbitPy_Grid.from_customgrid_dict(
        {"@type": "customGrid", "covGridFilePath": grid_file.name}
    )

    #### run propagation and coverage with OrbitPy ####
    step_size_s = request.time_step.total_seconds()
    if request.propagator != Propagator.J2:
        propagator = OrbitPy_J2AnalyticalPropagator.from_dict(
            {"@type": "J2 ANALYTICAL PROPAGATOR", "stepSize": step_size_s}
        )
    elif request.propagator != Propagator.SGP4:
        propagator = OrbitPy_SGP4Propagator.from_dict(
            {"@type": "SGP4 PROPAGATOR", "stepSize": step_size_s}
        )
    else:
        raise RuntimeError("OrbitPy only supports J2 and SGP4 propagators.")

    #### Convert request time to Julian Date UT1####
    utc_dt = request.start.astimezone(
        timezone.utc
    )  # Convert to UTC (if not already in UTC)
    astropy_utc_time = AstroPy_Time(
        utc_dt, scale="utc"
    )  # Convert to astropy Time object
    astropy_ut1_time = astropy_utc_time.ut1  # Convert to UT1 scale

    start_date_dict = {"@type": "JULIAN_DATE_UT1", "jd": astropy_ut1_time.jd}
    start_date = OrbitPy_OrbitState.date_from_dict(
        start_date_dict
    )  # assumed that the time scale is UT1.
    duration = request.duration.total_seconds() / 86400.0

    for orbitpy_sat in OrbitPy_Satellites:

        # run propagation with OrbitPy
        with tempfile.NamedTemporaryFile(
            mode="w+t", delete=False, dir=temp_dir
        ) as state_cart_file:  # store satellite states in a temporary file.
            propagator.execute(
                orbitpy_sat, start_date, state_cart_file.name, None, duration
            )

            # run access calculations with OrbitPy
            with tempfile.NamedTemporaryFile(
                mode="w+t", delete=False, dir=temp_dir
            ) as access_fl:
                cov_calc = OrbitPy_GridCoverage(
                    grid=orbitpy_custom_grid,
                    spacecraft=orbitpy_sat,
                    state_cart_file=state_cart_file.name,
                )
                instru_id = orbitpy_sat.get_instrument().get_id()
                cov_calc.execute(
                    instru_id=instru_id,
                    mode_id=None,
                    use_field_of_regard=True,
                    out_file_access=access_fl.name,
                    mid_access_only=False,
                )
                intervals_df = OrbitPy_find_access_intervals(access_fl.name)

                grouped_intervals = intervals_df.groupby("GP index")
                access_records = []  # record of accesses at each target point
                for gp_index, group in grouped_intervals:
                    # Iterate over each record in the group
                    access_sample = []
                    for index, row in group.iterrows():
                        access_start = request.start + timedelta(
                            seconds=row["Start time index"] * step_size_s
                        )
                        access_duration = timedelta(
                            seconds=row["Duration"] * step_size_s
                        )
                        # form access sample
                        access_sample.append(
                            AccessSample(
                                satellite_id=orbitpy_sat._id,
                                instrument_id=instru_id,
                                start=access_start,
                                duration=access_duration,
                            )
                        )
                    # Add access record
                    access_records.append(
                        AccessRecord(
                            target_id=row_to_target_id[gp_index], samples=access_sample
                        )
                    )

    # delete the temporary directory
    shutil.rmtree(temp_dir)

    return AccessResponse(
        **request.model_dump(exclude="target_records"), target_records=access_records
    )

request = AccessRequest(
    satellites=satellites,
    targets=targets,
    start=mission_start,
    duration=mission_duration,
    propagator=Propagator.SGP4,
    time_step=propagate_time_step,
    payload_ids=["Atom"],
)

# display(request.model_dump_json())

access_response = access_orbitpy(request)

# display(access_response.model_dump_json())

access_data = access_response.as_dataframe()

# display(access_data)