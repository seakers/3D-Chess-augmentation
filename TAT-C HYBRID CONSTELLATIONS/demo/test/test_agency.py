#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Unit tests for tatc.agency module.
"""

import unittest
import json

from tatc import *

class TestAgencyType(unittest.TestCase):
    def test_keys(self):
        self.assertEqual(AgencyType.get("GOVERNMENT"), AgencyType.GOVERNMENT)
        self.assertEqual(AgencyType.get("government"), AgencyType.GOVERNMENT)
        self.assertEqual(AgencyType.get("commercial"), AgencyType.COMMERCIAL)
        self.assertEqual(AgencyType.get("AcADemiC"), AgencyType.ACADEMIC)
        self.assertEqual(AgencyType.get("test"), None)
        self.assertEqual(AgencyType.get(None), None)

class TestAgency(unittest.TestCase):
    def test_from_json_1(self):
        o = Agency.from_json('{"name": "Agency", "acronym": "A", "agencyType": "GOVERNMENT"}')
        self.assertEqual(o.name, "Agency")
        self.assertEqual(o.acronym, "A")
        self.assertEqual(o.agencyType, AgencyType.GOVERNMENT)
        self.assertIsNone(o._id)
    def test_from_json_2(self):
        o = Agency.from_json('{"name": "Agency", "acronym": "A"}')
        self.assertEqual(o.agencyType, None)
    def test_to_json_1(self):
        d = json.loads(Agency(name="Agency", acronym="A", agencyType=AgencyType.GOVERNMENT).to_json())
        self.assertEqual(d.get("name"), "Agency")
        self.assertEqual(d.get("acronym"), "A")
        self.assertEqual(d.get("agencyType"), "GOVERNMENT")
        self.assertIsNone(d.get("@id"))
    def test_to_json_2(self):
        d = json.loads(Agency(name="Agency", acronym="A").to_json())
        self.assertIsNone(d.get("agencyType"))
