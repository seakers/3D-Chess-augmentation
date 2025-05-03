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
from eose.coverage import CoverageRecord, CoverageRequest, CoverageResponse
from eose.grids import UniformAngularGrid
from eose.instruments import BasicSensor, CircularGeometry, OpticalInstrumentScanTechnique, PassiveOpticalScanner, RectangularGeometry
from eose.orbits import GeneralPerturbationsOrbitState
from eose.satellites import Satellite
from eose.targets import TargetPoint
import pandas as pd
import numpy as np
from pydantic import AwareDatetime

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
from eose.propagation import (
    PropagationSample,
    PropagationRecord,
    PropagationRequest,
    PropagationResponse,
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
from instrupy.passive_optical_scanner_model import PassiveOpticalScannerModel

from eose.instruments import CircularGeometry, RectangularGeometry, BasicSensor, PassiveOpticalScanner
def propagate_orbitpy(request: PropagationRequest) -> PropagationResponse:

    # create a temporary directory to hold temporary files
    script_directory = os.path.dirname(os.path.abspath("__file__"))
    temp_dir = os.path.join(script_directory, "temp")
    os.makedirs(temp_dir, exist_ok=True)

    if request.frame != CartesianReferenceFrame.ICRF:
        raise ValueError(
            f"OrbitPy supports only CartesianReferenceFrame.ICRF and does not support {request.frame}"
        )

    #### Enumerate and convert from EOSE-API satellites to OrbitPy satellite objects. ####
    OrbitPy_Satellites = []
    for satellite in request.satellites:

        tle = satellite.orbit.to_tle()
        orbit_state = OrbitPy_OrbitState.from_dict(
            {"tle": {"tle_line0": "Unknown", "tle_line1": tle[0], "tle_line2": tle[1]}}
        )

        sat = OrbitPy_Spacecraft(
            _id=satellite.id,
            orbitState=orbit_state,  # satellite-bus and instrument attributes are ignored since they are not relevant to propagation
        )
        OrbitPy_Satellites.append(sat)

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

    satellite_records = []
    for orbitpy_sat in OrbitPy_Satellites:

        # run propagation with OrbitPy
        with tempfile.NamedTemporaryFile(
            mode="w+t", delete=False, dir=temp_dir
        ) as state_cart_file:  # store satellite states in a temporary file.
            propagator.execute(
                orbitpy_sat, start_date, state_cart_file.name, None, duration
            )

        # transform the OrbitPy propagation results to PropagationResponse object
        orbitpy_states_df = pd.read_csv(state_cart_file.name, skiprows=4)

        # iterate over the dataframe and form PropagationSamples
        propagation_samples = []
        for index, row in orbitpy_states_df.iterrows():
            propagation_samples.append(
                PropagationSample(
                    time=request.start
                    + timedelta(seconds=step_size_s * row["time index"]),
                    position=[
                        row["x [km]"] * 1e3,
                        row["y [km]"] * 1e3,
                        row["z [km]"] * 1e3,
                    ],
                    velocity=[
                        row["vx [km/s]"] * 1e3,
                        row["vy [km/s]"] * 1e3,
                        row["vz [km/s]"] * 1e3,
                    ],
                )
            )

        satellite_records.append(
            PropagationRecord(satellite_id=orbitpy_sat._id, samples=propagation_samples)
        )

    # delete the temporary directory
    shutil.rmtree(temp_dir)

    return PropagationResponse(
        **request.model_dump(exclude="satellite_records"),
        satellite_records=satellite_records,
    )
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
                elif instru_type == "PassiveOpticalScanner":
                    instrupy_fov_geom = None
                    if instru.field_of_view.type == "CircularGeometry":
                        instrupy_fov_geom = {
                            "shape": "CIRCULAR",
                            "diameter": instru.field_of_view.diameter,
                        }
                    elif instru.field_of_view.type == "RectangularGeometry":
                        instrupy_fov_geom = {
                            "shape": "RECTANGULAR",
                            "angleHeight": instru.field_of_view.angle_height,
                            "angleWidth": instru.field_of_view.angle_width,
                        }
                    else:
                        raise ValueError(
                            f"Only Circular and Rectangular geometries are supported and not {instru.field_of_view.type}"
                        )
                    
                    instrupy_scene_fov_geom = None
                    if instru.scene_field_of_view: 
                        if instru.scene_field_of_view.type == "CircularGeometry":
                            instrupy_scene_fov_geom = {
                                "shape": "CIRCULAR",
                                "diameter": instru.scene_field_of_view.diameter,
                            }
                        elif instru.scene_field_of_view.type == "RectangularGeometry":
                            instrupy_scene_fov_geom = {
                                "shape": "RECTANGULAR",
                                "angleHeight": instru.scene_field_of_view.angle_height,
                                "angleWidth": instru.scene_field_of_view.angle_width,
                            }
                        else:
                            raise ValueError(
                                f"Only Circular and Rectangular geometries are supported and not {instru.instrupy_scene_fov_geom.type}"
                            )

                    # Convert orientation in Quaternion to Euler rotations
                    r = Scipy_Rotation.from_quat(list(instru.orientation))
                    (x, y, z) = r.as_euler(
                        "XYZ", degrees=True
                    )  # Conventions 'XYZ' are for intrinsic rotations (used by OrbitPy), while 'xyz' are for extrinsic rotations.

                    instrupy_sensor = InstruPy_Instrument.from_dict(
                        {
                            "@type": "Passive Optical Scanner",
                            "mass": instru.mass, 
                            "volume": instru.volume, 
                            "power": instru.power,
                            "fieldOfViewGeometry": instrupy_fov_geom, 
                            "scenceFieldOfViewGeometry": instrupy_scene_fov_geom,
                            "scanTechnique": "PUSHBROOM",
                            "orientation": {
                                "referenceFrame": "NADIR_POINTING",
                                "convention": "XYZ",
                                "xRotation": x,
                                "yRotation": y,
                                "zRotation": z,
                            },
                            "maneuver": None,
                            "dataRate": instru.data_rate,
                            "numberDetectorRows": instru.number_detector_rows,
                            "numberDetectorCols": instru.number_detector_cols,
                            "detectorWidth": instru.detector_width,
                            "focalLength": instru.focal_length,
                            "operatingWavelength": instru.operating_wavelength,
                            "bandwidth": instru.bandwidth,
                            "quantumEff": instru.quantum_efficiency,
                            "targetBlackBodyTemp": instru.target_black_body_temp,
                            "bitsPerPixel": instru.bits_per_pixel,
                            "opticsSysEff": instru.optics_sys_eff,
                            "numOfReadOutE": instru.number_of_read_out_E,
                            "apertureDia": instru.aperture_dia,
                            "Fnum": instru.F_num,
                            "maxDetectorExposureTime": instru.max_detector_exposure_time,
                            "atmosLossModel": None,
                            "@id": instru.id,
                        }
                    )
                else:
                    raise ValueError(
                        f"{instru_type} instrument type is not supported. Only 'BasicSensor' and 'PassiveOpticalScanner' instrument types are supported."
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
def perform_access_response(architecture):
    # Initialize an empty list to hold Satellite objects
    satellites = parse_architecture(architecture)
    # Create the PropagationRequest
    targets = UniformAngularGrid(
    delta_latitude=20, delta_longitude=20, region=mapping(box(-180, -50, 180, 50))
    ).as_targets()
    payload_ids = [
        payload.id
        for sat in satellites
        for payload in sat.payloads
    ]
    request = AccessRequest(
        satellites=satellites,
        targets=targets,
        start=datetime(2024, 1, 1, tzinfo=timezone.utc),
        duration=timedelta(hours=1),
        propagator=Propagator.SGP4,
        payload_ids=payload_ids,
    )
    access_response = access_orbitpy(request)
    return access_response.model_dump_json(exclude=["start", "duration", "propagator"]),

def perform_orbit_propagation(architecture):
    # Initialize an empty list to hold Satellite objects
    satellites = parse_architecture(architecture)
    # Create the PropagationRequest
    request = PropagationRequest(
        satellites=satellites,
        start=datetime(2024, 1, 1, tzinfo=timezone.utc),
        duration=timedelta(hours=1),
        time_step=timedelta(minutes=1),
        frame=CartesianReferenceFrame.ICRF,
        propagator=Propagator.SGP4,
    )
    propagation_response = propagate_orbitpy(request)
    return propagation_response.model_dump_json(
        exclude=["start", "duration", "satellites", "time_step"]
    ),


def build_sensor(instrument):
    fov = instrument.get('fieldOfView', {})
    if instrument["type"] == "passiveOpticalScanner":
        scan_technique = None
        if instrument.get("scanTechnique") == "WHISKBROOM":
            scan_technique = OpticalInstrumentScanTechnique.WHISKBROOM
        elif instrument.get("scanTechnique") == "PUSHBROOM":
            scan_technique = OpticalInstrumentScanTechnique.PUSHBROOM

        sensor = PassiveOpticalScanner(
                id=instrument.get("id"),
                mass= instrument.get("mass"), 
                volume= instrument.get("volume"), 
                power= instrument.get("power"), 
                field_of_view= RectangularGeometry(angle_height= fov.get("alongTrackFieldOfView"), angle_width= fov.get("crossTrackFieldOfView")),
                scene_field_of_view= RectangularGeometry(angle_height= fov.get("alongTrackFieldOfView"), angle_width= fov.get("crossTrackFieldOfView")),
                scanTechnique= scan_technique,
                data_rate= instrument.get("dataRate"),
                number_detector_rows= instrument.get("numberOfDetectorsRowsAlongTrack"),
                number_detector_cols= instrument.get("numberOfDetectorsColsCrossTrack"),
                detector_width= instrument.get("detectorWidth"),
                focal_length= instrument.get("focalLength"),
                operating_wavelength= instrument.get("operatingWavelength"),
                bandwidth= instrument.get("bandwidth"),
                quantum_efficiency= instrument.get("quantumEff"),
                target_black_body_temp= instrument.get("targetBlackBodyTemp"),
                bits_per_pixel= instrument.get("bitsPerPixel"),
                optics_sys_eff= instrument.get("opticsSysEff"),
                number_of_read_out_E= instrument.get("numOfReadOutE"),
                aperture_dia= instrument.get("apertureDia"),
                F_num= instrument.get("Fnum"),
                max_detector_exposure_time=instrument.get("maxDetectorExposureTime")
            )
    else:
        # Extract the side look angle in degrees
        side_look_angle_deg = instrument.get("orientation", {}).get("sideLookAngle", 0)
        side_look_angle_rad = np.radians(side_look_angle_deg)

        # Compute quaternion for a rotation about x-axis (roll)
        q_x = np.sin(side_look_angle_rad / 2)
        q_w = np.cos(side_look_angle_rad / 2)
        orientation_quaternion = [q_x, 0.0, 0.0, q_w]

        # Define sensor
        sensor = BasicSensor(
            id=instrument.get("id"),
            mass=instrument.get("mass"),
            volume=instrument.get("volume"),
            power=instrument.get("power"),
            field_of_view=RectangularGeometry(
                angle_height=fov.get("alongTrackFieldOfView"),
                angle_width=fov.get("crossTrackFieldOfView")
            ),
            orientation=orientation_quaternion,
            data_rate=instrument.get("dataRate"),
            bits_per_pixel=instrument.get("bitsPerPixel"),
        )

    return sensor

def parse_architecture(architecture_json):
    satellites = []
    # Extract satellites from the architecture
    for constellation in architecture_json.get('spaceSegment', []):
        for sat in constellation.get('satellites', []):
            # Extract orbital parameters
            sat_id = sat.get('@id', 'Unknown')
            orbit = sat.get('orbit', {})
            semimajor_axis = orbit.get('semimajorAxis')  # km
            eccentricity = orbit.get('eccentricity')
            inclination = orbit.get('inclination') % 180  # degrees
            raan = orbit.get('rightAscensionAscendingNode')
            arg_periapsis = orbit.get('periapsisArgument')
            true_anomaly = orbit.get('trueAnomaly')
            epoch = orbit.get('epoch')
            try:
                # Parse the epoch string from format "2019-08-01T00:00:00Z" and set microseconds to 0
                epoch_datetime = datetime.strptime(epoch, "%Y-%m-%dT%H:%M:%SZ")
                # Example output: datetime.datetime(2024, 6, 7, 9, 53, 34, 728000)
                formatted_epoch = datetime(
                    epoch_datetime.year,
                    epoch_datetime.month,
                    epoch_datetime.day,
                    epoch_datetime.hour,
                    epoch_datetime.minute,
                    epoch_datetime.second,
                    728000  
                )
            except ValueError as e:
                print(f"Error parsing epoch: {epoch} - {e}")
                formatted_epoch = datetime.now(timezone.utc).replace(microsecond=0)
            # Compute mean motion
            mu = 3.986e5  # Earth's gravitational parameter in km^3/s^2
            P = 2 * np.pi * np.sqrt((semimajor_axis**3) / mu)  # Orbital period in seconds
            mean_motion = (86400 / P)  # revs per day

            # Compute mean anomaly
            E = np.arctan2(
                np.sqrt(1 - eccentricity**2) * np.sin(np.deg2rad(true_anomaly)),
                eccentricity + np.cos(np.deg2rad(true_anomaly))
            )
            mean_anomaly = (np.rad2deg(E - eccentricity * np.sin(E))) % 360  # Normalize to [0, 360)

            # Extract field of view from the payload
            payload = sat.get('payload', [])
            payloads_list = []
            if payload:
                for instrument in payload:
                    sensor = build_sensor(instrument)
                    fov = instrument.get('fieldOfView', {})
                    # Use crossTrackFieldOfView or fullConeAngle as field of regard
                    field_of_regard = fov.get('fullConeAngle')
                    
                    if sensor:
                        payloads_list.append(sensor)
            else:
                field_of_regard = 0  # Default value if no payload

            # Create satellite object
            omm = {
                "OBJECT_NAME": sat.get('name', 'Satellite'),
                "OBJECT_ID": "1998-067A",
                "EPOCH": formatted_epoch, 
                "MEAN_MOTION": mean_motion,
                "ECCENTRICITY": eccentricity,
                "INCLINATION": inclination,
                "RA_OF_ASC_NODE": raan,
                "ARG_OF_PERICENTER": arg_periapsis,
                "MEAN_ANOMALY": mean_anomaly,
                "EPHEMERIS_TYPE": 0,
                "CLASSIFICATION_TYPE": 'U',
                "NORAD_CAT_ID": 0,
                "ELEMENT_SET_NO": 999,
                "REV_AT_EPOCH": 0,
                "BSTAR": 0,
                "MEAN_MOTION_DOT": 0,
                "MEAN_MOTION_DDOT": 0
            }
           
            sat_obj = Satellite(
                id=sat_id,
                orbit=GeneralPerturbationsOrbitState.from_omm(omm),
                field_of_view=field_of_regard,
                payloads=payloads_list,
                satellite_bus=SatelliteBus(
                    id="BlueCanyon XB16", orientation=FixedOrientation.NADIR_GEOCENTRIC
        ),
            )
            satellites.append(sat_obj)
            #logging.debug(f'Created satellite: {sat_obj}')

    return satellites