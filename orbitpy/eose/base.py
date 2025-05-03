from datetime import timedelta
from typing import List

from pydantic import AwareDatetime, BaseModel, Field

from .orbits import Propagator
from .satellites import Satellite


class BaseRequest(BaseModel):
    start: AwareDatetime = Field(..., description="Requested operation start time.")
    duration: timedelta = Field(..., ge=0, description="Requested operation duration.")
    satellites: List[Satellite] = Field(..., description="Member satellites.")
    time_step: timedelta = Field(
        timedelta(seconds=10), gt=0, description="Propagation time step duration."
    )
    propagator: Propagator = Field(..., description="Propagator for satellite motion.")
