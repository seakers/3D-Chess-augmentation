#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Object models and methods for managing launch vehicle elements.
"""

import json

from .util import Entity

class LaunchVehicle(Entity):
    """An entity delivering satellites to orbit.

    :param name: Full name of this entity.
    :type name: str
    :param acronym: Acronym, initialism, or abbreviation.
    :type acronym: str
    :param inclination: Angle (decimal degrees) of an orbital trajectory \
    with respect to the equatorial plane. Ranges between 0° for a prograde \
    (with Earth's rotation) equatorial orbit to 180° for a retrograde \
    (opposite of Earth's rotation) equatorial orbit.
    :type inclination: str
    :param mass: Total wet mass (kg) including any consumable propellants or gases.
    :type mass: float
    :param maxAltitude: Maximum altitude (km) of a circular orbit achievable \
    by the launch vehicle at a given inclination.
    :type maxAltitude: float
    :param maxAltitudePayloadMass: Maximum payload mass (kg) deliverable to \
    the maximum altitude.
    :type maxAltitudePayloadMass: float
    :param minAltitude: Minimum altitude (km) of a circular orbit achievable \
    by the launch vehicle at a given inclination.
    :type minAltitude: float
    :param minAltitudePayloadMass: Maximum payload mass (kg) deliverable to \
    the minimum altitude.
    :type minAltitudePayloadMass: float
    :param finalStageDryMass: Mass (kg) of the final stage without any \
    payload or propellant.
    :type finalStageDryMass: float
    :param finalStagePropellantMass: Maximum mass (kg) of propellant for \
    the final stage.
    :type finalStagePropellantMass: float
    :param finalStageSpecificImpulse: Measure of efficiency (s) of the final \
    stage computed as the total impulse per unit of propellant.
    :type finalStageSpecificImpulse: float
    :param finalStageBurnDuration: Burn duration (s) of the final stage.
    :type finalStageBurnDuration: float
    :param finalStageRestartable: Denotes whether the final stage is restartable.
    :type finalStageRestartable: bool
    :param maxNumberRestarts: Maximum number of restarts of the upper stage \
    engine. Defaults to 5 if the upper stage is restartable.
    :type maxNumberRestarts: int
    :param reliability: Probability of success measured as the number of \
    successes divided by the total number of launch attempts. Ranges between 0.0 and 1.0.
    :type reliability: float
    :param cost: Cost ($M) to purchase this object or service.
    :type cost: float
    :param operatingNation: Operating nation specified by a three-letter \
    ISO-3166-1 alpha-3 country code.
    :type operatingNation: str
    :param fairingDiameter: Diameter (m) of the launch vehicle fairing.
    :type fairingDiameter: str
    :param fairingHeight: Height (m) of the launch vehicle fairing.
    :type fairingHeight: str
    """

    def __init__(self, name=None, acronym=None, inclination=None, mass=None,
            maxAltitude=None, maxAltitudePayloadMass=None, minAltitude=None,
            minAltitudePayloadMass=None, finalStageDryMass=None,
            finalStagePropellantMass=None, finalStageSpecificImpulse=None,
            finalStageBurnDuration=None, finalStageRestartable=None,
            maxNumberRestarts=None, reliability=None, cost=None,
            operatingNation=None, fairingDiameter=None, fairingHeight=None, _id=None):
        self.name = name
        self.acronym = acronym if acronym else name
        self.inclination = inclination
        self.mass = mass
        self.maxAltitude = maxAltitude
        self.maxAltitudePayloadMass = maxAltitudePayloadMass
        self.minAltitude = minAltitude
        self.minAltitudePayloadMass = minAltitudePayloadMass
        self.finalStageDryMass = finalStageDryMass
        self.finalStagePropellantMass = finalStagePropellantMass
        self.finalStageSpecificImpulse = finalStageSpecificImpulse
        self.finalStageBurnDuration = finalStageBurnDuration
        self.finalStageRestartable = finalStageRestartable
        self.maxNumberRestarts = maxNumberRestarts
        self.reliability = reliability
        self.cost = cost
        self.operatingNation = operatingNation
        self.fairingDiameter = fairingDiameter
        self.fairingHeight = fairingHeight
        super(LaunchVehicle,self).__init__(_id, "LaunchVehicle")

    @staticmethod
    def from_dict(d):
        """Parses a launch vehicle from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the launch vehicle
        :rtype: LaunchVehicle
        """
        return LaunchVehicle(
                name = d.get("name", None),
                acronym = d.get("acronym", None),
                inclination = d.get("inclination", None),
                mass = d.get("mass", None),
                maxAltitude = d.get("maxAltitude", None),
                maxAltitudePayloadMass = d.get("maxAltitudePayloadMass", None),
                minAltitude = d.get("minAltitude", None),
                minAltitudePayloadMass = d.get("minAltitudePayloadMass", None),
                finalStageDryMass = d.get("finalStageDryMass", None),
                finalStagePropellantMass = d.get("finalStagePropellantMass", None),
                finalStageSpecificImpulse = d.get("finalStageSpecificImpulse", None),
                finalStageBurnDuration = d.get("finalStageBurnDuration", None),
                finalStageRestartable = d.get("finalStageRestartable", None),
                maxNumberRestarts = d.get("maxNumberRestarts", None),
                reliability = d.get("reliability", None),
                cost = d.get("cost", None),
                operatingNation = d.get("operatingNation", None),
                fairingDiameter = d.get("fairingDiameter", None),
                fairingHeight = d.get("fairingHeight", None),
                _id = d.get("@id", None)
            )
