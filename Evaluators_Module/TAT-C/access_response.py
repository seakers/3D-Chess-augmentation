import json
from datetime import datetime, timedelta, timezone
from joblib import Parallel, delayed
from tatc.schemas import (
    Instrument as TATC_Instrument,
    Satellite as TATC_Satellite,
    TwoLineElements,
    Point
)
from tatc.analysis import (
    collect_multi_observations,
)

from eose.access import (
    AccessSample,
    AccessRecord,
    AccessRequest,
    AccessResponse,
)
from eose.grids import UniformAngularGrid
from eose.propagation import (
    PropagationSample,
    PropagationRecord,
    PropagationRequest,
    PropagationResponse,
)
from shapely.geometry import box, mapping
from eose.orbits import GeneralPerturbationsOrbitState, Propagator
from eose.satellites import Satellite, Payload
from eose.utils import CartesianReferenceFrame
from tat_c_manager import parse_architecture
import geopandas as gpd
import pandas as pd
def access_tatc(request: AccessRequest) -> AccessResponse:

    def get_field_of_regard(payload):
        if hasattr(payload, 'scene_field_of_view'):
            if payload.scene_field_of_view is not None:
                field_of_regard = payload.scene_field_of_view
            else:
                field_of_regard = payload.field_of_view
        else:
            field_of_regard = payload.field_of_view
        if field_of_regard.type == "CircularGeometry":
            return(field_of_regard.diameter)
        elif field_of_regard.type == "RectangularGeometry":
            return(field_of_regard.angle_height)
        else:
            raise RuntimeError('Unknown geometry type: {field_of_regard.type}')
        
    if request.propagator != Propagator.SGP4:
        raise RuntimeError("TAT-C only supports SGP4 propagator.")
    satellites = [
        TATC_Satellite(
            name=satellite.id,
            orbit=TwoLineElements(tle=satellite.orbit.to_tle()),
            instruments=[
                TATC_Instrument(
                    name=str(payload.id), field_of_regard= get_field_of_regard(payload)
                ) 
            ],
        )
        for satellite in request.satellites
        for payload in satellite.payloads
        if payload.id in request.payload_ids
    ]
    observations = Parallel(-1)(
        delayed(collect_multi_observations)(
            Point(
                id=i,
                longitude=target.position[0],
                latitude=target.position[1],
                altitude=(target.position[2] if len(target.position) > 2 else 0),
            ),
            satellites,
            request.start,
            request.start + request.duration,
        )
        for i, target in enumerate(request.targets)
    )
    return AccessResponse(
        **request.model_dump(exclude="target_records"),
        target_records=[
            (
                AccessRecord(target_id=target.id)
                if observations[i].empty
                else AccessRecord(
                    target_id=target.id,
                    samples=observations[i].apply(
                        lambda s: AccessSample(
                            start=s.start,
                            duration=s.end - s.start,
                            satellite_id=s.satellite,
                            instrument_id=s.instrument,
                        ),
                        axis=1,
                    ),
                )
            )
            for i, target in enumerate(request.targets)
        ],
    )
def perform_access_response(architecture):
    # Initialize an empty list to hold Satellite objects
    satellites = parse_architecture(architecture)
    # Create the PropagationRequest
    targets = UniformAngularGrid(
    delta_latitude=20, delta_longitude=20, region=mapping(box(-180, -50, 180, 50))
).as_targets()

    request = AccessRequest(
        satellites=satellites,
        targets=targets,
        start=datetime(2024, 1, 1, tzinfo=timezone.utc),
        duration=timedelta(hours=1),
        propagator=Propagator.SGP4,
        payload_ids=["FireSat-Sensor"],
    )
    access_response = access_tatc(request)
    return access_response.model_dump_json(exclude=["start", "duration", "propagator"]),