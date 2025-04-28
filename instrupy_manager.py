import math
from typing import List
from datetime import timezone
import json
from scipy.spatial.transform import Rotation as Scipy_Rotation
from astropy.time import Time as AstroPy_Time

from eose.datametrics import (
    DataMetricsRequest,
    BasicSensorDataMetricsInstantaneous,
    DataMetricsSample,
    DataMetricsRecord,
    DataMetricsResponse,
    PassiveOpticalScannerDataMetricsInstantaneous,
)
from eose.satellites import Satellite


from instrupy.basic_sensor_model import BasicSensorModel as InstruPy_BasicSensorModel
from instrupy.passive_optical_scanner_model import PassiveOpticalScannerModel

def calculate_media_metrics(json_data):
    # Parseamos el JSON si es un string, o usamos directamente el diccionario si ya está cargado
    if isinstance(json_data, str):
        data = json.loads(json_data)
    else:
        data = json_data
    
        # Initialize total metrics dictionary with keys for each performance metric
    metrics_totals = {
        "SNR": 0,  # Higher is better
        "dynamic_range": 0,  # Higher is better
        "along_track_resolution": 0,  # Lower is better
        "cross_track_resolution": 0,  # Lower is better
        "noise_equivalent_delta_T": 0  # Lower is better
    }
    count = 0  # Counter for the total number of metric samples

    # Iterate over target_records and accumulate each metric for all samples
    for target_record in data.get("target_records", []):
        for sample in target_record.get("samples", []):
            metrics_list = sample.get("instantaneous_metrics", {})
            for metrics in metrics_list:
                # Accumulate each metric if it exists in the data
                if metrics.get("signal_to_noise_ratio", 1) == 0:
                    metrics_totals["SNR"] += 1
                else:
                    metrics_totals["SNR"] += metrics.get("signal_to_noise_ratio", 1)

                metrics_totals["along_track_resolution"] += metrics.get("along_track_resolution", 0)
                metrics_totals["cross_track_resolution"] += metrics.get("cross_track_resolution", 0)
                metrics_totals["noise_equivalent_delta_T"] += metrics.get("noise_equivalent_delta_T", 0)
                count += 1

    # Calculate the average for each metric if there are samples available
    if count > 0:
        metrics_averages = {key: (value / count) for key, value in metrics_totals.items()}
    else:
        metrics_averages = {key: None for key in metrics_totals}  # Handle case with no samples

    # Define weights for each metric, assigning higher or lower influence based on mission priorities
    weights = {
        "SNR": 0.5,  # High weight, as SNR is critical for image clarity
        "dynamic_range": 0.1,  # Important for range of detectable values
        "along_track_resolution": 0.10,  # Lower is better, inversely related, so weight adjusted accordingly
        "cross_track_resolution": 0.20,  # Similar to above
        "noise_equivalent_delta_T": 0.1  # Lower is better, critical for thermal sensitivity
    }

    # Calculate weighted InstrumentScore based on whether higher or lower values are desirable
    if count > 0:
        instrument_score = (
            weights["SNR"] * 10*math.log10(metrics_averages["SNR"]if metrics_averages["SNR"] else 1) +
            weights["dynamic_range"] * 10*math.log10(metrics_averages["dynamic_range"]if metrics_averages["dynamic_range"] and metrics_averages["dynamic_range"]!=0 else 1)+
            weights["along_track_resolution"] * (1 / metrics_averages["along_track_resolution"] if metrics_averages["along_track_resolution"] else 0) +
            weights["cross_track_resolution"] * (1 / metrics_averages["cross_track_resolution"] if metrics_averages["cross_track_resolution"] else 0)+
            weights["noise_equivalent_delta_T"] * (1 / metrics_averages["noise_equivalent_delta_T"] if metrics_averages["noise_equivalent_delta_T"] else 0)
        )
    else:
        instrument_score = 0  # Handle division by zero if no samples

    # Add the final InstrumentScore to the averages dictionary
    metrics_averages["InstrumentScore"] = instrument_score
    if metrics_averages["cross_track_resolution"] is None:
        metrics_averages["cross_track_resolution"] = 0
    if metrics_averages["SNR"] is None:
        metrics_averages["SNR"] = 0
    if metrics_averages["along_track_resolution"] is None:
        metrics_averages["along_track_resolution"] = 0
    if metrics_averages["noise_equivalent_delta_T"] is None:
        metrics_averages["noise_equivalent_delta_T"] = 0
    if metrics_averages["dynamic_range"] is None:
        metrics_averages["dynamic_range"] = 0
    if metrics_averages["InstrumentScore"] is None:
        metrics_averages["InstrumentScore"] = 0
    return metrics_averages


def get_instantaneous_data_metrics_object(instru_type, time_instant, data_metrics):
    """
    Create an instantaneous data metrics object for the specified sensor type.

    :param instru_type: The type of the instrument (e.g., "BasicSensor").
    :paramtype instru_type: str
    :param time_instant: The time at which the data metrics are recorded.
    :paramtype time_instant: AwareDatetime
    :param data_metrics: A dictionary containing data metrics such as incidence angle, look angle, etc.
    :paramtype data_metrics: dict
    :return: An instance of `PassiveOpticalScannerDataMetricsInstantaneous` with the provided metrics.
    :rtype: PassiveOpticalScannerDataMetricsInstantaneous
    :raises ValueError: If the sensor type is not supported.
    """
    if instru_type == "PassiveOpticalScanner":
        return PassiveOpticalScannerDataMetricsInstantaneous(
            time=time_instant,
            noise_equivalent_delta_T=data_metrics["noise-equivalent delta T [K]"],
            dynamic_range=data_metrics["dynamic range"],
            signal_to_noise_ratio=data_metrics["SNR"],
            along_track_resolution=data_metrics["ground pixel along-track resolution [m]"],
            cross_track_resolution=data_metrics["ground pixel cross-track resolution [m]"],
        )
    else:
        raise ValueError(
            f"{instru_type} instrument type is not supported by this script. Only 'PassiveOpticalScanner' instrument type is supported."
        )


def data_metrics_instrupy(request):
    """
    Calculate data metrics using the provided request details and return the results.

    :param request: A `DataMetricsRequest` object containing target records, satellites, satellite records, and other metadata.
    :paramtype request: DataMetricsRequest
    :return: A `DataMetricsResponse` object containing the calculated data metrics for the requested targets and instruments.
    :rtype: DataMetricsResponse
    :raises ValueError: If the sensor type is not supported.
    """

    def get_propagation_record(propagation_records, satellite_id):
        """
        Find the PropagationRecord corresponding to the satellite-id.

        :param propagation_records: List of satellite propagation records.
        :paramtype propagation_records: list[PropagationRecord]
        :param satellite_id: The unique identifier of the satellite.
        :paramtype satellite_id: str
        :return: The matching PropagationRecord object.
        :rtype: PropagationRecord
        :raises RuntimeError: If the propagation record for the requested satellite-id is not found.
        """
        for record in propagation_records:
            if record.satellite_id == satellite_id:
                return record
        raise RuntimeError(
            "Propagation record for requested satellite-id was not found."
        )

    def get_instrument_model(satellites, satellite_id, instrument_id):
        """
        Find the instrument model corresponding to the instrument-id among the list of instruments in the satellite.

        :param satellites: List of satellite objects.
        :paramtype satellites: list[Satellite]
        :param satellite_id: The unique identifier of the satellite.
        :paramtype satellite_id: str
        :param instrument_id: The identifier of the instrument. The identifier of the instrument. The identifier needs to be unique within the list of instrument ids for a given satellite object.
        :paramtype instrument_id: str
        :return: The instrument type and corresponding sensor model.
        :rtype: tuple[str, InstruPy_PassiveOpticalScannerModel]
        :raises ValueError: If the instrument type is unsupported.
        :raises RuntimeError: If the instrument model is not found within the specified satellite.
        """
        for sat in satellites:
            if sat.id == satellite_id:
                for instru in sat.payloads:
                    if instru.id == instrument_id:
                        instru_type = instru.type
                        if instru_type == "PassiveOpticalScanner":
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

                            instrupy_sensor = PassiveOpticalScannerModel.from_dict(
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
                            return instru_type, instrupy_sensor                        
                        else:
                            raise ValueError(
                                f"{instru_type} instrument type is not supported by this script. Only 'PassiveOpticalScanner' instrument type is supported."
                            )
        raise RuntimeError(
            "Instrument model for the requested instrument-id (within the specified satellite_id) was not found."
        )

    def propagation_samples_within_time_range(
        propagation_samples, start_time, stop_time
    ):
        """
        Return propagation records within the specified time range.

        :param propagation_samples: List of propagation samples containing satellite states.
        :paramtype propagation_samples: list[PropagationSample]
        :param start_time: The start time of the range.
        :paramtype start_time: AwareDatetime
        :param stop_time: The stop time of the range.
        :paramtype stop_time: AwareDatetime
        :return: A list of filtered propagation records within the time range.
        :rtype: list[PropagationSample]
        """
        return [
            prop_record
            for prop_record in propagation_samples
            if start_time <= prop_record.time <= stop_time
        ]

    def get_target_position(target_id, all_targets):
        """
        Get the position of the target by its ID.

        :param target_id: The unique identifier of the target.
        :paramtype target_id: str
        :param all_targets: List of all targets.
        :paramtype all_targets: list[TargetPoint]
        :return: The position of the target as a tuple of (longitude, latitude).
        :rtype: Position
        :raises RuntimeError: If the target is not found.
        """
        for target in all_targets:
            if target.id == target_id:
                return target.position
        raise RuntimeError(f"Target with id {target_id} was not found.")

    # Main processing
    access_target_records = request.target_records
    dm_req_start = request.start
    dm_req_duration = request.duration

    dm_record = (
        []
    )  # Aggregation of data-metrics across multiple targets and multiple overpasses for each target.
    for access_record in access_target_records:
        target_position = get_target_position(access_record.target_id, request.targets)

        dm_sample = (
            []
        )  # Aggregation of data-metrics over multiple overpasses for the target
        for access_sample in access_record.samples:
            _sat_id = access_sample.satellite_id
            _instru_id = access_sample.instrument_id
            _start = max(dm_req_start, access_sample.start)
            _stop = min(
                dm_req_start + dm_req_duration,
                access_sample.start + access_sample.duration,
            )

            propagation_record = get_propagation_record(
                request.satellite_records, _sat_id
            )
            pr = propagation_samples_within_time_range(
                propagation_record.samples, _start, _stop
            )

            instru_type, instrupy_sensor = get_instrument_model(
                request.satellites, _sat_id, _instru_id
            )

            dm_inst = (
                []
            )  # Aggregation of data-metrics over a single overpass (=coverage sample) for the target
            if pr:
                for _pr in pr:
                    time_utc = AstroPy_Time(
                        _pr.time.astimezone(timezone.utc), scale="utc"
                    )
                    time_ut1 = time_utc.ut1
                    jd_ut1 = time_ut1.jd

                    SpacecraftOrbitState = {
                        "time [JDUT1]": jd_ut1,
                        "x [km]": _pr.position[0] * 1e-3,
                        "y [km]": _pr.position[1] * 1e-3,
                        "z [km]": _pr.position[2] * 1e-3,
                        "vx [km/s]": _pr.velocity[0] * 1e-3,
                        "vy [km/s]": _pr.velocity[1] * 1e-3,
                        "vz [km/s]": _pr.velocity[2] * 1e-3,
                    }
                    TargetCoords = {
                        "lat [deg]": target_position[1],
                        "lon [deg]": target_position[0],
                    }
                    data_metrics = instrupy_sensor.calc_data_metrics(
                        SpacecraftOrbitState, TargetCoords
                    )

                    _dm_inst = get_instantaneous_data_metrics_object(
                        instru_type, _pr.time, data_metrics
                    )
                    dm_inst.append(_dm_inst)

            _dm_sample = DataMetricsSample(
                **access_sample.model_dump(exclude="instantaneous_metrics"),
                instantaneous_metrics=dm_inst,
            )
            dm_sample.append(_dm_sample)

        _dm_record = DataMetricsRecord(
            **access_record.model_dump(exclude="samples"), samples=dm_sample
        )
        dm_record.append(_dm_record)

    data_metrics_response = DataMetricsResponse(
        **request.model_dump(exclude="target_records"), target_records=dm_record
    )
    average_metrics = calculate_media_metrics(data_metrics_response.model_dump_json())
    return average_metrics