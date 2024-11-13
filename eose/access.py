from typing import List, Optional
from datetime import timedelta

from geopandas import GeoDataFrame
from pandas import to_timedelta
from pydantic import AwareDatetime, BaseModel, Field

from .base import BaseRequest
from .geometry import Point, Feature, FeatureCollection
from .targets import TargetPoint
from .utils import Identifier
from .propagation import PropagationRecord


class AccessRequest(BaseRequest):
    targets: List[TargetPoint] = Field(..., description="Target points.")
    payload_ids: List[Identifier] = Field(
        ..., description="List of payload identifiers to consider for analysis."
    )
    propagation_records: Optional[List[PropagationRecord]] = Field(
        None,
        description="Optional propagation records input, which can be utilized in access calculations.",
    )


class AccessSample(BaseModel):
    satellite_id: str = Field(None, description="ID of satellite making access.")
    instrument_id: str = Field(None, description="ID of instrument making access.")
    start: AwareDatetime = Field(..., description="Access sample start time.")
    duration: timedelta = Field(..., ge=0, description="Access sample duration.")

    def as_feature(self, target: TargetPoint) -> Feature:
        """
        Convert this access sample to a GeoJSON `Feature`.
        """
        return Feature(
            type="Feature",
            geometry=target.as_geometry(),
            properties=dict({"target_id": target.id}, **self.model_dump()),
        )


class AccessRecord(BaseModel):
    target_id: Identifier = Field(..., description="Target point identifier.")
    samples: List[AccessSample] = Field([], description="List of access samples.")

    def as_feature(self, target: TargetPoint) -> Feature:
        """
        Convert this access record to a GeoJSON `Feature`.
        """
        return Feature(
            type="Feature",
            geometry=self.as_geometry(target),
            properties=self.model_dump(),
        )

    def as_geometry(self, target: TargetPoint) -> Point:
        """
        Convert this access record to a GeoJSON `Point` geometry.
        """
        return target.as_geometry()


class AccessResponse(AccessRequest):
    target_records: List[AccessRecord] = Field([], description="Access results")

    def as_features(self) -> FeatureCollection:
        """
        Converts this access response to a GeoJSON `FeatureCollection`.
        """
        return FeatureCollection(
            type="FeatureCollection",
            features=[
                sample.as_feature(
                    next(
                        target
                        for target in self.targets
                        if target.id == record.target_id
                    )
                )
                for record in self.target_records
                for sample in record.samples
            ],
        )

    def as_dataframe(self) -> GeoDataFrame:
        """
        Converts this access response to a `geopandas.GeoDataFrame`.
        """
        gdf = GeoDataFrame.from_features(self.as_features())
        gdf["duration"] = to_timedelta(gdf["duration"])  # helper for type coersion
        return gdf
