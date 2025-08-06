import json
from datetime import datetime, timedelta, timezone
from tatc.schemas import (
    Instrument as TATC_Instrument,
    Satellite as TATC_Satellite,
    TwoLineElements,
)
from eose.propagation import (
    PropagationSample,
    PropagationRecord,
    PropagationRequest,
    PropagationResponse,
)
from eose.orbits import GeneralPerturbationsOrbitState, Propagator
from eose.satellites import Satellite, Payload
from eose.utils import CartesianReferenceFrame
from tat_c_manager import parse_architecture
import geopandas as gpd
import pandas as pd

from tatc_propagation import propagate_tatc
# Load the JSON data from a file
def perform_orbit_propagation(architecture):
    # Initialize an empty list to hold Satellite objects
    satellites = parse_architecture(architecture)
    date_str = architecture.get("mission").get("start")
    year = int(date_str[0:4])
    month = int(date_str[5:7])
    day = int(date_str[8:10])
    start=datetime(year, month, day, tzinfo=timezone.utc)
        # Extract number of days (assumes format always includes 'D')
    days_part = architecture.get("mission").get("duration").split('D')[0]  # 'P0Y0M01'
    days_str = days_part.split('M')[-1]     # '01'
    days = int(days_str)
    hours = days * 24
    duration=timedelta(hours=hours)
    # Create the PropagationRequest
    request = PropagationRequest(
        satellites=satellites,
        start=start,
        duration=duration,
        time_step=timedelta(minutes=1),
        frame=CartesianReferenceFrame.ICRF,
        propagator=Propagator.SGP4,
    )
    propagation_response = propagate_tatc(request)
    return propagation_response.model_dump_json(
        exclude=["start", "duration", "satellites", "time_step"]
    ),