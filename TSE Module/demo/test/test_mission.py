#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Unit tests for tatc.mission module.
"""

import unittest
import json
import isodate
import datetime

from tatc import *

class TestMissionConcept(unittest.TestCase):
    def test_from_json_basic(self):
        o = MissionConcept.from_json('{"name": "Sustainable Land Imaging", "acronym": "SLI", "agency": {"agencyType": "Government"}, "start": "2017-08-01T00:00:00Z", "duration": "P0Y0M90D", "target": {"latitude": {"minValue": 35, "maxValue": 45}, "longitude": {"minValue": -115, "maxValue": -100}}, "objectives": [{"objectiveName": "test"}]}')
        self.assertEqual(o.name, "Sustainable Land Imaging")
        self.assertEqual(o.acronym, "SLI")
        self.assertIsInstance(o.agency, Agency)
        self.assertEqual(o.agency.agencyType, AgencyType.GOVERNMENT)
        self.assertEqual(isodate.parse_datetime(o.start), isodate.parse_datetime("2017-08-01T00:00:00Z"))
        self.assertEqual(isodate.parse_duration(o.duration), isodate.parse_duration("P0Y0M90D"))
        self.assertIsInstance(o.target, Region)
        self.assertIsInstance(o.target.latitude, QuantitativeValue)
        self.assertEqual(o.target.latitude.minValue, 35)
        self.assertEqual(o.target.latitude.maxValue, 45)
        self.assertIsInstance(o.target.longitude, QuantitativeValue)
        self.assertEqual(o.target.longitude.minValue, -115)
        self.assertEqual(o.target.longitude.maxValue, -100)
        self.assertEqual(o.objectives[0].objectiveName, "test")
    def test_from_json_numeric_duration(self):
        o = MissionConcept.from_json('{"start": "2017-08-01T00:00:00Z", "duration": 90}')
        self.assertEqual(isodate.parse_duration(o.duration), datetime.timedelta(days=90))
    def test_to_json_basic(self):
        d = json.loads(MissionConcept(name="Sustainable Land Imaging", acronym="SLI", agency=Agency(agencyType=AgencyType.GOVERNMENT), start="2017-08-01T00:00:00Z", duration="P0Y0M90D", target=Region(latitude=QuantitativeValue(minValue=35,maxValue=45), longitude=QuantitativeValue(minValue=-115, maxValue=-100)), objectives=[MissionObjective(objectiveName="test")]).to_json())
        self.assertEqual(d.get("name"), "Sustainable Land Imaging")
        self.assertEqual(d.get("acronym"), "SLI")
        self.assertEqual(d.get("agency").get("agencyType"), "GOVERNMENT")
        self.assertEqual(d.get("start"), "2017-08-01T00:00:00Z")
        self.assertEqual(d.get("duration"), "P0Y0M90D")
        self.assertEqual(d.get("target").get("latitude").get("minValue"), 35)
        self.assertEqual(d.get("target").get("latitude").get("maxValue"), 45)
        self.assertEqual(d.get("target").get("longitude").get("minValue"), -115)
        self.assertEqual(d.get("target").get("longitude").get("maxValue"), -100)
        self.assertEqual(d.get("objectives")[0].get("objectiveName"), "test")
    def test_to_json_numeric_duration(self):
        d = json.loads(MissionConcept(start="2017-08-01T00:00:00Z", duration=90).to_json())
        self.assertEqual(isodate.parse_duration(d.get("duration")), datetime.timedelta(days=90))

class TestMissionObjective(unittest.TestCase):
    def test_from_json_basic(self):
        o = MissionObjective.from_json('{"objectiveName": "test", "objectiveWeight": 10, "objectiveType": "MAX"}')
        self.assertEqual(o.objectiveName, "test")
        self.assertEqual(o.objectiveWeight, 10)
        self.assertEqual(o.objectiveType, ObjectiveType.MAX)
    def test_to_json_basic(self):
        d = json.loads(MissionObjective(objectiveName="test", objectiveWeight=10, objectiveType=ObjectiveType.MAX).to_json())
        self.assertEqual(d.get("objectiveName"), "test")
        self.assertEqual(d.get("objectiveWeight"), 10)
        self.assertEqual(d.get("objectiveType"), "MAX")
    def test_from_json_target(self):
        o = MissionObjective.from_json('{"objectiveName": "test", "objectiveWeight": 10, "objectiveType": "TAR", "objectiveTarget": 100}')
        self.assertEqual(o.objectiveName, "test")
        self.assertEqual(o.objectiveWeight, 10)
        self.assertEqual(o.objectiveType, ObjectiveType.TAR)
        self.assertEqual(o.objectiveTarget, 100)
    def test_to_json_target(self):
        d = json.loads(MissionObjective(objectiveName="test", objectiveWeight=10, objectiveType=ObjectiveType.TAR, objectiveTarget=100).to_json())
        self.assertEqual(d.get("objectiveName"), "test")
        self.assertEqual(d.get("objectiveWeight"), 10)
        self.assertEqual(d.get("objectiveType"), "TAR")
        self.assertEqual(d.get("objectiveTarget"), 100)

class TestDesignSpace(unittest.TestCase):
    def test_from_json_basic(self):
        o = DesignSpace.from_json('{"spaceSegment": {"@type": "Constellation"}, "launchers": {"@type": "LaunchVehicle"}, "satellites": {"@type": "Satellite"}, "groundSegment": {"@type": "GroundNetwork"}, "groundStations": {"@type": "GroundStation"}}')
        self.assertIsInstance(o.spaceSegment, list)
        self.assertEqual(len(o.spaceSegment), 1)
        self.assertIsInstance(o.spaceSegment[0], Constellation)
        self.assertIsInstance(o.launchers, list)
        self.assertEqual(len(o.launchers), 1)
        self.assertIsInstance(o.launchers[0], LaunchVehicle)
        self.assertIsInstance(o.satellites, list)
        self.assertEqual(len(o.satellites), 1)
        self.assertIsInstance(o.satellites[0], Satellite)
        self.assertIsInstance(o.groundSegment, list)
        self.assertEqual(len(o.groundSegment), 1)
        self.assertIsInstance(o.groundSegment[0], GroundNetwork)
        self.assertIsInstance(o.groundStations, list)
        self.assertEqual(len(o.groundStations), 1)
        self.assertIsInstance(o.groundStations[0], GroundStation)
    def test_from_json_list(self):
        o = DesignSpace.from_json('{"spaceSegment":[{"@type": "Constellation"}, {"@type": "Constellation"}], "launchers": [{"@type": "LaunchVehicle"}, {"@type": "LaunchVehicle"}], "satellites": [{"@type": "Satellite"}, {"@type": "Satellite"}], "groundSegment": [{"@type": "GroundNetwork"}, {"@type": "GroundNetwork"}], "groundStations": [{"@type": "GroundStation"}, {"@type": "GroundStation"}]}')
        self.assertIsInstance(o.spaceSegment, list)
        self.assertEqual(len(o.spaceSegment), 2)
        self.assertIsInstance(o.spaceSegment[0], Constellation)
        self.assertIsInstance(o.spaceSegment[1], Constellation)
        self.assertIsInstance(o.launchers, list)
        self.assertEqual(len(o.launchers), 2)
        self.assertIsInstance(o.launchers[0], LaunchVehicle)
        self.assertIsInstance(o.launchers[1], LaunchVehicle)
        self.assertIsInstance(o.satellites, list)
        self.assertEqual(len(o.satellites), 2)
        self.assertIsInstance(o.satellites[0], Satellite)
        self.assertIsInstance(o.satellites[1], Satellite)
        self.assertIsInstance(o.groundSegment, list)
        self.assertEqual(len(o.groundSegment), 2)
        self.assertIsInstance(o.groundSegment[0], GroundNetwork)
        self.assertIsInstance(o.groundSegment[1], GroundNetwork)
        self.assertIsInstance(o.groundStations, list)
        self.assertEqual(len(o.groundStations), 2)
        self.assertIsInstance(o.groundStations[0], GroundStation)
        self.assertIsInstance(o.groundStations[1], GroundStation)
    def test_to_json_basic(self):
        d = json.loads(DesignSpace(spaceSegment=Constellation(), launchers=LaunchVehicle(), satellites=Satellite(), groundSegment=GroundNetwork(), groundStations=GroundStation()).to_json())
        self.assertIsInstance(d.get("spaceSegment"), list)
        self.assertEqual(len(d.get("spaceSegment")), 1)
        self.assertEqual(d.get("spaceSegment")[0].get("@type"), "Constellation")
        self.assertIsInstance(d.get("launchers"), list)
        self.assertEqual(len(d.get("launchers")), 1)
        self.assertEqual(d.get("launchers")[0].get("@type"), "LaunchVehicle")
        self.assertIsInstance(d.get("satellites"), list)
        self.assertEqual(len(d.get("satellites")), 1)
        self.assertEqual(d.get("satellites")[0].get("@type"), "Satellite")
        self.assertIsInstance(d.get("groundSegment"), list)
        self.assertEqual(len(d.get("groundSegment")), 1)
        self.assertEqual(d.get("groundSegment")[0].get("@type"), "GroundNetwork")
        self.assertIsInstance(d.get("groundStations"), list)
        self.assertEqual(len(d.get("groundStations")), 1)
        self.assertEqual(d.get("groundStations")[0].get("@type"), "GroundStation")
    def test_to_json_list(self):
        d = json.loads(DesignSpace(spaceSegment=[Constellation(), Constellation()], launchers=[LaunchVehicle(), LaunchVehicle()], satellites=[Satellite(), Satellite()], groundSegment=[GroundNetwork(), GroundNetwork()], groundStations=[GroundStation(), GroundStation()]).to_json())
        self.assertIsInstance(d.get("spaceSegment"), list)
        self.assertEqual(len(d.get("spaceSegment")), 2)
        self.assertEqual(d.get("spaceSegment")[0].get("@type"), "Constellation")
        self.assertEqual(d.get("spaceSegment")[1].get("@type"), "Constellation")
        self.assertIsInstance(d.get("launchers"), list)
        self.assertEqual(len(d.get("launchers")), 2)
        self.assertEqual(d.get("launchers")[0].get("@type"), "LaunchVehicle")
        self.assertEqual(d.get("launchers")[1].get("@type"), "LaunchVehicle")
        self.assertIsInstance(d.get("satellites"), list)
        self.assertEqual(len(d.get("satellites")), 2)
        self.assertEqual(d.get("satellites")[0].get("@type"), "Satellite")
        self.assertEqual(d.get("satellites")[1].get("@type"), "Satellite")
        self.assertIsInstance(d.get("groundSegment"), list)
        self.assertEqual(len(d.get("groundSegment")), 2)
        self.assertEqual(d.get("groundSegment")[0].get("@type"), "GroundNetwork")
        self.assertEqual(d.get("groundSegment")[1].get("@type"), "GroundNetwork")
        self.assertIsInstance(d.get("groundStations"), list)
        self.assertEqual(len(d.get("groundStations")), 2)
        self.assertEqual(d.get("groundStations")[0].get("@type"), "GroundStation")
        self.assertEqual(d.get("groundStations")[1].get("@type"), "GroundStation")

    class TestArchitecture(unittest.TestCase):
        def test_from_json_basic(self):
            o = Architecture.from_json('{"spaceSegment": {"@type": "Constellation"}, "groundSegment": {"@type": "GroundNetwork"}}')
            self.assertIsInstance(o.spaceSegment, list)
            self.assertEqual(len(o.spaceSegment), 1)
            self.assertIsInstance(o.spaceSegment[0], Constellation)
            self.assertIsInstance(o.groundSegment, list)
            self.assertEqual(len(o.groundSegment), 1)
            self.assertIsInstance(o.groundSegment, GroundNetwork)
        def test_to_json_basic(self):
            d = json.loads(Architecture(spaceSegment=Constellation(), groundSegment=GroundNetwork()))
            self.assertIsInstance(d.get("spaceSegment"), list)
            self.assertEqual(len(d.get("spaceSegment")), 1)
            self.assertEqual(d.get("spaceSegment")[0].get("@type"), "Constellation")
            self.assertIsInstance(d.get("groundSegment"), list)
            self.assertEqual(len(d.get("groundSegment")), 1)
            self.assertEqual(d.get("groundSegment")[0].get("@type"), "GroundNetwork")
