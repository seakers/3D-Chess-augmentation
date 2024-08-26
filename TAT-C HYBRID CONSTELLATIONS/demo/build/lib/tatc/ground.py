#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Object models and methods for managing ground system elements including
    ground stations.
"""

import json
import itertools

from .util import Entity, CommunicationBand, QuantitativeValue, QuantitativeRange, load_resource_from_json
from .agency import Agency

class Region(Entity):
    """A region or point designated by bounding latitudes and longitudes.

    :param latitude: Latitude (decimal degrees) with respect to the WGS 84 \
    geodetic model. Ranges between -90° (south) and 90° (north) where 0°  \
    represents the equator.
    :type latitude: float or list[float] or QuantitativeValue or QuantitativeRange
    :param longitude: Longitude (decimal degrees) with respect to the WGS 84 \
    geodetic model. Ranges between -180° (west) and 180° (east) where 0° \
    represents the prime meridian.
    :type longitude: float or list[float] or QuantitativeValue or QuantitativeRange
    :param targetWeight: Weight or importance of this region, ranging between 0 to inf.
    :type targetWeight: float
    """

    def __init__(self, latitude=None, longitude=None, targetWeight=None, _id=None):
        self.latitude = latitude
        self.longitude = longitude
        self.targetWeight = targetWeight
        super(Region,self).__init__(_id, "Region")

    @staticmethod
    def from_dict(d):
        """Parses a region from a normalized JSON dictionary.

        :returns: the region
        :rtype: Region
        """
        return Region(
                latitude = QuantitativeValue.from_json(d.get("latitude", None)),
                longitude = QuantitativeValue.from_json(d.get("longitude", None)),
                targetWeight = QuantitativeValue.from_json(d.get("targetWeight", None)),
                _id = d.get("@id", None)
            )

#: the constant denoting a global region
GLOBAL_REGION = Region(
    latitude=QuantitativeValue(-90,90),
    longitude=QuantitativeValue(-180,180)
)

class GroundStation(Entity):
    """A surface facility providing uplink or downlink communication services
    to satellites.

    :param name: Full name of this entity.
    :type name: str
    :param acronym: Acronym, initialism, or abbreviation.
    :type acronym: str
    :param agency: Designer, provider, or operator of this entity.
    :type agency: Agency
    :param latitude: Latitude (decimal degrees) with respect to the WGS 84 \
    geodetic model. Ranges between -90° (south) and 90° (north) where 0° \
    represents the equator.
    :type latitude: float
    :param longitude: Longitude (decimal degrees) with respect to the WGS 84 \
    geodetic model. Ranges between -180° (west) and 180° (east) where 0° \
    represents the prime meridian.
    :type longitude: float
    :param elevation: Elevation (m) above mean sea level with respect to the \
    WGS 84 geodetic model.
    :type elevation: float
    :param  commBand: List of communication bands avaialble for broadcast. \
    Recognized values include: VHF, UHF, L, S, C, X, Ku, Ka.
    :type commBand: list[CommunicationBand]
    """

    def __init__(self, name=None, acronym=None, agency=None, latitude=None,
                 longitude=None, elevation=None, commBand=None, _id=None):
        self.name = name
        self.acronym = acronym if acronym else name
        self.agency = agency
        self.latitude = latitude
        self.longitude = longitude
        self.elevation = elevation
        self.commBand = commBand
        super(GroundStation,self).__init__(_id, "GroundStation")

    @staticmethod
    def from_dict(d):
        """Parses a ground station from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the ground station
        :rtype: GroundStation
        """
        return GroundStation(
                name = d.get("name", None),
                acronym = d.get("acronym", None),
                agency = Agency.from_json(d.get("agency", None)),
                latitude = QuantitativeValue.from_json(d.get("latitude", None)),
                longitude = QuantitativeValue.from_json(d.get("longitude", None)),
                elevation = d.get("elevation", None),
                commBand = CommunicationBand.get(d.get("commBand", None)),
                _id = d.get("@id", None)
            )

class GroundNetwork(Entity):
    """A network of ground stations providing communication services.

    :param name: Full name of this entity.
    :type name: str
    :param acronym: Acronym, initialism, or abbreviation.
    :type acronym: str
    :param agency: Designer, provider, or operator of this entity.
    :type agency: Agency
    :param numberStations: Number of ground stations participating in the network.
    :type numberStations: int or list[int] or QuantitativeRange
    :param groundStations: List of member ground stations in this network (default: None).
    :type groundStations: list[GroundStation]
    """

    def __init__(self, name=None, acronym=None, agency=None, numberStations=None,
                 groundStations=None, _id=None):
        """Initialize a ground network object.
        """
        self.name = name
        self.acronym = acronym if acronym else name
        self.agency = agency
        self.numberStations = numberStations
        self.groundStations = groundStations
        super(GroundNetwork,self).__init__(_id, "GroundNetwork")

    def generate_networks(self, groundStations):
        """Generates networks for a given set of ground stations.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the list of possible ground networks composed from a set of \
        ground stations.
        :rtype: list[GroundNetwork]
        """
        networks = []
        # iterate over all networks
        for network in self:
            if network.groundStations is not None:
                # if network has pre-defined ground stations, automatically include
                networks.append(network)
            else:
                # valid stations must have compatible agency types
                validStations = [station for station in groundStations
                        if network.agency is None or
                        (station.agency is not None and station.agency.agencyType == network.agency.agencyType)]
                # iterate over all possible combinations of valid stations
                for selectedStations in itertools.combinations(validStations, network.numberStations):
                    # append a new network
                    networks.append(
                        GroundNetwork(
                            name=network.name,
                            acronym=network.acronym,
                            agency=network.agency,
                            numberStations=network.numberStations,
                            groundStations=list(selectedStations)
                        )
                    )
        return networks

    def __iter__(self):
        # if ground stations specified, return self
        if self.groundStations is not None:
            return iter([self])

        # iterate number stations
        try:
            numberStationsIter = iter(self.numberStations)
        except TypeError:
            numberStationsIter = [self.numberStations]
        # return the Cartesian product of iterated values
        return iter([
                GroundNetwork(
                    name=self.name,
                    acronym=self.acronym,
                    agency=self.agency,
                    numberStations=numberStations
                )
                for numberStations,
                in itertools.product(numberStationsIter)
            ])

    @staticmethod
    def from_dict(d):
        """Parses a ground network from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the ground network
        :rtype: GroundNetwork
        """
        if d.get("name", None) == "NENgov": # FIXME should reference @id, not name
            return NENgov
        if d.get("name", None) == "NENcom": # FIXME should reference @id, not name
            return NENcom
        if d.get("name", None) == "NENall": # FIXME should reference @id, not name
            return NENall
        if d.get("name", None) == "DSN": # FIXME should reference @id, not name
            return DSN
        return GroundNetwork(
                name = d.get("name", None),
                acronym = d.get("acronym", None),
                agency = Agency.from_json(d.get("agency", None)),
                numberStations = QuantitativeRange.from_json(d.get("numberStations", None)),
                groundStations = GroundStation.from_json(d.get("groundStations", None)),
                _id = d.get("@id", None)
            )

#: the constant denoting the deep space ground network
DSN = load_resource_from_json(GroundNetwork, "DSN.json")

#: the constant denoting the near-Earth ground network (commerical segment)
NENcom = load_resource_from_json(GroundNetwork, "NENcom.json")

#: the constant denoting the near-Earth ground network (government segment)
NENgov = load_resource_from_json(GroundNetwork, "NENgov.json")

#: the constant denoting the near-Earth ground network (all segments)
NENall = GroundNetwork(
    name="Near Earth Network (All)",
    acronym="NENall",
    groundStations=NENcom.groundStations + NENgov.groundStations
)
