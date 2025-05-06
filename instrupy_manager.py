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

import json

import json

def calculate_media_metrics(json_data):
    extrema = {
        "SNR_min": 0.0,
        "SNR_max": 3429538.805,
        "dynamic_range_min": 0.0,
        "dynamic_range_max": 3023402653.5,
        "along_track_resolution_min": 0.0,
        "along_track_resolution_max": 48712.125,
        "cross_track_resolution_min": 0.0,
        "cross_track_resolution_max": 50950.3,
        "noise_equivalent_delta_T_min": 0.0,
        "noise_equivalent_delta_T_max": 0.00014

    }

    data = json.loads(json_data) if isinstance(json_data, str) else json_data

    totals = {
        "SNR": 0.0,
        "dynamic_range": 0.0,
        "along_track_resolution": 0.0,
        "cross_track_resolution": 0.0,
        "noise_equivalent_delta_T": 0.0
    }
    count = 0

    def norm_maximize(val, min_val, max_val):
        return (val - min_val) / (max_val - min_val) if max_val > min_val else 0.0

    def norm_minimize(val, min_val, max_val):
        return (max_val - val) / (max_val - min_val) if max_val > min_val else 0.0

    for target in data.get("target_records", []):
        for sample in target.get("samples", []):
            for m in sample.get("instantaneous_metrics", []):
                snr = m.get("signal_to_noise_ratio", 0.0)
                dr = m.get("dynamic_range", 0.0)
                atr = m.get("along_track_resolution", 0.0)
                ctr = m.get("cross_track_resolution", 0.0)
                netd = m.get("noise_equivalent_delta_T", 0.0)

                totals["SNR"] += norm_maximize(snr, extrema["SNR_min"], extrema["SNR_max"])
                totals["dynamic_range"] += norm_maximize(dr, extrema["dynamic_range_min"], extrema["dynamic_range_max"])
                totals["along_track_resolution"] += norm_minimize(atr, extrema["along_track_resolution_min"], extrema["along_track_resolution_max"])
                totals["cross_track_resolution"] += norm_minimize(ctr, extrema["cross_track_resolution_min"], extrema["cross_track_resolution_max"])
                totals["noise_equivalent_delta_T"] += norm_minimize(netd, extrema["noise_equivalent_delta_T_min"], extrema["noise_equivalent_delta_T_max"])

                count += 1

    averages = {k: (v / count) if count else 0.0 for k, v in totals.items()}

    weights = {
        "SNR": 0.5,
        "dynamic_range": 0,
        "along_track_resolution": 0.2,
        "cross_track_resolution": 0.2,
        "noise_equivalent_delta_T": 0.1
    }

    score = sum(weights[k] * averages[k] for k in weights)
    averages["InstrumentScore"] = score

    return averages







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