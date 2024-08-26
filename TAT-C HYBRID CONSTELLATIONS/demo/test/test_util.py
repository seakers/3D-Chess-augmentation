#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Unit tests for tatc.util module.
"""

import unittest
import json
from numbers import Number

from tatc import *

class TestEntity(unittest.TestCase):
    def test_eq(self):
        self.assertNotEqual(Entity(), Entity())
        self.assertNotEqual(Entity(), Entity(_id="test"))
        self.assertNotEqual(Entity(_id="test"), Entity())
        self.assertNotEqual(Entity(_id="foo"), Entity(_id="bar"))
        self.assertEqual(Entity(_id="test"), Entity(_id="test"))
    def test_hash(self):
        self.assertNotEqual(hash(Entity(_id="foo")), hash(Entity(_id="bar")))
        self.assertEqual(hash(Entity(_id="test")), hash(Entity(_id="test")))
    def test_to_json(self):
        d = json.loads(Entity().to_json())
        self.assertEqual(d.get("@type"), "Entity")
        self.assertIsNone(d.get("@id"))
    def test_to_json_id(self):
        d = json.loads(Entity(_id="test").to_json())
        self.assertEqual(d.get("@id"), "test")
    def test_from_json(self):
        o = Entity.from_json('{}')
        self.assertEqual(o._type, "Entity")
        self.assertIsNone(o._id)
    def test_from_json_id(self):
        o = Entity.from_json('{"@id": "test"}')
        self.assertEqual(o._id, "test")

class TestCommunicationBnd(unittest.TestCase):
    def test_keys(self):
        self.assertEqual(CommunicationBand.get("X"), CommunicationBand.X)
        self.assertEqual(CommunicationBand.get("KU"), CommunicationBand.KU)
        self.assertEqual(CommunicationBand.get("vhf"), CommunicationBand.VHF)
        self.assertEqual(CommunicationBand.get("UhF"), CommunicationBand.UHF)
        self.assertEqual(CommunicationBand.get("Ka"), CommunicationBand.KA)
        self.assertEqual(CommunicationBand.get("s"), CommunicationBand.S)
        self.assertEqual(CommunicationBand.get("c"), CommunicationBand.C)
        self.assertEqual(CommunicationBand.get("l"), CommunicationBand.L)
        self.assertEqual(CommunicationBand.get("Laser"), CommunicationBand.LASER)
        self.assertIsNone(CommunicationBand.get("missing"), None)
        self.assertEqual(CommunicationBand.get(None), None)
    def test_get_list(self):
        self.assertEqual(sorted(CommunicationBand.get(["X","S"])),
            sorted([CommunicationBand.X, CommunicationBand.S]))

class TestQuantitativeValue(unittest.TestCase):
    def test_from_json(self):
        o = QuantitativeValue.from_json('{"minValue": 1, "maxValue": 10}')
        self.assertEqual(o.minValue, 1)
        self.assertEqual(o.maxValue, 10)
    def test_to_json(self):
        d = json.loads(QuantitativeValue(minValue=1, maxValue=10).to_json())
        self.assertEqual(d.get("minValue"), 1)
        self.assertEqual(d.get("maxValue"), 10)
    def test_from_json_string(self):
        o = QuantitativeValue.from_json('27')
        self.assertEqual(o, 27)
    def test_from_json_primitive(self):
        o = QuantitativeValue.from_json(27)
        self.assertEqual(o, 27)
    def test_iter(self):
        o = QuantitativeValue(minValue=5, maxValue=10)
        self.assertEqual(sorted(list(o)), sorted([5, 10]))

class TestQuantitativeRange(unittest.TestCase):
    def test_from_json_step(self):
        o = QuantitativeRange.from_json('{"minValue": 1, "maxValue": 10, "stepSize": 1}')
        self.assertEqual(o.minValue, 1)
        self.assertEqual(o.maxValue, 10)
        self.assertEqual(o.stepSize, 1)
        self.assertIsNone(o.numberSteps)
    def test_from_json_number(self):
        o = QuantitativeRange.from_json('{"minValue": 1, "maxValue": 10, "numberSteps": 10}')
        self.assertEqual(o.minValue, 1)
        self.assertEqual(o.maxValue, 10)
        self.assertEqual(o.numberSteps, 10)
        self.assertIsNone(o.stepSize)
    def test_to_json_step(self):
        d = json.loads(QuantitativeRange(minValue=1, maxValue=10, stepSize=1).to_json())
        self.assertEqual(d.get("minValue"), 1)
        self.assertEqual(d.get("maxValue"), 10)
        self.assertEqual(d.get("stepSize"), 1)
        self.assertIsNone(d.get("numberSteps"))
    def test_to_json_number(self):
        d = json.loads(QuantitativeRange(minValue=1, maxValue=10, numberSteps=10).to_json())
        self.assertEqual(d.get("minValue"), 1)
        self.assertEqual(d.get("maxValue"), 10)
        self.assertEqual(d.get("numberSteps"), 10)
        self.assertIsNone(d.get("stepSize"))
    def test_from_json_step_string(self):
        o = QuantitativeRange.from_json('{"minValue": 1, "maxValue": 10, "stepSize": 1}')
        self.assertIsInstance(o, QuantitativeRange)
        self.assertNotIsInstance(o, Number)
    def test_from_json_string(self):
        o = QuantitativeRange.from_json('27')
        self.assertEqual(o, 27)
    def test_from_json_primitive(self):
        o = QuantitativeRange.from_json(27)
        self.assertEqual(o, 27)
    def test_iter_integer_steps_size(self):
        o = QuantitativeRange.from_json('{"minValue": 1, "maxValue": 10, "stepSize": 1}')
        self.assertEqual(sorted(list(o)), sorted([1,2,3,4,5,6,7,8,9,10]))
    def test_iter_fractional_steps_size(self):
        o = QuantitativeRange.from_json('{"minValue": 1, "maxValue": 5, "stepSize": 0.5}')
        self.assertEqual(sorted(list(o)), sorted([1,1.5,2,2.5,3,3.5,4,4.5,5]))
    def test_iter_fractional_steps_size_no_end(self):
        o = QuantitativeRange.from_json('{"minValue": 1, "maxValue": 5.25, "stepSize": 0.5}')
        self.assertEqual(sorted(list(o)), sorted([1,1.5,2,2.5,3,3.5,4,4.5,5]))
    def test_iter_integer_steps_number(self):
        o = QuantitativeRange.from_json('{"minValue": 1, "maxValue": 10, "numberSteps": 10}')
        self.assertEqual(sorted(list(o)), sorted([1,2,3,4,5,6,7,8,9,10]))
    def test_iter_fractional_steps_number(self):
        o = QuantitativeRange.from_json('{"minValue": 1, "maxValue": 5, "numberSteps":9}')
        self.assertEqual(sorted(list(o)), sorted([1,1.5,2,2.5,3,3.5,4,4.5,5]))
    def test_iter_end_points(self):
        o = QuantitativeRange.from_json('{"minValue": 1, "maxValue": 5}')
        self.assertEqual(sorted(list(o)), sorted([1,5]))
