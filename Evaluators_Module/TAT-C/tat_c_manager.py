import json
import logging
import datetime
from datetime import datetime, timedelta, timezone
import os
import numpy as np
import pandas as pd
from scipy.stats import hmean
from joblib import Parallel, delayed
from shapely.geometry import box, mapping

# Import your TAT-C specific modules
from tatc.schemas import Instrument, Satellite as TATC_Satellite, TwoLineElements, Point
from tatc.analysis import collect_multi_observations, aggregate_observations, reduce_observations
from tatc.utils import swath_width_to_field_of_regard

from eose.coverage import CoverageRecord, CoverageRequest, CoverageResponse
from eose.grids import UniformAngularGrid
from eose.instruments import BasicSensor, CircularGeometry, OpticalInstrumentScanTechnique, PassiveOpticalScanner, RectangularGeometry
from eose.orbits import GeneralPerturbationsOrbitState
from eose.satellites import Satellite
from eose.targets import TargetPoint

# Define the directory for logs
log_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "logs")

# Check if the log directory exists, if not, create it
if not os.path.exists(log_dir):
    os.makedirs(log_dir)

# Construct the log filename in the logs directory
log_filename = os.path.join(log_dir, f'debug_{datetime.now().strftime("%Y%m%d_%H%M%S")}.log')

# Create a logger
logger = logging.getLogger('TAT-C_Evaluator')
logger.setLevel(logging.DEBUG)

# Create file handler which logs even debug messages
fh = logging.FileHandler(log_filename)
fh.setLevel(logging.DEBUG)

# Create console handler to output to console (optional)
ch = logging.StreamHandler()
ch.setLevel(logging.DEBUG)

# Create formatter and add it to the handlers
formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
fh.setFormatter(formatter)
ch.setFormatter(formatter)

# Add the handlers to the logger
logger.addHandler(fh)
logger.addHandler(ch)



def evaluate_coverage(architecture_json):
    # Parse the architecture JSON to extract satellites and targets
    satellites = parse_architecture(architecture_json)
    logger.debug(f'Parsed satellites: {satellites}')

    start = datetime.now(timezone.utc)
    duration = timedelta(days=1)  # Analyze coverage over 1 day
    logger.debug(f'Analysis start time: {start}, duration: {duration}')

    sample_points = UniformAngularGrid(
        delta_latitude=5, delta_longitude=5, region=mapping(box(-180, -90, 180, 90))
    ).as_targets()
    logger.debug(f'Generated {len(sample_points)} target points for coverage analysis.')

    # Create the coverage request
    request = CoverageRequest(
        satellites=satellites,
        targets=sample_points,
        start=start,
        duration=duration,
    )

    # Perform the coverage analysis
    response = coverage_tatc(request)

    # Extract coverage metrics
    harmonic_mean_revisit = (
        None if response.harmonic_mean_revisit is None else
        response.harmonic_mean_revisit.total_seconds() / 3600  # Convert to hours
    )
    coverage_fraction = response.coverage_fraction

    logger.debug(f'Harmonic Mean Revisit Time: {harmonic_mean_revisit} hours')
    logger.debug(f'Coverage Fraction: {coverage_fraction}%')

    # Return the coverage metrics
    return {
        'HarmonicMeanRevisitTime': harmonic_mean_revisit,
        'CoverageFraction': coverage_fraction
    }

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
            if payload:
                instrument = payload[0]  # Assuming first instrument
                fov = instrument.get('fieldOfView', {})
                # Use crossTrackFieldOfView or fullConeAngle as field of regard
                field_of_regard = fov.get('crossTrackFieldOfView') or fov.get('fullConeAngle')
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
            basic_sensor = BasicSensor(
                id="Atom",
                mass=100.5,
                volume=0.75,
                power=150.0,
                field_of_view=CircularGeometry(diameter=60.0),
                data_rate=10.5,
                bits_per_pixel=16,
            )
            firesat = PassiveOpticalScanner(
                id="FireSat-Sensor",
                mass= 28, 
                volume= 0.12, 
                power= 32, 
                field_of_view= RectangularGeometry(angle_height= 0.628, angle_width= 115.8),
                scene_field_of_view= RectangularGeometry(angle_height= 30, angle_width= 115.8),
                scanTechnique= OpticalInstrumentScanTechnique.WHISKBROOM,
                data_rate= 85,
                number_detector_rows= 256,
                number_detector_cols= 1,
                detector_width= 30e-6,
                focal_length= 0.7,
                operating_wavelength= 4.2e-6,
                bandwidth= 1.9e-6,
                quantum_efficiency= 0.5,
                target_black_body_temp= 290,
                bits_per_pixel= 8,
                optics_sys_eff= 0.75,
                number_of_read_out_E= 25,
                aperture_dia= 0.26,
                F_num= 2.7
            )
            sat_obj = Satellite(
                id=sat_id,
                orbit=GeneralPerturbationsOrbitState.from_omm(omm),
                field_of_view=field_of_regard,
                payloads=[firesat]
            )
            satellites.append(sat_obj)
            logging.debug(f'Created satellite: {sat_obj}')

    return satellites

def coverage_tatc(request: CoverageRequest) -> CoverageResponse:
    unique_ids = len(set(target.id for target in request.targets)) == len(request.targets)
    points = [
        Point(
            id=target.id if unique_ids and isinstance(target.id, int) else i,
            longitude=target.position[0],
            latitude=target.position[1],
            altitude=target.position[2] if len(target.position) > 2 else 0
        )
        for i, target in enumerate(request.targets)
    ]
    satellites = [
        TATC_Satellite(
            name=satellite.orbit.object_name,
            orbit=TwoLineElements(tle=satellite.orbit.to_tle()),
            instruments=[
                Instrument(
                    name="Default",
                    field_of_regard=114
                )
            ]
        )
        for satellite in request.satellites
    ]

    observations = reduce_observations(
        aggregate_observations(
            pd.concat(
                Parallel(n_jobs=-1)(
                    delayed(collect_multi_observations)(
                        point,
                        satellites,
                        request.start,
                        request.start + request.duration
                    )
                    for point in points
                )
            )
        )
    )

    observations['revisit'] = observations['revisit'].dt.total_seconds()

    records = list(
        observations.apply(
            lambda r: CoverageRecord(
                target=request.targets[points.index(next(p for p in points if p.id == r["point_id"]))],
                mean_revisit=None if pd.isnull(r["revisit"]) else timedelta(seconds=r["revisit"]),
                number_samples=r["samples"]
            ),
            axis=1,
        )
    ) + [
        CoverageRecord(
            target=request.targets[i],
            mean_revisit=None,
            number_samples=0
        )
        for i, point in enumerate(points)
        if not any(observations["point_id"] == point.id)
    ]
    records.sort(key=lambda r: r.target.id)

    harmonic_mean_revisit = (
        None if observations['revisit'].dropna().empty
        else timedelta(seconds=hmean(observations['revisit'].dropna()))
    )

    return CoverageResponse(
        records=records,
        harmonic_mean_revisit=harmonic_mean_revisit,
        coverage_fraction=len(observations.index) / len(points)
    )