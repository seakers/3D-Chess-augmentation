from flask import Flask, request, jsonify
import os
import logging
import json
from datetime import datetime, timedelta, timezone
import numpy as np

from tatc.schemas import Instrument, Satellite as TATC_Satellite, TwoLineElements, Point
from tatc.analysis import collect_multi_observations, aggregate_observations, reduce_observations
from tatc.utils import swath_width_to_field_of_regard

from eose.coverage import CoverageRecord, CoverageRequest, CoverageResponse
from eose.grids import UniformAngularGrid
from eose.orbits import GeneralPerturbationsOrbitState
from eose.satellites import Satellite
from eose.targets import TargetPoint

from shapely.geometry import box, mapping
from scipy.stats import hmean
import pandas as pd
from joblib import Parallel, delayed

# Configure logging
logging.basicConfig(level=logging.DEBUG, filename='tatc_server.log', filemode='w',
                    format='%(asctime)s - %(levelname)s - %(message)s')

app = Flask(__name__)

@app.route('/health', methods=['GET'])
def health_check():
    logging.debug('Health check endpoint called.')
    return jsonify({'status': 'running'}), 200

@app.route('/evaluate', methods=['POST'])
def evaluate():
    logging.debug('Received request at /evaluate endpoint.')
    data = request.get_json()

    # Log the received data for debugging purposes
    logging.debug(f'Request data: {data}')
    
    if not data:
        logging.error('Invalid input data: No JSON body received.')
        return jsonify({'error': 'Invalid input data'}), 400

    # Extract architecture data and folder path
    architecture = data.get('architecture')
    folder_path = data.get('folderPath')

    logging.debug(f'Extracted architecture: {architecture}')
    logging.debug(f'Extracted folder path: {folder_path}')

    if not architecture or not folder_path:
        logging.error('Missing architecture data or folder path.')
        return jsonify({'error': 'Missing architecture data or folder path'}), 400

    try:
        # Perform coverage evaluation
        logging.debug('Starting coverage evaluation...')
        coverage_metrics = evaluate_coverage(architecture, folder_path)
        logging.debug(f'Coverage evaluation completed. Result: {coverage_metrics}')

        # Return the result
        return jsonify(coverage_metrics), 200

    except Exception as e:
        logging.error(f'Error during evaluation: {str(e)}')
        return jsonify({'error': 'Evaluation failed', 'details': str(e)}), 500

def evaluate_coverage(architecture_json, folder_path):
    # Parse the architecture JSON to extract satellites and targets
    satellites = parse_architecture(architecture_json)
    logging.debug(f'Parsed satellites: {satellites}')

    # Define the analysis parameters
    start = datetime.now(timezone.utc)
    duration = timedelta(days=1)  # Analyze coverage over 1 day
    logging.debug(f'Analysis start time: {start}, duration: {duration}')

    # Define target points (e.g., global grid)
    sample_points = UniformAngularGrid(
        delta_latitude=5, delta_longitude=5, region=mapping(box(-180, -90, 180, 90))
    ).as_targets()
    logging.debug(f'Generated {len(sample_points)} target points for coverage analysis.')

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
    coverage_fraction = response.coverage_fraction * 100  # Convert to percentage

    logging.debug(f'Harmonic Mean Revisit Time: {harmonic_mean_revisit} hours')
    logging.debug(f'Coverage Fraction: {coverage_fraction}%')

    # Return the coverage metrics
    return {
        'harmonicMeanRevisitTime': harmonic_mean_revisit,
        'MeanResponseTime': harmonic_mean_revisit,
        'coverageFraction': coverage_fraction
    }

def parse_architecture(architecture_json):
    satellites = []
    # Extract satellites from the architecture
    for constellation in architecture_json.get('spaceSegment', []):
        for sat in constellation.get('satellites', []):
            # Extract orbital parameters
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
                    728000  # Setting microseconds as an example
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

            sat_obj = Satellite(
                orbit=GeneralPerturbationsOrbitState.from_omm(omm),
                field_of_view=field_of_regard
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
                    field_of_regard=satellite.field_of_view
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

    records = list(
        observations.apply(
            lambda r: CoverageRecord(
                target=request.targets[points.index(next(p for p in points if p.id == r["point_id"]))],
                mean_revisit=None if pd.isnull(r["revisit"]) else timedelta(seconds=r["revisit"].total_seconds()),
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

    return CoverageResponse(
        records=records,
        harmonic_mean_revisit=None
            if observations.dropna(subset="revisit").empty
            else timedelta(seconds=hmean(observations.dropna(subset="revisit")["revisit"].dt.total_seconds())),
        coverage_fraction=len(observations.index) / len(points)
    )

if __name__ == '__main__':
    logging.debug('Starting TAT-C server...')
    print("TAT-C Server running...")
    app.run(host='localhost', port=5001)  # Running on port 5001 to avoid conflicts
