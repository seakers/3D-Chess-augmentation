#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Unit tests for tatc.instrument module.
"""

import unittest
import json

from tatc import *

class TestInstrument(unittest.TestCase):
    def test_from_json_basic(self):
        o = Instrument.from_json('{"name": "OLI/TIRS", "agency": {"agencyType": "GOVERNMENT"}, "mass": 657, "volume": 10.016, "power": 319, "fieldOfView": 7.5}')
        self.assertEqual(o.name, "OLI/TIRS")
        self.assertIsInstance(o.agency, Agency)
        self.assertEqual(o.agency.agencyType, AgencyType.GOVERNMENT)
        self.assertEqual(o.mass, 657)
        self.assertEqual(o.volume, 10.016)
        self.assertEqual(o.power, 319)
        self.assertIsInstance(o.fieldOfView, FieldOfView)
        self.assertEqual(o.fieldOfView.fullConeAngle, 7.5)
        self.assertEqual(o._type, "Basic Sensor")
        self.assertIsNone(o._id)
    def test_to_json_basic(self):
        d = json.loads(Instrument(name="OLI/TIRS", agency=Agency(agencyType="GOVERNMENT"), mass=657, volume=10.016, power=319, fieldOfView=FieldOfView(sensorGeometry="CONICAL", fullConeAngle=7.5)).to_json())
        self.assertEqual(d.get("name"), "OLI/TIRS")
        self.assertEqual(d.get("agency").get("agencyType"), "GOVERNMENT")
        self.assertEqual(d.get("mass"), 657)
        self.assertEqual(d.get("volume"), 10.016)
        self.assertEqual(d.get("power"), 319)
        self.assertEqual(d.get("fieldOfView")["@type"], "FieldOfView")
        self.assertEqual(d.get("fieldOfView")["fullConeAngle"], 7.5)
        self.assertEqual(d.get("@type"), "Basic Sensor")
        self.assertIsNone(d.get("@id"))
class TestFieldOfView(unittest.TestCase):
    def test_from_json_conical(self):
        o = FieldOfView.from_json('{"sensorGeometry": "CONICAL", "fullConeAngle": 7.5}')
        self.assertEqual(o.sensorGeometry, SensorGeometry.CONICAL)
        self.assertEqual(o.fullConeAngle,  7.5)
        self.assertEqual(o._type, "FieldOfView")
        self.assertIsNone(o._id)
    def test_from_json_rectangular(self):
        o = FieldOfView.from_json('{"sensorGeometry": "RECTANGULAR", "alongTrackFieldOfView": 10, "crossTrackFieldOfView": 15}')
        self.assertEqual(o.sensorGeometry, SensorGeometry.RECTANGULAR)
        self.assertEqual(o.alongTrackFieldOfView,  10)
        self.assertEqual(o.crossTrackFieldOfView,  15)
        self.assertEqual(o._type, "FieldOfView")
        self.assertIsNone(o._id)
    def test_from_json_custom(self):
        o = FieldOfView.from_json('{"sensorGeometry": "CUSTOM", "customConeAnglesVector": [10,15], "customClockAnglesVector": [0,180]}')
        self.assertEqual(o.sensorGeometry, SensorGeometry.CUSTOM)
        self.assertEqual(o.customConeAnglesVector,  [10,15])
        self.assertEqual(o.customClockAnglesVector,  [0,180])
        self.assertEqual(o._type, "FieldOfView")
        self.assertIsNone(o._id)
