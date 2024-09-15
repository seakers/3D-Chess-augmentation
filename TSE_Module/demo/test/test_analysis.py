#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Unit tests for tatc.analysis module.
"""

import unittest
import json

from tatc import *

class TestTradespaceSearch(unittest.TestCase):
    def test_from_json_basic(self):
        o = TradespaceSearch.from_json('{"mission": {"@type": "MissionConcept"}, "designSpace": {"@type": "DesignSpace"}, "settings": {"@type": "AnalysisSettings"}}')
        self.assertIsInstance(o.mission, MissionConcept)
        self.assertIsInstance(o.designSpace, DesignSpace)
        self.assertIsInstance(o.settings, AnalysisSettings)
    def test_to_json_basic(self):
        d = json.loads(TradespaceSearch(mission=MissionConcept(), designSpace=DesignSpace(), settings=AnalysisSettings()).to_json())
        self.assertEqual(d.get("mission").get("@type"), "MissionConcept")
        self.assertEqual(d.get("designSpace").get("@type"), "DesignSpace")
        self.assertEqual(d.get("settings").get("@type"), "AnalysisSettings")

class TestAnalysisSettings(unittest.TestCase):
    def test_from_json_basic(self):
        o = AnalysisSettings.from_json('{"includePropulsion": true, "searchStrategy": "KDO", "searchParameters": {}}')
        self.assertEqual(o.includePropulsion, True)
        self.assertEqual(o.searchStrategy, SearchStrategy.KDO)
        self.assertIsInstance(o.searchParameters, SearchParameters)
    def test_to_json_basic(self):
        d = json.loads(AnalysisSettings(includePropulsion=True, searchStrategy="KDO", searchParameters=SearchParameters()).to_json())
        self.assertEqual(d.get("includePropulsion"), True)
        self.assertEqual(d.get("searchStrategy"), "KDO")
        self.assertEqual(d.get("searchParameters").get("@type"), "SearchParameters")

class TestSearchParameters(unittest.TestCase):
    pass #TODO
