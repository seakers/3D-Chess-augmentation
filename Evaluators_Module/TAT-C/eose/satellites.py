from pydantic import BaseModel, Field
from typing import List, Union, Optional

from .orbits import GeneralPerturbationsOrbitState
from .instruments import BasicSensor, PassiveOpticalScanner, SinglePolStripMapSAR
from .utils import Identifier, Quaternion, FixedOrientation

from eose.utils import Quaternion


class Payload(BaseModel):
    id: Identifier = Field(..., description="Payload identifier.")
    field_of_view: float = Field(
        gt=0, le=180, description="Angular payload field of view."
    )


class SatelliteBus(BaseModel):
    id: Identifier = Field(..., description="Spacecraft bus identifier.")
    mass: Optional[float] = Field(
        None, gt=0, description="Mass of the sensor in kilograms."
    )
    volume: Optional[float] = Field(
        None, gt=0, description="Volume of the sensor in cubic centimeter."
    )
    orientation: Optional[Union[FixedOrientation, Quaternion]] = Field(
        FixedOrientation.NADIR_GEOCENTRIC,
        description="Orientation of the spacecraft body-fixed frame, relative to requested frame.",
    )


class Satellite(BaseModel):
    id: Identifier = Field(..., description="Satellite identifier.")
    orbit: GeneralPerturbationsOrbitState = Field(
        ..., description="Initial orbit state."
    )
    payloads: List[
        Union[Payload, BasicSensor, PassiveOpticalScanner, SinglePolStripMapSAR]
    ] = Field([], description="Satellite payloads.")
    satellite_bus: Optional[SatelliteBus] = Field(
        None, description="Satellite bus specification."
    )