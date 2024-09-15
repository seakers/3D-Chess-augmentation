#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Unit tests for tatc.ground module.
"""

import unittest
import json

from tatc import *

class TestRegion(unittest.TestCase):
    def test_from_json_basic(self):
        o = Region.from_json('{"latitude": {"minValue": -90, "maxValue": 90}, "longitude": {"minValue": -180, "maxValue": 180}}')
        self.assertIsInstance(o.latitude, QuantitativeValue)
        self.assertEqual(o.latitude.minValue, -90)
        self.assertEqual(o.latitude.maxValue, 90)
        self.assertIsInstance(o.longitude, QuantitativeValue)
        self.assertEqual(o.longitude.minValue, -180)
        self.assertEqual(o.longitude.maxValue, 180)
        self.assertEqual(o._type, "Region")
        self.assertIsNone(o._id)
    def test_from_json_point(self):
        o = Region.from_json('{"latitude": 40.5974791834978, "longitude": -104.83875274658203}')
        self.assertEqual(o.latitude, 40.5974791834978)
        self.assertEqual(o.longitude, -104.83875274658203)
        self.assertEqual(o._type, "Region")
        self.assertIsNone(o._id)
    def test_to_json_basic(self):
        d = json.loads(Region(latitude=QuantitativeValue(minValue=-90,maxValue=90), longitude=QuantitativeValue(minValue=-180,maxValue=180)).to_json())
        self.assertEqual(d.get("latitude").get("minValue"), -90)
        self.assertEqual(d.get("latitude").get("maxValue"), 90)
        self.assertEqual(d.get("longitude").get("minValue"), -180)
        self.assertEqual(d.get("longitude").get("maxValue"), 180)
    def test_to_json_point(self):
        d = json.loads(Region(latitude=40.5974791834978, longitude=-104.83875274658203).to_json())
        self.assertEqual(d.get("latitude"), 40.5974791834978)
        self.assertEqual(d.get("longitude"), -104.83875274658203)

class TestGroundStation(unittest.TestCase):
    def test_from_json_basic(self):
        o = GroundStation.from_json('{"name": "Test", "latitude": 40.5974791834978, "longitude": -104.83875274658203, "elevation": 1570, "commBand": ["X"]}')
        self.assertEqual(o.name, "Test")
        self.assertIsNone(o.agency)
        self.assertEqual(o.latitude, 40.5974791834978)
        self.assertEqual(o.longitude, -104.83875274658203)
        self.assertEqual(o.elevation, 1570)
        self.assertEqual(o.commBand, ["X"])
        self.assertEqual(o._type, "GroundStation")
        self.assertIsNone(o._id)
    def test_from_json_comm_list(self):
        o = GroundStation.from_json('{"name": "Test", "commBand": ["X","S"]}')
        self.assertEqual(sorted(o.commBand), sorted(["X","S"]))
    def test_to_json_basic(self):
        d = json.loads(GroundStation(name="Test", latitude=40.5974791834978, longitude=-104.83875274658203, elevation=1570, commBand=["X"]).to_json())
        self.assertEqual(d.get("name"), "Test")
        self.assertEqual(d.get("latitude"), 40.5974791834978)
        self.assertEqual(d.get("longitude"), -104.83875274658203)
        self.assertEqual(d.get("elevation"), 1570)
        self.assertEqual(d.get("commBand"), ["X"])
        self.assertEqual(d.get("@type"), "GroundStation")
        self.assertIsNone(d.get("@id"))
    def test_to_json_comm_list(self):
        d = json.loads(GroundStation(name="Test", commBand=["X","S"]).to_json())
        self.assertEqual(sorted(d.get("commBand")), sorted(["X","S"]))

class TestGroundNetwork(unittest.TestCase):
    def test_from_json_basic(self):
        o = GroundNetwork.from_json('{"name": "Test", "agency":{"@agencyType": "GOVERNMENT"}, "numberStations": 2}')
        self.assertEqual(o.name, "Test")
        self.assertIsInstance(o.agency, Agency)
        self.assertEqual(o.numberStations, 2)
        self.assertIsNone(o._id)
    def test_from_json_station_list(self):
        o = GroundNetwork.from_json('{"name": "Test", "groundStations": [{"@type": "GroundStation"}, {"@type": "GroundStation"}]}')
        self.assertEqual(o.name, "Test")
        self.assertIsInstance(o.groundStations, list)
        self.assertEqual(len(o.groundStations), 2)
        self.assertIsInstance(o.groundStations[0], GroundStation)
        self.assertIsInstance(o.groundStations[1], GroundStation)
    def test_to_json_basic(self):
        d = json.loads(GroundNetwork(name="Test", agency=Agency(agencyType=AgencyType.GOVERNMENT), numberStations=2).to_json())
        self.assertEqual(d.get("name"), "Test")
        self.assertEqual(d.get("agency").get("@type"), "Agency")
        self.assertEqual(d.get("numberStations"), 2)
        self.assertIsNone(d.get("@id"))
    def test_to_json_station_list(self):
        d = json.loads(GroundNetwork(name="Test", groundStations=[GroundStation(), GroundStation()]).to_json())
        self.assertEqual(d.get("name"), "Test")
        self.assertIsInstance(d.get("groundStations"), list)
        self.assertEqual(len(d.get("groundStations")), 2)
        self.assertEqual(d.get("groundStations")[0].get("@type"), "GroundStation")
        self.assertEqual(d.get("groundStations")[1].get("@type"), "GroundStation")
    def test_iter(self):
        o = GroundNetwork(numberStations=QuantitativeRange(1,3,stepSize=1))
        self.assertEqual(len(list(o)), 3)
        for i in o:
            self.assertIsInstance(i, GroundNetwork)
        self.assertEqual(sorted([i.numberStations for i in o]), sorted([1,2,3]))
