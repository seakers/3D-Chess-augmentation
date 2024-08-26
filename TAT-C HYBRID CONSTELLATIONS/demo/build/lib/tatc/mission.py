#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Object models and methods for space missions.
"""

import json
import isodate
import datetime
from numbers import Number
import itertools
import copy

from .util import Entity, EnumEntity
from .agency import Agency
from .space import Satellite, Constellation, ConstellationType
from .ground import GroundStation, GroundNetwork, Region, GLOBAL_REGION
from .launch import LaunchVehicle
from .instrument import Instrument

class MissionConcept(Entity):
    """Top-level functional description of an Earth-observing mission.

    :param name: Full name of this entity.
    :type name: str
    :param acronym: Acronym, initialism, or abbreviation.
    :type acronym: str
    :param agency: Designer, provider, or operator of this entity.
    :type agency: Agency
    :param start: Mission start in ISO-8601 datetime format (default: today).
    :type start: str
    :param duration: Mission duration in ISO-8601 duration format (default: P90D, 90 days).
    :type duration: str
    :param altitudeDriftPercentThreshold: Acceptable drift in altitude as a \
    percentage of the nominal/target altitude for orbtial maintenance. For \
    example: a 0.02 threshold for a nominal 500 kilometer orbit indicates a \
    +/- 10 kilometer drift tolerance.
    :type altitudeDriftPercentThreshold: float
    :param argumentOfLatitudeDriftPercentThreshold: Acceptable drift in the \
    argument of latitude as a percentage of the nominal/target spread in \
    relative mean anomaly for orbital maintenance. For example: a 0.10 \
    threshold for a constellation with eight satellites equally-spaced in \
    mean anomaly (i.e. 45 degrees) indicates a +/- 4.5 degree drift tolerance.
    :type argumentOfLatitudeDriftPercentThreshold: float
    :param target: Target region of interest for mission objectives (default: GLOBAL_REGION).
    :type target: Region
    :param objectives: List of mission objectives.
    :type objectives: list[MissionObjective]
    :param constraints: List of mission constraints.
    :type constraints: list[MissionConstraint]
    """

    def __init__(self, name=None, acronym=None, agency=None,
                start=datetime.date.today().isoformat(), duration="P90D",
                altitudeDriftPercentThreshold=None,
                argumentOfLatitudeDriftPercentThreshold=None,
                target=GLOBAL_REGION, objectives=None, constraints=None, _id=None):
        self.name = name
        self.acronym = acronym if acronym else name
        self.agency = agency
        self.start = start
        if isinstance(duration, Number):
            self.duration = isodate.duration_isoformat(datetime.timedelta(days=duration))
        else: self.duration = duration
        self.altitudeDriftPercentThreshold = altitudeDriftPercentThreshold
        self.argumentOfLatitudeDriftPercentThreshold = argumentOfLatitudeDriftPercentThreshold
        self.target = target
        # convert objectives to list, if necessary
        if isinstance(objectives, str): self.objectives = [objectives]
        else: self.objectives = objectives
        # convert objectives to list, if necessary
        if isinstance(constraints, str): self.constraints = [constraints]
        else: self.constraints = constraints
        super(MissionConcept, self).__init__(_id, "MissionConcept")

    @staticmethod
    def from_dict(d):
        """Parses a mission concept from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the mission concept
        :rtype: MissionConcept
        """
        return MissionConcept(
                name = d.get("name", None),
                acronym = d.get("acronym", None),
                agency = Agency.from_json(d.get("agency", None)),
                start = d.get("start", datetime.date.today().isoformat()),
                duration = d.get("duration", "P90D"),
                altitudeDriftPercentThreshold = d.get("altitudeDriftPercentThreshold", None),
                argumentOfLatitudeDriftPercentThreshold = d.get("argumentOfLatitudeDriftPercentThreshold", None),
                target = Region.from_json(d.get("target", GLOBAL_REGION)),
                objectives = MissionObjective.from_json(d.get("objectives", None)),
                constraints = MissionConstraint.from_json(d.get("constraints", None)),
                _id = d.get("@id", None)
            )

class MissionObjective(Entity):
    """A variable to be maximized or minimized to acheive mission objectives.

    :param objectiveName: The full name of this objective.
    :type objectiveName: str
    :param objectiveParent: The full name of the parent objective. Requires \
    properties: objectiveWeight, objectiveMinValue, and objectiveMaxValue. \
    Example (MAX type): parent = sum_{i=1}^n weight_i*(value_i-minValue_i)/(maxValue_i-minValue_i). \
    :type objectiveParent: str
    :param objectiveWeight: Weight or importance of this metric, ranging between \
    0 to inf. Only required for objectives with objectiveParent. Example (MAX type): \
    parent = sum_{i=1}^n weight_i*(value_i-minValue_i)/(maxValue_i-minValue_i)
    :type objectiveWeight: float
    :param objectiveMinValue: Minimum value of this metric, ranging between 0 to inf. \
    Linearly maps to 0.0 for MAX, 1.0 for MIN, and 0.0 for TAR. \
    Only required for objectives with objectiveParent. Example (MAX type): \
    parent = sum_{i=1}^n weight_i*(value_i-minValue_i)/(maxValue_i-minValue_i)
    :type objectiveMinValue: float
    :param objectiveMaxValue: Maximum value of this metric, ranging between 0 to inf. \
    Linearly maps to 1.0 for MAX, 0.0 for MIN, and 0.0 for TAR. \
    Only required for objectives with objectiveParent. Example (MAX type): \
    parent = sum_{i=1}^n weight_i*(value_i-minValue_i)/(maxValue_i-minValue_i)
    :type objectiveMaxValue: float
    :param objectiveType: Type of metric. Recognized case-insensitive values include: \
    MAX (maximize), MIN (minimize), TAR (target).
    :type objectiveType: str or ObjectiveType
    :param objectiveTarget: Target value for the objective. Required for TAR type only.
    :type objectiveTarget: float
    """

    def __init__(self, objectiveName=None, objectiveParent=None, objectiveWeight=None,
            objectiveMinValue=None, objectiveMaxValue=None, objectiveType=None,
            objectiveTarget=None, _id=None):
        self.objectiveName = objectiveName
        self.objectiveParent = objectiveParent
        self.objectiveWeight = objectiveWeight
        self.objectiveMinValue = objectiveMinValue
        self.objectiveMaxValue = objectiveMaxValue
        self.objectiveType = ObjectiveType.get(objectiveType)
        self.objectiveTarget = objectiveTarget
        super(MissionObjective,self).__init__(_id, "MissionObjective")

    @staticmethod
    def from_dict(d):
        """Parses a mission objective from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the mission objective
        :rtype: MissionObjective
        """
        return MissionObjective(
                objectiveName = d.get("objectiveName", None),
                objectiveParent = d.get("objectiveParent", None),
                objectiveWeight = d.get("objectiveWeight", None),
                objectiveMinValue = d.get("objectiveMinValue", None),
                objectiveMaxValue = d.get("objectiveMaxValue", None),
                objectiveType = d.get("objectiveType", None),
                objectiveTarget = d.get("objectiveTarget", None),
                _id = d.get("@id", None)
        )

class ObjectiveType(EnumEntity):
    """Enumeration of recognized objective types.

    :var MAX: maximize objective.
    :vartype MAX: ObjectiveType
    :var MIN: minimize objective.
    :vartype MIN: ObjectiveType
    :var TAR: target objective.
    :vartype TAR: ObjectiveType
    """
    MAX = "MAX"
    MIN = "MIN"
    TAR = "TAR"

class MissionConstraint(Entity):
    """A constraint to be enforced to acheive mission objectives.

    :param constraintName: The full name of this constraint.
    :type constraintName: str
    :param isHard: Indicates a hard (true) or soft (false) constraint.
    :type isHard: bool
    :param constraintWeight: Weight or importance of this constraint, ranging \
    between 0 to inf.
    :type constraintWeight: float
    :param constraintMinValue: Minimum value of this constraint used for \
    normalization, ranging between 0 to inf. \
    :type constraintMinValue: float
    :param constraintMaxValue: Maximum value of this constraint used for \
    normalization, ranging between 0 to inf.
    :type constraintMaxValue: float
    :param constraintType: Type of constraint. Recognized case-insensitive \
    values include: GEQ (greater or equal), LEQ (less or equal), EQ (equality) \
    or NEQ (not equal).
    :type constraintType: str or ConstraintType
    :param constraintLevel: Level value of the constraint.
    :type constraintLevel: float
    """

    def __init__(self, constraintName=None, isHard=None, constraintWeight=None,
            constraintMinValue=None, constraintMaxValue=None,
            constraintType=None, constraintLevel=None, _id=None):
        self.constraintName = constraintName
        self.isHard = isHard
        self.constraintWeight = constraintWeight
        self.constraintMinValue = constraintMinValue
        self.constraintMaxValue = constraintMaxValue
        self.constraintType = ConstraintType.get(constraintType)
        self.constraintLevel = constraintLevel
        super(MissionConstraint,self).__init__(_id, "MissionConstraint")

    @staticmethod
    def from_dict(d):
        """Parses a mission constraint from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the mission constraint
        :rtype: MissionConstraint
        """
        return MissionConstraint(
                constraintName = d.get("constraintName", None),
                isHard = d.get("isHard", None),
                constraintWeight = d.get("constraintWeight", None),
                constraintMinValue = d.get("constraintMinValue", None),
                constraintMaxValue = d.get("constraintMaxValue", None),
                constraintType = d.get("constraintType", None),
                constraintLevel = d.get("constraintLevel", None),
                _id = d.get("@id", None)
        )

class ConstraintType(EnumEntity):
    """Enumeration of recognized constraint types.

    :var GEQ: greater or equal constraint.
    :vartype GEQ: ConstraintType
    :var LEQ: less or equal constraint.
    :vartype LEQ: ConstraintType
    :var EQ: equality constraint.
    :vartype EQ: ConstraintType
    :var NEQ: inequality constraint.
    :vartype NEQ: ConstraintType
    """
    GEQ = "GEQ"
    LEQ = "LEQ"
    EQ = "EQ"
    NEQ = "NEQ"

class DesignSpace(Entity):
    """Specification of fixed and variable quantities for a space mission.

    :param spaceSegment: List of potential constellations to consider.
    :type spaceSegment: list[Constellation]
    :param launchers: List of available launch vehicles to consider (overrides\
    default database).
    :type launchers: list[LaunchVehicle]
    :param satellites: List of available satellites.
    :type satellites: list[Satellite]
    :param groundSegment: List of potential ground networks to consider.
    :type groundSegment: list[GroundNetwork]
    :param groundStations: List of available ground stations.
    :type groundStations: list[GroundStation]
    """

    def __init__(self, spaceSegment=None, launchers=None, satellites=None,
                 groundSegment=None, groundStations=None, _id=None):
        # convert space segment to list, if necessary
        if isinstance(spaceSegment, Constellation): self.spaceSegment = [spaceSegment]
        else: self.spaceSegment = spaceSegment
        # convert launchers to list, if necessary
        if isinstance(launchers, LaunchVehicle): self.launchers = [launchers]
        else: self.launchers = launchers
        # convert satellites to list, if necessary
        if isinstance(satellites, Satellite): self.satellites  = [satellites]
        else: self.satellites = satellites
        # convert ground segment to list, if necessary
        if isinstance(groundSegment, GroundNetwork): self.groundSegment = [groundSegment]
        else: self.groundSegment = groundSegment
        # convert ground stations to list, if necessary
        if isinstance(groundStations, GroundStation): self.groundStations = [groundStations]
        else: self.groundStations = groundStations
        super(DesignSpace,self).__init__(_id, "DesignSpace")

    def generate_architectures(self, epoch=None):
        """Generates architectures in this design space.

        :param epoch: The default epoch value (ISO 8601 datetime format).
        :type epoch: str
        :return: an iterator of architectures generated in this design space
        :rtype: iter[Architecture]
        """
        # take the Cartesian product of all generated constellations and ground networks
        # constellations generated from the Cartesian product of the flattened list of all constellations
        # ground networks generated from the flattened list of all ground networks

        # generate all networks from the flattened list of ground networks
        groundNetworks = [copy.deepcopy(network_instance)
            for network_space in itertools.chain.from_iterable(self.groundSegment)
            for network_instance in network_space.generate_networks(self.groundStations)]

        # expand each constellation to the greatest extent
        constellation_sets = [[const_instance
                for const_space in constellation
                for const_instance in const_space.generate_constellations(self.satellites, epoch)]
            for constellation in self.spaceSegment if constellation.constellationType != ConstellationType.EXISTING]
        # take the Cartesian product of all generated constellations
        constellations_list = [[copy.deepcopy(c) for c in cs] for cs in itertools.product(*constellation_sets)]

        return iter([
                Architecture(
                    spaceSegment=constellations,
                    groundSegment=[groundNetwork],
                    _id='arch-{:d}'.format(i)
                )
                for i, (constellations, groundNetwork) in enumerate(itertools.product(
                    constellations_list, groundNetworks
                ))
            ])

    @staticmethod
    def from_dict(d):
        """Parses a design space from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the design space
        :rtype: DesignSpace
        """
        return DesignSpace(
                spaceSegment = Constellation.from_json(d.get("spaceSegment", None)),
                launchers = LaunchVehicle.from_json(d.get("launchers", None)),
                satellites = Satellite.from_json(d.get("satellites", None)),
                groundSegment = GroundNetwork.from_json(d.get("groundSegment", None)),
                groundStations = GroundStation.from_json(d.get("groundStations", None)),
                _id = d.get("@id", None)
            )

class Architecture(Entity):
    """Instantiation of a space mission including satellites and ground stations.

    :param spaceSegment: List of member constellations.
    :type spaceSegment: list[Constellation]
    :param groundSegment: List of member ground networks.
    :type groundSegment: list[GroundNetwork]
    """

    def __init__(self, spaceSegment=None, groundSegment=None, _id=None):
        con_id = 0
        sat_id = 0
        for con in spaceSegment if isinstance(spaceSegment, list) else [spaceSegment]:
            if con._id is None:
                con._id = 'con-{}'.format(con_id)
                con_id += 1
            for sat in con.satellites if isinstance(con.satellites, list) else [con.satellites]:
                if sat._id is None:
                    sat._id = 'sat-{}'.format(sat_id)
                    sat_id += 1
        self.spaceSegment = spaceSegment if isinstance(spaceSegment, list) else [spaceSegment]
        gs_id = 0
        for net in groundSegment if isinstance(groundSegment, list) else [groundSegment]:
            for gs in net.groundStations if isinstance(net.groundStations, list) else [net.groundStations]:
                if gs._id is None:
                    gs._id = 'gs-{}'.format(gs_id)
                    gs_id += 1
        self.groundSegment = groundSegment if isinstance(groundSegment, list) else [groundSegment]
        super(Architecture, self).__init__(_id, "Architecture")

    @staticmethod
    def from_dict(d):
        """Parses an architecture from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the architecture
        :rtype: Architecture
        """
        return Architecture(
                spaceSegment = Constellation.from_json(d.get("spaceSegment", None)),
                groundSegment = GroundNetwork.from_json(d.get("groundSegment", None)),
                _id = d.get("@id", None)
            )
