#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Object models and methods for managing space system elements including \
satellites, orbits, and constellations.
"""

import json
import math
from numbers import Number
import isodate
import datetime
import itertools
import copy

from .util import Entity, EnumEntity, CommunicationBand, QuantitativeRange
from .instrument import Instrument
from .launch import LaunchVehicle

class PropellantType(EnumEntity):
    """Enumeration of recognized propellant types.

    :var COLD_GAS: cold gas propellant.
    :vartype COLD_GAS: PropellantType
    :var SOLID: solid propellant.
    :vartype SOLID: PropellantType
    :var LIQUID_MONO_PROP: liquid monopropellant.
    :vartype LIQUID_MONO_PROP: PropellantType
    :var LIQUID_BI_PROP: liquid bipropellant.
    :vartype LIQUID_BI_PROP: PropellantType
    :var HYBRID: hybrid propellant.
    :vartype HYBRID: PropellantType
    :var ELECTROTHERMAL: electrothermal propellant.
    :vartype ELECTROTHERMAL: PropellantType
    :var ELECTROSTATIC: electrostatic propellant.
    :vartype ELECTROSTATIC: PropellantType
    :var MONO_PROP: monopropellant.
    :vartype MONO_PROP: PropellantType
    """
    COLD_GAS = "COLD_GAS"
    SOLID = "SOLID"
    LIQUID_MONO_PROP = "LIQUID_MONO_PROP"
    LIQUID_BI_PROP = "LIQUID_BI_PROP"
    HYBRID = "HYBRID"
    ELECTROTHERMAL = "ELECTROTHERMAL"
    ELECTROSTATIC = "ELECTROSTATIC"
    MONO_PROP = "MONO_PROP"

class StabilizationType(EnumEntity):
    """Enumeration of recognized stabilization types.

    :var AXIS_3: three-axis stabilization.
    :vartype AXIS_3: StabilizationType
    :var SPINNING: spin-stabilized.
    :vartype SPINNING: StabilizationType
    :var GRAVITY_GRADIENT: gravity gradient stabilization.
    :vartype GRAVITY_GRADIENT: StabilizationType
    """
    AXIS_3 = "AXIS_3"
    SPINNING = "SPINNING"
    GRAVITY_GRADIENT = "GRAVITY_GRADIENT"

class Satellite(Entity):
    """An entity orbiting the Earth in support of mission objectives.

    :param name: Full name of this entity.
    :type name: str
    :param acronym: Acronym, initialism, or abbreviation.
    :type acronym: str
    :param mass: Total mass (kg) including any consumable propellant or gases (default: 0).
    :type mass: float
    :param dryMass: Total mass (kg) excluding any consumable propellant or gases (default: mass).
    :type dryMass: float
    :param volume: Total volume (m3).
    :type volume: float
    :param power: Nominal operating power (W).
    :type power: float
    :param commBand: List of communication bands available for broadcast. \
    Recognized values include: VHF, UHF, L, S, C, X, Ku, Ka, Laser.
    :type commBand: list(CommunicationBand)
    :param payload: List of instruments carried onboard this satellite.
    :type payload: Instrument or list[Instrument]
    :param orbit: Orbital trajectory of this satellite.
    :type orbit: Orbit
    :param techReadinessLevel: Spacecraft technology readiness level (default: 9).
    :type techReadinessLevel: int
    :param isGroundCommand: Command performed by ground station (default: True).
    :type isGroundCommand: bool
    :param isSpare: Spacecraft is a spare (default: False).
    :type isSpace: bool
    :param propellantType: Type of propellant. Recognized values include: \
    COLD_GAS, SOLID, LIQUID_MONO_PROP, LIQUID_BI_PROP, HYBRID, ELECTROTHERMAL, \
    ELECTROSTATIC, MONO_PROP (default).
    :type propellantType: str or PropellantType
    :param stabilizationType: Type of spacecraft stabilization. Recognized \
    values include: AXIS_3 (default), SPINNING, GRAVITY_GRADIENT.
    :type stabilizationType: str or StabilizationType
    """

    def __init__(self, name=None, acronym=None, mass=0.0, dryMass=None, volume=0.0,
                 power=0.0, commBand=None, payload=None, orbit=None,
                 techReadinessLevel=9, isGroundCommand=True, isSpare=False,
                 propellantType="MONO_PROP", stabilizationType="AXIS_3", _id=None):
        self.name = name
        self.acronym = acronym if acronym is not None else name
        self.mass = mass
        self.dryMass = dryMass if dryMass is not None else mass
        self.volume = volume
        self.power = power
        self.commBand = commBand
        self.payload = payload
        self.orbit = orbit
        self.techReadinessLevel = techReadinessLevel
        self.isGroundCommand = isGroundCommand
        self.isSpare = isSpare
        self.propellantType = propellantType
        self.stabilizationType = stabilizationType
        super(Satellite,self).__init__(_id, "Satellite")

    @staticmethod
    def from_dict(d):
        """Parses a satellite from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the satellite
        :rtype: Satellite
        """
        return Satellite(
                name = d.get("name", None),
                acronym = d.get("acronym", None),
                mass = d.get("mass", 0),
                dryMass = d.get("dryMass", None),
                volume = d.get("volume", 0),
                power = d.get("power", 0),
                commBand = CommunicationBand.get(d.get("commBand", None)),
                payload = Instrument.from_json(d.get("payload", None)),
                orbit = Orbit.from_json(d.get("orbit", None)),
                techReadinessLevel = d.get("techReadinessLevel", 9),
                isGroundCommand = d.get("isGroundCommand", True),
                isSpare = d.get("isSpare", False),
                propellantType = d.get("propellantType", "MONO_PROP"),
                stabilizationType = d.get("stabilizationType", "AXIS_3"),
                _id = d.get("@id", None)
            )

class OrbitType(EnumEntity):
    """Enumeration of recognized orbit types.

    :var KEPLERIAN: Keplerian orbit.
    :vartype KEPLERIAN: OrbitType
    :var CIRCULAR: circular orbit.
    :vartype CIRCULAR: OrbitType
    :var SUN_SYNCHRONOUS: sun-synchronous orbit.
    :vartype SUN_SYNCHRONOUS: OrbitType
    """
    KEPLERIAN = "KEPLERIAN"
    CIRCULAR = "CIRCULAR"
    SUN_SYNCHRONOUS = "SUN_SYNCHRONOUS"

class Orbit(Entity):
    """An orbital trajectory about the Earth.

    :param orbitType: Type of orbit. Recognized values include: KEPLERIAN, \
    CIRCULAR, SUN_SYNCHRONOUS.
    :type orbitType: str or OrbitType
    :param altitude: Average distance (km) above mean sea level with respect \
    to the WGS 84 geodetic model (for circular orbits).
    :type altitude: float or list[float] or QuantitativeRange
    :param inclination: Angle (decimal degrees) of an obrital trajectory with \
    respect to the equatorial plane. Ranges between 0 for a prograde (with \
    Earth's rotation) equatorial orbit to 180 for a retrograde (opposite of \
    Earth's rotation) equatorial orbit. Also accepts special values including: \
    SSO (sun-synchronous).
    :type inclination: str or float or list[float] or QuantitativeRange
    :param semimajorAxis: Average of the distances (km) of periapsis (closest \
    approach) and apoapsis (furthest extent) between the center of masses of a \
    planetary body and a satellite.
    :type semimajorAxis: float
    :param eccentricity: Nondimensional measure of deviation from a circular \
    orbit. Ranges from 0.0 for a circular orbit to 1.0 for a parabolic escape \
    orbit or > 1 for hyperbolic escape orbits.
    :type eccentricity: float
    :param periapsisArgument: Angle (decimal degrees) between the ascending \
    node (location at which the orbit crosses the equatorial plane moving \
    north) and the periapsis (point of closest extent).
    :type periapsisArgument: float
    :param rightAscensionAscendingNode: Angle (decimal degrees) between the \
    ascending node (location at which the orbit crosses the equatorial plane \
    moving north) and the frame's vernal point (vector between the Sun and the \
    Earth on the vernal equinox).
    :type rightAscensionAscendingNode: float
    :param trueAnomaly: Angle (decimal degrees) between the satellite and its \
    argument of periapsis.
    :type trueAnomaly: float
    :param epoch: The initial or reference point in time (ISO 8601) for a \
    set of Keplerian orbital elements.
    :type epoch: str
    :param localSolarTimeAscendingNode: Local time (ISO 8601) as measured by \
    the angle of the sun when crossing the equator in the northerly \
    (ascending) direction.
    :type localSolarTimeAscendingNode: str
    """

    def __init__(self, orbitType=None, altitude=None, inclination=None,
                 semimajorAxis=None, eccentricity=None, periapsisArgument=None,
                 rightAscensionAscendingNode=None, trueAnomaly=None, epoch=None,
                 localSolarTimeAscendingNode=None, _id=None):
        self.orbitType = OrbitType.get(orbitType)
        self.altitude = altitude
        # compute and assign special inclination for sun-synchronous orbits
        if isinstance(self.altitude, Number) and (inclination == "SSO" or self.orbitType == OrbitType.SUN_SYNCHRONOUS):
            self.inclination = Orbit.get_sso_inclination(self.altitude)
        else: self.inclination = inclination
        self.semimajorAxis = semimajorAxis
        # assign 0 eccentricity for circular orbits
        if self.orbitType == OrbitType.CIRCULAR or self.orbitType == OrbitType.SUN_SYNCHRONOUS:
            self.eccentricity = 0.0
        # assign 0 eccentricity for Keplerian orbits with missing information
        elif self.orbitType == OrbitType.KEPLERIAN and eccentricity is None:
            self.eccentricity = 0.0
        else: self.eccentricity = eccentricity
        # assign 0 periapsis arg for Keplerian orbits with missing information
        if self.orbitType == OrbitType.KEPLERIAN and periapsisArgument is None:
            self.periapsisArgument = 0.0
        else: self.periapsisArgument = periapsisArgument
        self.rightAscensionAscendingNode = rightAscensionAscendingNode
        self.trueAnomaly = trueAnomaly
        self.epoch = epoch
        self.localSolarTimeAscendingNode = localSolarTimeAscendingNode
        super(Orbit,self).__init__(_id, "Orbit")

    def __iter__(self):
        # iterate altitudes
        try: altitudeIter = iter(self.altitude)
        except TypeError: altitudeIter = [self.altitude]
        # iterate inclinations
        try:
            if isinstance(self.inclination, str): raise TypeError
            inclinationIter = iter(self.inclination)
        except TypeError: inclinationIter = [self.inclination]

        # return the Cartesian product of iterated values
        return iter([
                Orbit(
                    orbitType=self.orbitType,
                    altitude=altitude,
                    inclination=inclination,
                    semimajorAxis=self.semimajorAxis,
                    eccentricity=self.eccentricity,
                    periapsisArgument=self.periapsisArgument,
                    rightAscensionAscendingNode=self.rightAscensionAscendingNode,
                    trueAnomaly=self.trueAnomaly,
                    epoch=self.epoch,
                    localSolarTimeAscendingNode=self.localSolarTimeAscendingNode
                )
                for altitude, inclination
                in itertools.product(altitudeIter, inclinationIter)
            ])

    @staticmethod
    def get_orbital_period(altitude):
        """Returns the orbital period (s) for a given altitude (km).

        :param altitude: the altitude.
        :type altitude: float
        """
        re = 6378.14 # radius of the earth (km)
        sma = re + altitude # semimajor axis (km)
        mu = 3.98600440*(10**5) # gravitational constant (km^3/s^2)
        return 2*math.pi*math.sqrt((sma**3)/mu)

    @staticmethod
    def get_semimajor_axis(altitude):
        """Returns the semimajor axis for a given altitude (km).

        :param altitude: the altitude.
        :type altitude: float
        """
        re = 6378.14 # radius of the earth (km)
        return re + altitude

    @staticmethod
    def get_sso_inclination(altitude):
        """Returns the sun-synchronous inclination (deg) for a given altitude (km).

        :param altitude: the altitude.
        :type altitude: float
        """
        re = 6378.14 # radius of the earth (km)
        coef = 10.10949 # calculation coefficient
        return math.degrees(math.acos((((re+altitude)/re)**3.5)/(-coef)))

    @staticmethod
    def from_dict(d):
        """Parses an orbit from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the orbit
        :rtype: Orbit
        """
        return Orbit(
                orbitType = d.get("orbitType", None),
                altitude = QuantitativeRange.from_json(d.get("altitude", None)),
                inclination = QuantitativeRange.from_json(d.get("inclination", None)),
                semimajorAxis = d.get("semimajorAxis", None),
                eccentricity = d.get("eccentricity", None),
                periapsisArgument = d.get("periapsisArgument", None),
                rightAscensionAscendingNode = d.get("rightAscensionAscendingNode", None),
                trueAnomaly = d.get("trueAnomaly", None),
                epoch = d.get("epoch", None),
                localSolarTimeAscendingNode = d.get("localSolarTimeAscendingNode", None),
                _id = d.get("@id", None)
            )

class ConstellationType(EnumEntity):
    """Enumeration of recognized constellation types.

    :var DELTA_HOMOGENEOUS: Walker Delta constellation with homogeneous satellites.
    :vartype DELTA_HOMOGENEOUS: ConstellationType
    :var DELTA_HETEROGENEOUS: Walker Delta constellation with heterogeneous satellites.
    :vartype DELTA_HETEROGENEOUS: ConstellationType
    :var PRECESSING: Processing constellation.
    :vartype PRECESSING: ConstellationType
    :var AD_HOC: Ad-hoc constellation.
    :vartype AD_HOC: ConstellationType
    :var TRAIN: Train-style constellation.
    :vartype TRAIN: ConstellationType
    :var EXISTING: Existing constellation.
    :vartype EXISTING: ConstellationType
    """
    DELTA_HOMOGENEOUS = "DELTA_HOMOGENEOUS"
    DELTA_HETEROGENEOUS = "DELTA_HETEROGENEOUS"
    PRECESSING = "PRECESSING"
    AD_HOC = "AD_HOC"
    TRAIN = "TRAIN"
    EXISTING = "EXISTING"

class Constellation(Entity):
    """A set of orbital trajectories about the Earth.

    :param constellationType: Type of constellation. Recognized values include: \
    DELTA_HOMOGENEOUS, DELTA_HETEROGENEOUS, PRECESSING, AD_HOC, TRAIN, EXISTING.
    :type constellationType: str or ConstellationType
    :param numberSatellites: Total number of satellites.
    :type numberSatellites: int or list[int] or QuantitativeRange
    :param numberPlanes: Number of equally-spaced orbital planes for a \
    Walker-type constellation. Ranges from 1 to (number of satellites). \
    Defaults to 1 for Walker Delta constellations.
    :type numberPlanes: int or list[int] or QuantitativeRange
    :param relativeSpacing: Relative spacing of satellites between plans for a \
    Walker-type constellation. Ranges from 0 for equal true anomaly to \
    (number of planes) - 1. Defaults to 1 for sun-synchronous multi-plane \
    Walker Delta constellations or 0 for other Walker Delta constellations.
    :type relativeSpacing: int or list[int] or QuantitativeRange
    :param satelliteInterval: The local time interval (ISO 8601 format) between \
    satellites in a train-type constellation.
    :type satelliteInterval: str or list[str]
    :param orbit: Orbital trajectory of member satellites.
    :type orbit: Orbit or list[Orbit]
    :param satellites: List of member satellites.
    :type satellites: list[Satellite]
    :param secondaryPayload: Toggle if this constellation utilizes secondary payloads (default: None, equivalent to False).
    :type secondaryPayload: bool
    """
    def __init__(self, constellationType=None, numberSatellites=1, numberPlanes=None,
                relativeSpacing=None, satelliteInterval=None, orbit=None,
                satellites=None, secondaryPayload=None, _id=None):
        self.constellationType = ConstellationType.get(constellationType)
        self.numberSatellites = len(satellites) if self.constellationType == ConstellationType.EXISTING else numberSatellites
        # assign default value of 1 plane for delta constellations
        if numberPlanes is None and (self.constellationType == ConstellationType.DELTA_HOMOGENEOUS
                or self.constellationType == ConstellationType.DELTA_HETEROGENEOUS):
            self.numberPlanes = 1
        else: self.numberPlanes = numberPlanes

        # assign default relative spacing value for delta constellations
        if relativeSpacing is None and isinstance(self.numberPlanes, Number) and (
                self.constellationType == ConstellationType.DELTA_HOMOGENEOUS
                or self.constellationType == ConstellationType.DELTA_HETEROGENEOUS):
            # assign relative spacing of 1 for multi-plane sun-synchronous constellations
            if isinstance(orbit, Orbit) and self.numberPlanes > 1 and (
                    orbit.orbitType == OrbitType.SUN_SYNCHRONOUS
                    or orbit.inclination == "SSO"):
                self.relativeSpacing = 1
            # otherwise assign relative spacing of 0
            else: self.relativeSpacing = 0
        else:
            self.relativeSpacing = relativeSpacing
        if isinstance(satelliteInterval, Number):
            self.satelliteInterval = isodate.duration_isoformat(datetime.timedelta(minutes=satelliteInterval))
        else: self.satelliteInterval = satelliteInterval
        self.orbit = orbit
        self.satellites = satellites
        self.secondaryPayload = secondaryPayload
        super(Constellation,self).__init__(_id, "Constellation")

    def generate_delta_orbits(self, epoch=None):
        """Generates Walker delta orbital elements for each member satellite.

        :param epoch: The default epoch value (ISO 8601 datetime format).
        :type epoch: str
        :return: the list of orbits
        :rtype: list[Orbit]
        """
        orbits = []
        for satellite in range(self.numberSatellites):
            satellitesPerPlane = math.ceil(self.numberSatellites/self.numberPlanes)
            plane = satellite // satellitesPerPlane
            orbits.append(
                Orbit(
                    orbitType="KEPLERIAN",
                    inclination=self.orbit.inclination,
                    semimajorAxis=self.orbit.semimajorAxis if self.orbit.semimajorAxis else Orbit.get_semimajor_axis(self.orbit.altitude),
                    eccentricity=self.orbit.eccentricity,
                    periapsisArgument=self.orbit.periapsisArgument,
                    rightAscensionAscendingNode=plane*360./self.numberPlanes,
                    trueAnomaly=((satellite % satellitesPerPlane)*self.numberPlanes
                                 + self.relativeSpacing*plane)*360./(satellitesPerPlane*self.numberPlanes),
                    epoch=self.orbit.epoch if self.orbit.epoch else epoch
                )
            )
        return orbits

    def generate_constellations(self, satellites, epoch=None):
        """Generates constellations for a given set of satellites.

        :param satellites: List of member satellites.
        :type satellites: list[Satellite]
        :param epoch: The default epoch value (ISO 8601 datetime format).
        :type epoch: str
        :return: the list of constellations
        :rtype: list[Constellation]
        """
        constellations = []
        for constellation in self:
            if self.constellationType == ConstellationType.EXISTING:
                # if constellation has existing satellites, automatically include
                constellations.append(constellation)
            elif constellation.constellationType == ConstellationType.DELTA_HOMOGENEOUS:
                # generate one constellation iteration per satellite
                for satellite in self.satellites if self.satellites is not None else satellites:
                    selectedSatellites = [None] * constellation.numberSatellites
                    for i in range(constellation.numberSatellites):
                        selectedSatellites[i] = copy.deepcopy(satellite)
                        if selectedSatellites[i]._id is not None:
                            selectedSatellites[i]._id = '{}_{}'.format(selectedSatellites[i]._id, i+1)
                    for i, orbit in enumerate(self.generate_delta_orbits(epoch)):
                        selectedSatellites[i].orbit = orbit
                    constellations.append(
                        Constellation(
                            constellationType=constellation.constellationType,
                            numberSatellites=constellation.numberSatellites,
                            numberPlanes=constellation.numberPlanes,
                            relativeSpacing=constellation.relativeSpacing,
                            orbit=constellation.orbit,
                            satellites=selectedSatellites,
                            secondaryPayload=constellation.secondaryPayload
                        )
                    )
            elif self.constellationType == ConstellationType.DELTA_HETEROGENEOUS:
                raise NotImplementedError
            elif self.constellationType == ConstellationType.AD_HOC:
                raise NotImplementedError
            elif self.constellationType == ConstellationType.PRECESSING:
                raise NotImplementedError
            else:
                raise NotImplementedError
        return constellations

    def __iter__(self):
        # if existing satellites, return self
        if self.constellationType == ConstellationType.EXISTING:
            return iter([self])
        # iterate number satellites
        try: numberSatellitesIter = iter(self.numberSatellites)
        except TypeError: numberSatellitesIter = [self.numberSatellites]
        # iterate number planes
        try: numberPlanesIter = iter(self.numberPlanes)
        except TypeError: numberPlanesIter = [self.numberPlanes]
        # iterate relative phasing
        try: relativeSpacingIter = iter(self.relativeSpacing)
        except TypeError: relativeSpacingIter = [self.relativeSpacing]
        # iterate satellite interval
        try:
            if isinstance(self.satelliteInterval, str): raise TypeError
            satelliteIntervalIter = iter(self.satelliteInterval)
        except TypeError: satelliteIntervalIter = [self.satelliteInterval]
        # iterate orbit
        try: orbitIter = iter(self.orbit)
        except TypeError: orbitIter = [self.orbit]
        # return the Cartesian product of iterated values
        return iter([
                Constellation(
                    constellationType=self.constellationType,
                    numberSatellites=numberSatellites,
                    numberPlanes=numberPlanes,
                    relativeSpacing=relativeSpacing,
                    satelliteInterval=satelliteInterval,
                    orbit=orbit,
                    satellites=self.satellites,
                    secondaryPayload=self.secondaryPayload
                )
                for numberSatellites, numberPlanes, relativeSpacing, satelliteInterval, orbit
                in itertools.product(numberSatellitesIter, numberPlanesIter, relativeSpacingIter, satelliteIntervalIter, orbitIter)
                if ((relativeSpacing is None or
                        numberPlanes is None or
                        relativeSpacing < numberPlanes) and
                    (numberSatellites is None or
                        numberPlanes is None or
                        numberPlanes <= numberSatellites))
            ])

    @staticmethod
    def from_dict(d):
        """Parses a constellation from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the constellation
        :rtype: Constellation
        """
        return Constellation(
                constellationType = d.get("constellationType", None),
                numberSatellites = QuantitativeRange.from_json(d.get("numberSatellites", 1)),
                numberPlanes = QuantitativeRange.from_json(d.get("numberPlanes", None)),
                relativeSpacing = QuantitativeRange.from_json(d.get("relativeSpacing", None)),
                satelliteInterval = d.get("satelliteInterval", None),
                orbit = Orbit.from_json(d.get("orbit", None)),
                satellites = Satellite.from_json(d.get("satellites", None)),
                secondaryPayload = d.get("secondaryPayload", None),
                _id = d.get("@id", None)
            )
