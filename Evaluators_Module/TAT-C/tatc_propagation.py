from joblib import Parallel, delayed

from tatc.schemas import (
    Instrument as TATC_Instrument,
    Satellite as TATC_Satellite,
    TwoLineElements,
)
from tatc.analysis import collect_orbit_track, OrbitCoordinate, OrbitOutput

from eose.propagation import (
    PropagationSample,
    PropagationRecord,
    PropagationRequest,
    PropagationResponse,
)
from eose.orbits import GeneralPerturbationsOrbitState, Propagator
from eose.satellites import Satellite, Payload
from eose.utils import CartesianReferenceFrame

import geopandas as gpd
import pandas as pd


def propagate_tatc(request: PropagationRequest) -> PropagationResponse:
    if request.propagator != Propagator.SGP4:
        raise RuntimeError("TAT-C only supports SGP4 propagator.")
    orbit_tracks = Parallel(-1)(
        delayed(collect_orbit_track)(
            TATC_Satellite(
                name=satellite.id,
                orbit=TwoLineElements(tle=satellite.orbit.to_tle()),
            ),
            TATC_Instrument(name="Instrument"),
            pd.date_range(
                request.start,
                request.start + request.duration,
                freq=request.time_step,
            ),
            coordinates=(
                OrbitCoordinate.ECI
                if request.frame == CartesianReferenceFrame.ICRF
                else OrbitCoordinate.ECEF
            ),
            orbit_output=OrbitOutput.POSITION_VELOCITY,
        )
        for satellite in request.satellites
    )
    return PropagationResponse(
        **request.model_dump(exclude="satellite_records"),
        satellite_records=[
            PropagationRecord(
                satellite_id=satellite.id,
                samples=orbit_tracks[i].apply(
                    lambda r: PropagationSample(
                        time=r.time,
                        frame=request.frame,
                        position=r.geometry.coords[0],
                        velocity=r.velocity.coords[0],
                    ),
                    axis=1,
                ),
            )
            for i, satellite in enumerate(request.satellites)
        ],
    )


import json
from datetime import datetime, timedelta, timezone

# note: celestrak request is rate-limited; using hard-coded version
# import requests
# response = requests.get("https://celestrak.org/NORAD/elements/gp.php?NAME=ZARYA&FORMAT=JSON").content
response = '[{"OBJECT_NAME":"ISS (ZARYA)","OBJECT_ID":"1998-067A","EPOCH":"2024-06-07T09:53:34.728000","MEAN_MOTION":15.50975122,"ECCENTRICITY":0.0005669,"INCLINATION":51.6419,"RA_OF_ASC_NODE":3.7199,"ARG_OF_PERICENTER":284.672,"MEAN_ANOMALY":139.0837,"EPHEMERIS_TYPE":0,"CLASSIFICATION_TYPE":"U","NORAD_CAT_ID":25544,"ELEMENT_SET_NO":999,"REV_AT_EPOCH":45703,"BSTAR":0.00033759,"MEAN_MOTION_DOT":0.00019541,"MEAN_MOTION_DDOT":0}]'
iss_omm = json.loads(response)[0]

request = PropagationRequest(
    satellites=[
        Satellite(
            id="ISS",
            orbit=GeneralPerturbationsOrbitState.from_omm(iss_omm),
            instruments=[Payload(id="Test", field_of_view=100)],
        )
    ],
    start=datetime(2024, 1, 1, tzinfo=timezone.utc),
    duration=timedelta(hours=1),
    time_step=timedelta(minutes=1),
    frame=CartesianReferenceFrame.ICRF,
    propagator=Propagator.SGP4,
)


response = propagate_tatc(request)


data = response.as_dataframe()

