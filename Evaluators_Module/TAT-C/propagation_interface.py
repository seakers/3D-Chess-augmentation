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
    # Create the PropagationRequest
    request = PropagationRequest(
        satellites=satellites,
        start=datetime(2024, 1, 1, tzinfo=timezone.utc),
        duration=timedelta(hours=1),
        time_step=timedelta(minutes=1),
        frame=CartesianReferenceFrame.ICRF,
        propagator=Propagator.SGP4,
    )
    propagation_response = propagate_tatc(request)
    return propagation_response.model_dump_json(
        exclude=["start", "duration", "satellites", "time_step"]
    ),
