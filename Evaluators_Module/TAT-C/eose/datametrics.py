from typing import List, Literal, Union
from pydantic import AwareDatetime, BaseModel, Field

from .access import AccessResponse, AccessRecord, AccessSample
from .propagation import PropagationResponse


class DataMetricsRequest(AccessResponse, PropagationResponse):
    """The data-metrics are calculated for all the target points included in the coverage response, within the requested time period.
    Data metrics are calculated for all the ``AccessRecords`` available in the ``AccessResponse``.
    Information about the satellite specs is obtained from ``ObservationResponse`` or ``PropagationResponse``.
    The satellite states info is available with the ``PropagationResponse`` and the access info is available with the ``ObservationResponse``.
    """

    pass


class BasicSensorDataMetricsInstantaneous(BaseModel):
    """Basic sensor data metrics results calculated at an instant."""

    type: Literal["BasicSensor"] = Field("BasicSensor")
    time: AwareDatetime = Field(
        ..., description="Time instant at which the data metrics are recorded."
    )
    incidence_angle: float = Field(
        ...,
        description="Incidence angle in degrees at the target point calculated assuming spherical Earth.",
    )
    look_angle: float = Field(
        ...,
        description=" Look angle in degrees at the target point calculated assuming spherical Earth. Positive sign => look is in positive half-space made by the orbit-plane (i.e. orbit plane normal vector) and vice-versa.",
    )
    observation_range: float = Field(
        ...,
        description="Distance in kilometers from satellite to ground-point during the observation.",
    )
    solar_zenith: float = Field(
        ..., description="Solar Zenith angle in degrees during observation."
    )


class PassiveOpticalScannerDataMetricsInstantaneous(BaseModel):
    """Passive optical scanner data metrics results calculated at an instant."""

    type: Literal["PassiveOpticalScanner"] = Field("PassiveOpticalScanner")
    time: AwareDatetime = Field(
        ..., description="Time instant at which the data metrics are recorded."
    )
    noise_equivalent_delta_T: float = Field(
        ...,
        description="Noise Equivalent delta temperature in Kelvin. Characterizes the instrument in its ability to resolve temperature variations for a given background temperature.",
    )
    dynamic_range: float = Field(
        ...,
        description="Dynamic Range. Is the quotient of the signal and read-out noise electrons the sensor sees between dark and bright scenes.",
    )
    signal_to_noise_ratio: float = Field(
        ...,
        description="Signal-to-noise Ratio.",
    )
    along_track_resolution: float = Field(
        ..., description="Spatial resolution in meters of a hypothetical ground-pixel centered about observation point in the along-track direction."
    )
    cross_track_resolution: float = Field(
        ..., description="Spatial resolution in meters of a hypothetical ground-pixel centered about observation point in the cross-track direction."
    )


class SinglePolStripMapSARInstantaneous(BaseModel):
    """TBD"""

    type: Literal["SinglePolStripMapSAR"] = Field("SinglePolStripMapSAR")


class DataMetricsSample(AccessSample):
    """Aggregation of data metrics per overpass (for a target) for a satellite, instrument pair specified in the ``AccessSample``."""

    instantaneous_metrics: List[
        Union[
            BasicSensorDataMetricsInstantaneous,
            PassiveOpticalScannerDataMetricsInstantaneous,
            SinglePolStripMapSARInstantaneous,
        ]
    ] = Field(
        [],
        description="List of instantaneous data metrics calculated during a single overpass.",
    )


class DataMetricsRecord(AccessRecord):
    """Aggregation of data metrics per target (across multiple overpasses)."""

    samples: List[DataMetricsSample] = Field(
        [], description="List of data metric samples."
    )


class DataMetricsResponse(DataMetricsRequest):
    """Aggregation of data metrics over all targets (and all overpasses)."""

    target_records: List[DataMetricsRecord] = Field(
        [], description="List of data metrics records."
    )
