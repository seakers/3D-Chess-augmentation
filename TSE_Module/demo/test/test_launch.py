#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Unit tests for tatc.launch module.
"""

import unittest
import json
import isodate
import datetime

from tatc import *

class TestLaunchVehicle(unittest.TestCase):
    def test_from_json_basic(self):
        o = LaunchVehicle.from_json('{"@type": "LaunchVehicle", "name": "Atlas V 401 28.5", "acronym": "Atlas V 401", "inclination": 28.5, "maxAltitude": 2000, "maxAltitudePayloadMass": 6000, "minAltitude": 200, "minAltitudePayloadMass": 9800, "mass": 333320, "finalStageDryMass": 2030, "finalStagePropellantMass": 20800, "finalStageSpecificImpulse": 450, "finalStageBurnDuration": 835, "finalStageRestartable": true, "reliability": 0.948718, "cost": 150, "operatingNation": "USA", "fairingDiameter": 4.2, "fairingHeight": 12.0}')
        self.assertEqual(o.name, "Atlas V 401 28.5")
        self.assertEqual(o.acronym, "Atlas V 401")
        self.assertEqual(o.inclination, 28.5)
        self.assertEqual(o.maxAltitude, 2000)
        self.assertEqual(o.maxAltitudePayloadMass, 6000)
        self.assertEqual(o.minAltitude, 200)
        self.assertEqual(o.minAltitudePayloadMass, 9800)
        self.assertEqual(o.mass, 333320)
        self.assertEqual(o.finalStageDryMass, 2030)
        self.assertEqual(o.finalStagePropellantMass, 20800)
        self.assertEqual(o.finalStageSpecificImpulse, 450)
        self.assertEqual(o.finalStageBurnDuration, 835)
        self.assertEqual(o.finalStageRestartable, True)
        self.assertIsNone(o.maxNumberRestarts)
        self.assertEqual(o.reliability, 0.948718)
        self.assertEqual(o.cost, 150)
        self.assertEqual(o.operatingNation, "USA")
        self.assertEqual(o.fairingDiameter, 4.2)
        self.assertEqual(o.fairingHeight, 12.0)
        self.assertEqual(o._type, "LaunchVehicle")
        self.assertIsNone(o._id)
    def test_to_json_basic(self):
        d = json.loads(LaunchVehicle(name="Atlas V 401 28.5", acronym="Atlas V 401", inclination=28.5, maxAltitude=2000, maxAltitudePayloadMass=6000, minAltitude=200, minAltitudePayloadMass=9800, mass=333320, finalStageDryMass=2030, finalStagePropellantMass=20800, finalStageSpecificImpulse=450, finalStageBurnDuration=835, finalStageRestartable=True, reliability=0.948718, cost=150, operatingNation="USA", fairingDiameter=4.2, fairingHeight=12.0).to_json())
        self.assertEqual(d.get("name"), "Atlas V 401 28.5")
        self.assertEqual(d.get("acronym"), "Atlas V 401")
        self.assertEqual(d.get("inclination"), 28.5)
        self.assertEqual(d.get("maxAltitude"), 2000)
        self.assertEqual(d.get("maxAltitudePayloadMass"), 6000)
        self.assertEqual(d.get("minAltitude"), 200)
        self.assertEqual(d.get("minAltitudePayloadMass"), 9800)
        self.assertEqual(d.get("mass"), 333320)
        self.assertEqual(d.get("finalStageDryMass"), 2030)
        self.assertEqual(d.get("finalStagePropellantMass"), 20800)
        self.assertEqual(d.get("finalStageSpecificImpulse"), 450)
        self.assertEqual(d.get("finalStageBurnDuration"), 835)
        self.assertEqual(d.get("finalStageRestartable"), True)
        self.assertIsNone(d.get("maxNumberRestarts"))
        self.assertEqual(d.get("reliability"), 0.948718)
        self.assertEqual(d.get("cost"), 150)
        self.assertEqual(d.get("operatingNation"), "USA")
        self.assertEqual(d.get("fairingDiameter"), 4.2)
        self.assertEqual(d.get("fairingHeight"), 12.0)
        self.assertEqual(d.get("@type"), "LaunchVehicle")
        self.assertIsNone(d.get("@id"))
