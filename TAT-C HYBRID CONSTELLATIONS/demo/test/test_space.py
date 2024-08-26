#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Unit tests for tatc.space module.
"""

import unittest
import json
import isodate
import datetime

from tatc import *

class TestSatellite(unittest.TestCase):
    def test_from_json_basic(self):
        o = Satellite.from_json('{"name": "Landsat 8", "mass": 2750, "dryMass": 2000, "volume": 43.2, "power": 1550, "commBand": ["X"]}')
        self.assertEqual(o.name, "Landsat 8")
        self.assertEqual(o.power, 1550)
        self.assertEqual(o.mass, 2750)
        self.assertEqual(o.dryMass, 2000)
        self.assertEqual(o.volume, 43.2)
        self.assertEqual(o.commBand, ["X"])
        self.assertEqual(o._type, "Satellite")
        self.assertIsNone(o._id)
    def test_from_json_comm_list(self):
        o = Satellite.from_json('{"name": "Landsat 8", "mass": 2750, "volume": 43.2, "power": 1550, "commBand": ["X","S"]}')
        self.assertIsInstance(o.commBand, list)
        self.assertEqual(sorted(o.commBand), sorted(["X","S"]))
    def test_from_json_id(self):
        o = Satellite.from_json('{"@id": "landsat-8", "name": "Landsat 8", "mass": 2750, "volume": 43.2, "power": 1550, "commBand": ["X"]}')
        self.assertEqual(o._id, "landsat-8")
    def test_to_json_basic(self):
        d = json.loads(Satellite(name="Landsat 8", mass=2750, dryMass=2000, volume=43.2, power=1550, commBand=["X"]).to_json())
        self.assertEqual(d.get("name"), "Landsat 8")
        self.assertEqual(d.get("mass"), 2750)
        self.assertEqual(d.get("dryMass"), 2000)
        self.assertEqual(d.get("volume"), 43.2)
        self.assertEqual(d.get("power"), 1550)
        self.assertEqual(d.get("commBand"), ["X"])
        self.assertEqual(d.get("@type"), "Satellite")
        self.assertIsNone(d.get("@id"))
    def test_to_json_comm_list(self):
        d = json.loads(Satellite(name="Landsat 8", mass=2750, volume=43.2, power=1550, commBand=["X","S"]).to_json())
        self.assertIsInstance(d.get("commBand"), list)
        self.assertEqual(sorted(d.get("commBand")), sorted(["X","S"]))
    def test_to_json_id(self):
        d = json.loads(Satellite(_id="landsat-8", name="Landsat 8", mass=2750, volume=43.2, power=1550, commBand=["X"]).to_json())
        self.assertEqual(d.get("@id"), "landsat-8")
    def test_from_json_architecture(self):
        o = Satellite.from_json('{"orbit": {"@type": "Orbit"}, "payload": {"@type": "Basic Sensor"}}')
        self.assertIsInstance(o.orbit, Orbit)
        self.assertIsInstance(o.payload, Instrument)
    def test_from_json_architecture_list(self):
        o = Satellite.from_json('{"orbit": {"@type": "Orbit"}, "payload": [{"@type": "Basic Sensor"}, {"@type": "Basic Sensor"}]}')
        self.assertIsInstance(o.orbit, Orbit)
        self.assertIsInstance(o.payload, list)
        self.assertEqual(len(o.payload), 2)
        self.assertIsInstance(o.payload[0], Instrument)
        self.assertIsInstance(o.payload[1], Instrument)
    def test_to_json_architecture(self):
        d = json.loads(Satellite(orbit=Orbit(), payload=Instrument()).to_json())
        self.assertEqual(d.get("orbit").get("@type"), "Orbit")
        self.assertEqual(d.get("payload").get("@type"), "Basic Sensor")
    def test_to_json_architecture_list(self):
        d = json.loads(Satellite(orbit=Orbit(), payload=[Instrument(), Instrument()]).to_json())
        self.assertEqual(d.get("orbit").get("@type"), "Orbit")
        self.assertIsInstance(d.get("payload"), list)
        self.assertEqual(len(d.get("payload")), 2)
        self.assertEqual(d.get("payload")[0].get("@type"), "Basic Sensor")
        self.assertEqual(d.get("payload")[1].get("@type"), "Basic Sensor")

class TestOrbitType(unittest.TestCase):
    def test_keys(self):
        self.assertEqual(OrbitType.get("KEPLERIAN"), OrbitType.KEPLERIAN)
        self.assertEqual(OrbitType.get("circular"), OrbitType.CIRCULAR)
        self.assertEqual(OrbitType.get("SUN_SYNCHRONOUS"), OrbitType.SUN_SYNCHRONOUS)
        self.assertEqual(OrbitType.get("test"), None)
        self.assertEqual(OrbitType.get(None), None)

class TestOrbitSunSynchronous(unittest.TestCase):
    def test_from_json(self):
        o = Orbit.from_json('{"orbitType": "sun_synchronous", "altitude": 500, "localSolarTimeAscendingNode": "10:30"}')
        self.assertEqual(o.orbitType, OrbitType.SUN_SYNCHRONOUS)
        self.assertEqual(o.altitude, 500)
        self.assertEqual(o.localSolarTimeAscendingNode, "10:30")
        self.assertEqual(o.inclination, Orbit.get_sso_inclination(500))
        self.assertEqual(o.eccentricity, 0.0)
        self.assertIsNone(o.epoch)
        self.assertIsNone(o.semimajorAxis)
        self.assertIsNone(o.periapsisArgument)
        self.assertIsNone(o.rightAscensionAscendingNode)
        self.assertIsNone(o.trueAnomaly)
        self.assertIsNone(o._id)
    def test_to_json(self):
        d = json.loads(Orbit(orbitType="sun_synchronous", altitude=500, localSolarTimeAscendingNode="10:30").to_json())
        self.assertEqual(d.get("orbitType"), "SUN_SYNCHRONOUS")
        self.assertEqual(d.get("altitude"), 500)
        self.assertEqual(d.get("localSolarTimeAscendingNode"), "10:30")
        self.assertEqual(d.get("inclination"), Orbit.get_sso_inclination(500))
        self.assertEqual(d.get("eccentricity"), 0.0)
        self.assertIsNone(d.get("epoch"))
        self.assertIsNone(d.get("semimajorAxis"))
        self.assertIsNone(d.get("periapsisArgument"))
        self.assertIsNone(d.get("rightAscensionAscendingNode"))
        self.assertIsNone(d.get("trueAnomaly"))
        self.assertIsNone(d.get("@id"))
    def test_iter_altitude(self):
        o = Orbit(altitude=QuantitativeRange(405,505,stepSize=25),inclination="SSO")
        self.assertEqual(len(list(o)), 5)
        for i in range(5):
            self.assertIsInstance(list(o)[i], Orbit)
        self.assertEqual(sorted([i.altitude for i in list(o)]), sorted([405,430,455,480,505]))
        self.assertEqual(sorted([i.inclination for i in list(o)]), sorted([Orbit.get_sso_inclination(a) for a in [405,430,455,480,505]]))

class TestOrbitKeplerian(unittest.TestCase):
    def test_from_json(self):
        o = Orbit.from_json('{"orbitType": "keplerian", "epoch": "2017-08-01T00:00:00Z", "semimajorAxis": 7083.14, "eccentricity": 0, "inclination": 98.20807124037059, "periapsisArgument": 0, "rightAscensionAscendingNode": 0.0, "trueAnomaly": 0.0 }')
        self.assertEqual(o.orbitType, OrbitType.KEPLERIAN)
        self.assertEqual(o.epoch, "2017-08-01T00:00:00Z")
        self.assertEqual(o.semimajorAxis, 7083.14)
        self.assertEqual(o.eccentricity, 0)
        self.assertEqual(o.inclination, 98.20807124037059)
        self.assertEqual(o.periapsisArgument, 0)
        self.assertEqual(o.rightAscensionAscendingNode, 0.0)
        self.assertEqual(o.trueAnomaly, 0.0)
        self.assertIsNone(o.altitude)
        self.assertIsNone(o.localSolarTimeAscendingNode)
        self.assertIsNone(o._id)
    def test_to_json(self):
        d = json.loads(Orbit(orbitType="keplerian", epoch="2017-08-01T00:00:00Z", semimajorAxis=7083.14, eccentricity=0, inclination=98.20807124037059, periapsisArgument=0, rightAscensionAscendingNode=0.0, trueAnomaly=0.0).to_json())
        self.assertEqual(d.get("orbitType"), "KEPLERIAN")
        self.assertEqual(d.get("epoch"), "2017-08-01T00:00:00Z")
        self.assertEqual(d.get("semimajorAxis"), 7083.14)
        self.assertEqual(d.get("eccentricity"), 0)
        self.assertEqual(d.get("inclination"), 98.20807124037059)
        self.assertEqual(d.get("periapsisArgument"), 0)
        self.assertEqual(d.get("rightAscensionAscendingNode"), 0.0)
        self.assertEqual(d.get("trueAnomaly"), 0.0)
        self.assertIsNone(d.get("altitude"))
        self.assertIsNone(d.get("localSolarTimeAscendingNode"))
        self.assertIsNone(d.get("@id"))

class TestOrbitCircular(unittest.TestCase):
    def test_from_json_basic(self):
        o = Orbit.from_json('{"@id": "iss", "orbitType": "circular", "altitude": 405, "inclination": 51.64}')
        self.assertEqual(o._id, "iss")
        self.assertEqual(o.orbitType, OrbitType.CIRCULAR)
        self.assertEqual(o.altitude, 405)
        self.assertEqual(o.inclination, 51.64)
        self.assertEqual(o.eccentricity, 0.0)
        self.assertIsNone(o.epoch)
        self.assertIsNone(o.semimajorAxis)
        self.assertIsNone(o.periapsisArgument)
        self.assertIsNone(o.rightAscensionAscendingNode)
        self.assertIsNone(o.trueAnomaly)
    def test_from_json_altitude_range(self):
        o = Orbit.from_json('{"@id": "iss", "orbitType": "circular", "altitude": {"minValue": 405, "maxValue": 505, "stepSize": 25}, "inclination": 51.64}')
        self.assertIsInstance(o.altitude, QuantitativeRange)
        self.assertEqual(o.altitude.minValue, 405)
        self.assertEqual(o.altitude.maxValue, 505)
        self.assertEqual(o.altitude.stepSize, 25)
    def test_from_json_inclination_range(self):
        o = Orbit.from_json('{"@id": "iss", "orbitType": "circular", "altitude": {"minValue": 405, "maxValue": 505, "stepSize": 25}, "inclination": {"minValue": 50, "maxValue": 60, "numberSteps": 11}}')
        self.assertIsInstance(o.inclination, QuantitativeRange)
        self.assertEqual(o.inclination.minValue, 50)
        self.assertEqual(o.inclination.maxValue, 60)
        self.assertEqual(o.inclination.numberSteps, 11)
    def test_to_json_basic(self):
        d = json.loads(Orbit(_id="iss", orbitType="circular", altitude=500, inclination=51.64).to_json())
        self.assertEqual(d.get("@id"), "iss")
        self.assertEqual(d.get("orbitType"), "CIRCULAR")
        self.assertEqual(d.get("altitude"), 500)
        self.assertEqual(d.get("inclination"), 51.64)
        self.assertEqual(d.get("eccentricity"), 0.0)
        self.assertIsNone(d.get("epoch"))
        self.assertIsNone(d.get("semimajorAxis"))
        self.assertIsNone(d.get("periapsisArgument"))
        self.assertIsNone(d.get("rightAscensionAscendingNode"))
        self.assertIsNone(d.get("trueAnomaly"))
    def test_to_json_altitude_range(self):
        d = json.loads(Orbit(_id="iss", orbitType="circular", altitude=QuantitativeRange(minValue=405,maxValue=505,stepSize=25), inclination=51.64).to_json())
        self.assertEqual(d.get("@id"), "iss")
        self.assertEqual(d.get("orbitType"), "CIRCULAR")
        self.assertEqual(d.get("altitude").get("@type"), "QuantitativeRange")
        self.assertEqual(d.get("altitude").get("minValue"), 405)
        self.assertEqual(d.get("altitude").get("maxValue"), 505)
        self.assertEqual(d.get("altitude").get("stepSize"), 25)
        self.assertEqual(d.get("inclination"), 51.64)
        self.assertEqual(d.get("eccentricity"), 0.0)
        self.assertIsNone(d.get("epoch"))
        self.assertIsNone(d.get("semimajorAxis"))
        self.assertIsNone(d.get("periapsisArgument"))
        self.assertIsNone(d.get("rightAscensionAscendingNode"))
        self.assertIsNone(d.get("trueAnomaly"))
    def test_iter_altitude(self):
        o = Orbit(altitude=QuantitativeRange(405,505,stepSize=25),inclination=51.6)
        self.assertEqual(len(list(o)), 5)
        for i in range(5):
            self.assertIsInstance(list(o)[i], Orbit)
        self.assertEqual(sorted([i.altitude for i in list(o)]), sorted([405,430,455,480,505]))
        self.assertEqual(sorted([i.inclination for i in list(o)]), sorted([51.6,51.6,51.6,51.6,51.6]))
    def test_iter_product(self):
        o = Orbit(altitude=QuantitativeRange(405,505,stepSize=50),inclination=QuantitativeRange(50,55))
        self.assertEqual(len(list(o)), 3*2)
        for i in range(3*2):
            self.assertIsInstance(list(o)[i], Orbit)

class TestConstellationDeltaHomogeneous(unittest.TestCase):
    def test_from_json_basic(self):
        o = Constellation.from_json('{"constellationType": "DELTA_HOMOGENEOUS", "numberSatellites": 2, "numberPlanes": 2, "orbit": {"orbitType": "circular", "altitude": 405, "inclination": 51.64}}')
        self.assertEqual(o.constellationType, ConstellationType.DELTA_HOMOGENEOUS)
        self.assertEqual(o.numberSatellites, 2)
        self.assertEqual(o.numberPlanes, 2)
        self.assertIsInstance(o.orbit, Orbit)
        self.assertEqual(o.orbit.altitude, 405)
        self.assertEqual(o.orbit.inclination, 51.64)
        self.assertEqual(o.relativeSpacing, 0)
        self.assertIsNone(o.satelliteInterval)
        self.assertIsNone(o._id)
    def test_from_json_range(self):
        o = Constellation.from_json('{"constellationType": "DELTA_HOMOGENEOUS", "numberSatellites": {"minValue": 1, "maxValue": 2}, "numberPlanes": {"minValue": 1, "maxValue": 2}, "orbit": {"altitude": 405, "inclination": 51.64}}')
        self.assertIsInstance(o.numberSatellites, QuantitativeRange)
        self.assertEqual(o.numberSatellites.minValue, 1)
        self.assertEqual(o.numberSatellites.maxValue, 2)
        self.assertIsNone(o.numberSatellites.stepSize)
        self.assertIsNone(o.numberSatellites.numberSteps)
        self.assertIsInstance(o.numberPlanes, QuantitativeRange)
        self.assertEqual(o.numberPlanes.minValue, 1)
        self.assertEqual(o.numberPlanes.maxValue, 2)
    def test_from_json_list_range(self):
        o = Constellation.from_json('{"constellationType": "DELTA_HOMOGENEOUS", "numberSatellites": [1, 2, 3], "numberPlanes": [1, 2], "orbit": {"altitude": 405, "inclination": 51.64}}')
        self.assertEqual(sorted(o.numberSatellites), sorted([1, 2, 3]))
        self.assertEqual(sorted(o.numberPlanes), sorted([1, 2]))
    def test_from_json_orbit_range(self):
        o = Constellation.from_json('{"constellationType": "DELTA_HOMOGENEOUS", "numberSatellites": 2, "numberPlanes": 2, "orbit": [{"orbitType": "circular", "altitude": 405, "inclination": 51.64},{"orbitType": "sun_synchronous", "altitude": 500}]}')
        self.assertIsInstance(o.orbit, list)
        self.assertEqual(len(o.orbit), 2)
        self.assertIsInstance(o.orbit[0], Orbit)
        self.assertEqual(o.orbit[0].orbitType, OrbitType.CIRCULAR)
        self.assertIsInstance(o.orbit[1], Orbit)
        self.assertEqual(o.orbit[1].orbitType, OrbitType.SUN_SYNCHRONOUS)
    def test_from_json_iso8601_duration(self):
        o = Constellation.from_json('{"constellationType": "TRAIN", "satelliteInterval": "P0Y0M0DT0H15M"}')
        self.assertEqual(o.constellationType, ConstellationType.TRAIN)
        self.assertEqual(isodate.parse_duration(o.satelliteInterval), isodate.parse_duration("P0Y0M0DT0H15M"))
    def test_from_json_numeric_duration(self):
        o = Constellation.from_json('{"constellationType": "TRAIN", "satelliteInterval": 15}')
        self.assertEqual(isodate.parse_duration(o.satelliteInterval), datetime.timedelta(minutes=15))
    def test_to_json(self):
        d = json.loads(Constellation(constellationType="DELTA_HOMOGENEOUS", numberSatellites=2, numberPlanes=2, orbit=Orbit(orbitType="circular", altitude=405, inclination=51.64)).to_json())
        self.assertEqual(d.get("constellationType"), "DELTA_HOMOGENEOUS")
        self.assertEqual(d.get("numberSatellites"), 2)
        self.assertEqual(d.get("numberPlanes"), 2)
        self.assertEqual(d.get("orbit").get("@type"), "Orbit")
        self.assertEqual(d.get("orbit").get("orbitType"), "CIRCULAR")
        self.assertEqual(d.get("orbit").get("altitude"), 405)
        self.assertEqual(d.get("orbit").get("inclination"), 51.64)
        self.assertEqual(d.get("relativeSpacing"), 0)
        self.assertIsNone(d.get("satelliteInterval"))
        self.assertIsNone(d.get("@id"))
    def test_to_json_iso8601_duration(self):
        d = json.loads(Constellation(constellationType="TRAIN", satelliteInterval="P0Y0M0DT0H15M").to_json())
        self.assertEqual(d.get("constellationType"), "TRAIN")
        self.assertEqual(isodate.parse_duration(d.get("satelliteInterval")), isodate.parse_duration("P0Y0M0DT0H15M"))
    def test_to_json_numeric_duration(self):
        d = json.loads(Constellation(constellationType="TRAIN", satelliteInterval=15).to_json())
        self.assertEqual(d.get("constellationType"), "TRAIN")
        self.assertEqual(isodate.parse_duration(d.get("satelliteInterval")), datetime.timedelta(minutes=15))
    def test_iter_satellites(self):
        o = Constellation(numberSatellites=[1,2])
        self.assertEqual(len(list(o)), 2)
        for i in range(2):
            self.assertIsInstance(list(o)[i], Constellation)
        self.assertEqual(sorted([i.numberSatellites for i in o]), sorted([1,2]))
    def test_iter_number_planes(self):
        o = Constellation(numberSatellites=[1,2], numberPlanes=[1,2])
        self.assertEqual(len(list(o)), 3)
        for i in range(3):
            self.assertIsInstance(list(o)[i], Constellation)
    def test_iter_relative_phasing(self):
        o = Constellation(numberSatellites=[1,2], numberPlanes=[1,2], relativeSpacing=[0,1])
        self.assertEqual(len(list(o)), 4)
        for i in range(4):
            self.assertIsInstance(list(o)[i], Constellation)
