from datetime import timedelta
from typing import List, Union

from pandas import to_datetime
from pydantic import AwareDatetime, BaseModel, Field
from geopandas import GeoDataFrame
from skyfield.api import load, Distance, Velocity, wgs84
from skyfield.framelib import itrs
from skyfield.positionlib import ICRF

from .base import BaseRequest
from .geometry import Point, Feature, FeatureCollection
from .utils import Vector, CartesianReferenceFrame, Identifier


class PropagationRequest(BaseRequest):
    frame: Union[CartesianReferenceFrame, str] = Field(
        CartesianReferenceFrame.ICRF,
        description="Reference frame in which propagation results defined.",
    )


class PropagationSample(BaseModel):
    time: AwareDatetime = Field(..., description="Time")
    position: Vector = Field(
        ...,
        description="Position (m)",
    )
    velocity: Vector = Field(
        ...,
        description="Velocity (m/s)",
    )

    def as_feature(
        self, satellite_id: Identifier, frame: CartesianReferenceFrame
    ) -> Feature:
        """
        Convert this propagation record to a GeoJSON `Feature`.
        """
        return Feature(
            type="Feature",
            geometry=self.as_geometry(frame),
            properties=dict({"satellite_id": satellite_id}, **self.model_dump()),
        )

    def as_geometry(self, frame: CartesianReferenceFrame) -> Point:
        """
        Convert this propagation record to a GeoJSON `Point` geometry.
        """
        ts = load.timescale()
        if frame == CartesianReferenceFrame.ICRF:
            icrf_position = ICRF(
                Distance(m=self.position).au,
                Velocity(km_per_s=[i / 1000 for i in self.velocity]).au_per_d,
                ts.from_datetime(self.time),
                399,
            )
        elif frame == CartesianReferenceFrame.ITRS:
            icrf_position = ICRF.from_time_and_frame_vectors(
                ts.from_datetime(self.time),
                itrs,
                Distance(m=self.position),
                Velocity(km_per_s=[i / 1000 for i in self.velocity]),
            )
            icrf_position.center = 399

        return Point.from_skyfield(wgs84.geographic_position_of(icrf_position))


class PropagationRecord(BaseModel):
    satellite_id: Identifier = Field(..., description="Satellite identifier.")
    samples: List[PropagationSample] = Field(
        [], description="List of propagation samples."
    )


class PropagationResponse(PropagationRequest):
    satellite_records: List[PropagationRecord] = Field(
        [], description="Propagation results"
    )

    def as_features(self) -> FeatureCollection:
        """
        Converts this propagation response to a GeoJSON `FeatureCollection`.
        """
        return FeatureCollection(
            type="FeatureCollection",
            features=[
                sample.as_feature(record.satellite_id, self.frame)
                for record in self.satellite_records
                for sample in record.samples
            ],
        )

    def as_dataframe(self) -> GeoDataFrame:
        """
        Converts this propagation response to a `geopandas.GeoDataFrame`.
        """
        gdf = GeoDataFrame.from_features(self.as_features())
        gdf["time"] = to_datetime(gdf["time"])  # helper for type coersion
        return gdf