#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Object models and methods for managing space system instruments.
"""

import json
from numbers import Number

from .util import Entity, EnumEntity
from .agency import Agency

class MountType(EnumEntity):
    """Enumeration of recognized instrument mount types.

    :var BODY: mounted to the spacecraft body.
    :vartype BODY: MountType
    :var MAST: mounted to a mast.
    :vartype MAST: MountType
    :var PROBE: mounted to a probe.
    :vartype PROBE: MountType
    """
    BODY = "BODY"
    MAST = "MAST"
    PROBE = "PROBE"

class OrientationConvention(EnumEntity):
    """Enumeration of recognized instrument orientation conventions.

    :var XYZ: orientation based on rotations about X, Y, and Z axes.
    :vartype XYZ: OrientationConvention
    :var SIDE_LOOK: orientation based on a side look angle.
    :vartype SIDE_LOOK: OrientationConvention
    """
    XYZ = "XYZ"
    SIDE_LOOK = "SIDE_LOOK"

class Orientation(Entity):
    """Orientation of the instrument with respect to the satellite body frame.

    :param convention: Convention used to specify the orientation. Recognized \
    values include: XYZ (x-axis, y-axis, z-axis rotation; default), \
    SIDE_LOOK (only specify side look (y-axis) angle).
    :type convention: str or OrientationConvention
    :param xRotation: Rotation angle (deg) about x-axis (default: 0).
    :type xRotation: float
    :param yRotation: Rotation angle (deg) about y-axis (default: 0).
    :type yRotation: float
    :param zRotation: Rotation angle (deg) about z-axis (default: 0).
    :type zRotation: float
    :param sideLookAngle: Rotation angle (deg) about spacecraft side (y-axis).
    :type sideLookAngle: float
    """
    def __init__(self, convention="XYZ", xRotation=None, yRotation=None,
            zRotation=None, sideLookAngle=None, _id=None):
        self.convention = OrientationConvention.get(convention)
        self.xRotation = xRotation if xRotation is not None else (0 if convention == "XYZ" else None)
        self.yRotation = yRotation if yRotation is not None else (0 if convention == "XYZ" else None)
        self.zRotation = zRotation if zRotation is not None else (0 if convention == "XYZ" else None)
        self.sideLookAngle = sideLookAngle
        super(Orientation,self).__init__(_id, "Orientation")

    @staticmethod
    def from_dict(d):
        """Parses an orientation from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the orientation
        :rtype: Orientation
        """
        return Orientation(
            convention = d.get("convention", "XYZ"),
            xRotation = d.get("xRotation ", None),
            yRotation = d.get("yRotation ", None),
            zRotation = d.get("zRotation ", None),
            sideLookAngle = d.get("sideLookAngle", None),
            _id = d.get("@id", None)
        )

class SensorGeometry(EnumEntity):
    """Enumeration of recognized instrument sensor geometries.

    :var CONICAL: conical shape
    :vartype CONICAL: SensorGeometry
    :var RECTANGULAR: rectangular shape
    :vartype RECTANGULAR: SensorGeometry
    :var CUSTOM: custom shape
    :vartype CUSTOM: SensorGeometry
    """
    CONICAL = "CONICAL",
    RECTANGULAR = "RECTANGULAR",
    CUSTOM = "CUSTOM"

class FieldOfView(Entity):
    """Field of view provided by an instrument.

    :param sensorGeometry: Specification of sensor geometry. Recongized values \
    include: CONICAL, RECTANGULAR (default), CUSTOM.
    :type sensorGeometry: str or SensorGeometry
    :param fullConeAngle: Angle (deg) of full FoV cone (for CONE geometry).
    :type fullConeAngle: float
    :param alongTrackFieldOfView: Angle (deg) in along-track direction.
    :type alongTrackFieldOfView: float
    :param crossTrackFieldOfView: Angle (deg) in cross-track direction.
    :type crossTrackFieldOfView: float
    :param customConeAnglesVector: List of numeric values explaining the field \
     of view angles (deg) at various angular positions defined in \
     `customClockAnglesVector`.
    :type customConeAnglesVector: list[float]
    :param customClockAnglesVector: List of numeric values explaining the \
    angular positions (deg) at which field of view angles in \
    `customConeAnglesVector` are defined.
    :type customClockAnglesVector: list[float]
    """
    def __init__(self, sensorGeometry="CONICAL", fullConeAngle=30,
            alongTrackFieldOfView=None, crossTrackFieldOfView=None,
            customConeAnglesVector=None, customClockAnglesVector=None,
            _id=None):
        self.sensorGeometry = SensorGeometry.get(sensorGeometry)
        self.fullConeAngle = fullConeAngle if sensorGeometry == SensorGeometry.CONICAL else None
        self.alongTrackFieldOfView = alongTrackFieldOfView if sensorGeometry == SensorGeometry.RECTANGULAR else None
        self.crossTrackFieldOfView = crossTrackFieldOfView if sensorGeometry == SensorGeometry.RECTANGULAR else None
        self.customConeAnglesVector = customConeAnglesVector if sensorGeometry == SensorGeometry.CUSTOM else None
        self.customClockAnglesVector = customClockAnglesVector if sensorGeometry == SensorGeometry.CUSTOM else None
        super(FieldOfView,self).__init__(_id, "FieldOfView")

    @staticmethod
    def from_dict(d):
        """Parses a field of view from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the field of view
        :rtype: FieldOfView
        """
        if isinstance(d, Number):
            # if d is a number, interpret as conical sensor with full cone angle
            return FieldOfView(
                sensorGeometry = "CONICAL",
                fullConeAngle = d
            )
        if (isinstance(d, list) and len(d) == 2
                and isinstance(d[0], Number) and isinstance(d[1], Number)):
            # if d is a numeric list of size 2, interpret as rectangular sensor
            # with cross-track x along-track angles
            return FieldOfView(
                sensorGeometry = "RECTANGULAR",
                alongTrackFieldOfView = d[1],
                crossTrackFieldOfView = d[0],
            )
        return FieldOfView(
            sensorGeometry = d.get("sensorGeometry", "CONICAL"),
            fullConeAngle = d.get("fullConeAngle", 30),
            alongTrackFieldOfView = d.get("alongTrackFieldOfView", None),
            crossTrackFieldOfView = d.get("crossTrackFieldOfView", None),
            customConeAnglesVector = d.get("customConeAnglesVector", None),
            customClockAnglesVector = d.get("customClockAnglesVector", None),
            _id = d.get("@id", None)
        )

class Instrument(Entity):
    """A payload component that performs scientific observation functions.

    :param name: Full name of this entity.
    :type name: str
    :param acronym: Acronym, initialism, or abbreviation.
    :type acronym: str
    :param agency: Designer, provider, or operator of this entity.
    :type agency: Agency
    :param mass: Total mass (kg) of this entity including any consumable \
    propellants or gases.
    :type mass: float
    :param volume: Total volume (m3) of this entity.
    :type volume: float
    :param power: Nominal operating power (W).
    :type power: float
    :param orientation: Instrument orientation with respect to the satellite \
    body frame.
    :type orientation: str or OrientationConvention
    :param fieldOfView: Instrument field of view.
    :type fieldOfView: float or FieldOfView
    :param dataRate: Rate of data recorded (Mbps) during nominal operations.
    :type dataRate: float
    :ivar bitsPerPixel: Bits encoded per pixel of image
    :vartype bitsPerPixel: int
    :param techReadinessLevel: Instrument technology readiness level.
    :type techReadinessLevel: int
    :param mountType: Type of mounting. Recognized values include: \
    BODY (default), MAST, PROBE.
    :type mountType: str or MountType
    """

    def __init__(self, name=None, acronym=None, agency=None, mass=None,
            volume=None, power=None, orientation=Orientation(),
            fieldOfView=FieldOfView(), dataRate=None, bitsPerPixel = None, techReadinessLevel=9,
            mountType="BODY", _id=None, _type="Basic Sensor"):
        self.name = name
        self.acronym = acronym if acronym else name
        self.agency = agency
        self.mass = mass
        self.volume = volume
        self.power = power
        self.orientation = orientation
        self.fieldOfView = fieldOfView
        self.dataRate = dataRate
        self.bitsPerPixel = bitsPerPixel
        self.techReadinessLevel = techReadinessLevel
        self.mountType = MountType.get(mountType)
        super(Instrument,self).__init__(_id, _type)

    @staticmethod
    def from_dict(d):
        """Parses an instrument from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the instrument
        :rtype: Instrument
        """
        type = d.get("@type", "Basic Sensor")
        if type == "Basic Sensor":
            return Instrument(
                    name = d.get("name", None),
                    acronym = d.get("acronym", None),
                    agency = Agency.from_json(d.get("agency", None)),
                    mass = d.get("mass", None),
                    volume = d.get("volume", None),
                    power = d.get("power", None),
                    orientation = Orientation.from_json(d.get("orientation", Orientation())),
                    fieldOfView = FieldOfView.from_json(d.get("fieldOfView", FieldOfView())),
                    dataRate = d.get("dataRate", None),
                    bitsPerPixel = d.get("bitsPerPixel", None),
                    techReadinessLevel = d.get("techReadinessLevel", 9),
                    mountType = d.get("mountType", "BODY"),
                    _id = d.get("@id", None)
                )
        elif type == "Passive Optical Scanner":
            return OpticalScanner(
                    name = d.get("name", None),
                    acronym = d.get("acronym", None),
                    agency = Agency.from_json(d.get("agency", None)),
                    mass = d.get("mass", None),
                    volume = d.get("volume", None),
                    power = d.get("power", None),
                    orientation = Orientation.from_json(d.get("orientation", Orientation())),
                    fieldOfView = FieldOfView.from_json(d.get("fieldOfView", FieldOfView())),
                    dataRate = d.get("dataRate", None),
                    scanTechnique = d.get("scanTechnique", None),
                    numberOfDetectorsRowsAlongTrack = d.get("numberOfDetectorsRowsAlongTrack", None),
                    numberOfDetectorsColsCrossTrack = d.get("numberOfDetectorsColsCrossTrack", None),
                    Fnum = d.get("Fnum", None),
                    focalLength = d.get("focalLength", None),
                    apertureDia = d.get("apertureDia", None),
                    operatingWavelength = d.get("operatingWavelength", None),
                    bandwidth = d.get("bandwidth", None),
                    opticsSysEff = d.get("opticsSysEff", 0.8),
                    quantumEff = d.get("quantumEff", None),
                    numOfReadOutE = d.get("numOfReadOutE", None),
                    targetBlackBodyTemp = d.get("targetBlackBodyTemp", None),
                    bitsPerPixel = d.get("bitsPerPixel", None),
                    detectorWidth = d.get("detectorWidth", None),
                    maxDetectorExposureTime = d.get("maxDetectorExposureTime", None),
                    snrThreshold = d.get("snrThreshold", None),
                    techReadinessLevel = d.get("techReadinessLevel", 9),
                    mountType = d.get("mountType", "BODY"),
                    _id = d.get("@id", None)
                )
        elif type == "Synthetic Aperture Radar":
            return SyntheticApertureRadar(
                    name = d.get("name", None),
                    acronym = d.get("acronym", None),
                    agency = Agency.from_json(d.get("agency", None)),
                    mass = d.get("mass", None),
                    volume = d.get("volume", None),
                    power = d.get("power", None),
                    orientation = Orientation.from_json(d.get("orientation", Orientation())),
                    dataRate = d.get("dataRate", None),
                    bitsPerPixel = d.get("bitsPerPixel", None),
                    pulseWidth = d.get("pulseWidth", None),
                    antennaDimensionAlongTrack = d.get("antennaDimensionAlongTrack", None),
                    antennaDimensionCrossTrack = d.get("antennaDimensionCrossTrack", None),
                    antennaApertureEfficiency = d.get("antennaApertureEfficiency", None),
                    operatingFrequency = d.get("operatingFrequency", None),
                    peakTransmitPower = d.get("peakTransmitPower", None),
                    chirpBandwidth = d.get("chirpBandwidth", None),
                    minPulseRepetitionFrequency = d.get("minPulseRepetitionFrequency", None),
                    maxPulseRepetitionFrequency = d.get("maxPulseRepetitionFrequency", None),
                    sceneNoiseTemp = d.get("sceneNoiseTemp", None),
                    systemNoiseFigure = d.get("systemNoiseFigure", None),
                    radarLosses = d.get("radarLosses", None),
                    thresholdSigmaNEZ0 = d.get("thresholdSigmaNEZ0", None),
                    techReadinessLevel = d.get("techReadinessLevel", 9),
                    mountType = d.get("mountType", "BODY"),
                    _id = d.get("@id", None)
                )

class ScanTechnique(EnumEntity):
    """Scanning technique used to compose images.

    :var PUSHBROOM: push-broom
    :vartype PUSHBROOM: ScanTechnique
    :var WHISKBROOM: whisk-broom
    :vartype WHISKBROOM: ScanTechnique
    :var MATRIX_IMAGER: Matrix Imager
    :vartype MATRIX_IMAGER: ScanTechnique
    """
    PUSHBROOM = "PUSHBROOM"
    WHISKBROOM = "WHISKBROOM"
    MATRIX_IMAGER = "MATRIX_IMAGER"

class OpticalScanner(Instrument):
    """An optical scanner type instrument.

    :param name: Full name of this entity.
    :type name: str
    :param acronym: Acronym, initialism, or abbreviation.
    :type acronym: str
    :param agency: Designer, provider, or operator of this entity.
    :type agency: Agency
    :param mass: Total mass (kg) of this entity including any consumable \
    propellants or gases.
    :type mass: float
    :param volume: Total volume (m3) of this entity.
    :type volume: float
    :param power: Nominal operating power (W).
    :type power: float
    :param orientation: Instrument orientation with respect to the satellite \
    body frame.
    :type orientation: str or OrientationConvention
    :param fieldOfView: Instrument field of view.
    :type fieldOfView: float or FieldOfView
    :param dataRate: Rate of data recorded (Mbps) during nominal operations.
    :type dataRate: float
    :param scanTechnique: Scanning technique used to compose images.
    :ivar bitsPerPixel: Bits encoded per pixel of image
    :vartype bitsPerPixel: int
    :type scanTechnique: str or ScanTechnique
    :param numberOfDetectorsRowsAlongTrack: Number of detectors in the along-track direction.
    :type numberOfDetectorsRowsAlongTrack: int
    :param numberOfDetectorsColsCrossTrack: Number of detectors in the cross-track direction.
    :type numberOfDetectorsColsCrossTrack: int
    :param Fnum: F-number.
    :type Fnum: float
    :param focalLength: Focal length.
    :type focalLength: float
    :param apertureDia: Aperture diameter (meters).
    :type apertureDia: float
    :param operatingWavelength: Operating wavelength.
    :type operatingWavelength: float
    :param bandwidth: Bandwidth.
    :type bandwidth: float
    :param opticsSysEff: Optical systems efficiency between 0 and 1 (default: 0.8).
    :type opticsSysEff: float
    :param quantumEff: Quantum efficiency.
    :type quantumEff: float
    :param numOfReadOutE: Number of read-out electrons.
    :type numOfReadOutE: int
    :param targetBlackBodyTemp: Target black body temperature.
    :type targetBlackBodyTemp: int
    :param bitsPerPixel: Number of bits per pixel.
    :type bitsPerPixel: int
    :param detectorWidth: Width of the detector.
    :type detectorWidth: float
    :param maxDetectorExposureTime: Maximum detector exposure time.
    :type maxDetectorExposureTime: float
    :param snrThreshold: Threshold signal-to-noise ratio for valid observation.
    :type snrThreshold: float
    :param techReadinessLevel: Instrument technology readiness level.
    :type techReadinessLevel: int
    :param mountType: Type of mounting. Recognized values include: \
    BODY (default), MAST, PROBE.
    :type mountType: str or MountType
    """
    def __init__(self, name=None, acronym=None, agency=None, mass=None,
            volume=None, power=None, orientation=Orientation(),
            fieldOfView=FieldOfView(), dataRate=None, scanTechnique=None,
            numberOfDetectorsRowsAlongTrack=None, numberOfDetectorsColsCrossTrack=None,
            Fnum=None, focalLength=None, apertureDia=None,
            operatingWavelength=None, bandwidth=None, opticsSysEff = 0.8,
            quantumEff=None, numOfReadOutE=None, targetBlackBodyTemp=None, bitsPerPixel=None,
            detectorWidth=None, maxDetectorExposureTime= None, snrThreshold=None,
            techReadinessLevel=9, mountType="BODY", _id=None):
        self.scanTechnique = ScanTechnique.get(scanTechnique)
        self.numberOfDetectorsRowsAlongTrack = numberOfDetectorsRowsAlongTrack
        self.numberOfDetectorsColsCrossTrack = numberOfDetectorsColsCrossTrack
        self.Fnum = Fnum
        self.focalLength = focalLength
        self.apertureDia = apertureDia
        self.operatingWavelength = operatingWavelength
        self.bandwidth = bandwidth
        self.opticsSysEff = opticsSysEff
        self.quantumEff = quantumEff
        self.numOfReadOutE = numOfReadOutE
        self.targetBlackBodyTemp = targetBlackBodyTemp
        self.detectorWidth = detectorWidth
        self.maxDetectorExposureTime = maxDetectorExposureTime
        self.snrThreshold = snrThreshold
        super(OpticalScanner,self).__init__(name=name, acronym=acronym,
                agency=agency, mass=mass, volume=volume, power=power,
                orientation=orientation, fieldOfView=fieldOfView,
                dataRate=dataRate, bitsPerPixel = bitsPerPixel, techReadinessLevel=techReadinessLevel,
                mountType=mountType, _id=_id, _type="Passive Optical Scanner")

class SyntheticApertureRadar(Instrument):
    """A synthetic aperture radar (SAR) type instrument.

    :param name: Full name of this entity.
    :type name: str
    :param acronym: Acronym, initialism, or abbreviation.
    :type acronym: str
    :param agency: Designer, provider, or operator of this entity.
    :type agency: Agency
    :param mass: Total mass (kg) of this entity including any consumable \
    propellants or gases.
    :type mass: float
    :param volume: Total volume (m3) of this entity.
    :type volume: float
    :param power: Nominal operating power (W).
    :type power: float
    :param orientation: Instrument orientation with respect to the satellite \
    body frame.
    :type orientation: str or OrientationConvention
    :param dataRate: Rate of data recorded (Mbps) during nominal operations.
    :type dataRate: float
    :ivar bitsPerPixel: Bits encoded per pixel of image
    :vartype bitsPerPixel: int
    :param pulseWidth: Pulse width.
    :type pulseWidth: float
    :param antennaDimensionAlongTrack: Antenna dimension in the along-track direction.
    :type antennaDimensionAlongTrack: float
    :param antennaDimensionCrossTrack: Antenna dimension in the cross-track direction.
    :type antennaDimensionCrossTrack: float
    :param antennaApertureEfficiency: Antenna aperture efficiency.
    :type antennaApertureEfficiency: float
    :param operatingFrequency: Operating frequency.
    :type operatingFrequency: float
    :param peakTransmitPower: Peak transmit power.
    :type peakTransmitPower: float
    :param chirpBandwidth: Chirp bandwidth.
    :type chirpBandwidth: float
    :param minPulseRepetitionFrequency: Minimum pulse repetition frequency.
    :type minPulseRepetitionFrequency: float
    :param maxPulseRepetitionFrequency: Maximum pulse repetition frequency.
    :type maxPulseRepetitionFrequency: float
    :param sceneNoiseTemp: Scene noise temperature.
    :type sceneNoiseTemp: float
    :param systemNoiseFigure: System noise figure.
    :type systemNoiseFigure: float
    :param radarLosses: Radar losses.
    :type radarLosses: float
    :param thresholdSigmaNEZ0: Threshold sigma NEZ_0.
    :type thresholdSigmaNEZ0: float
    :param techReadinessLevel: Instrument technology readiness level.
    :type techReadinessLevel: int
    :param mountType: Type of mounting. Recognized values include: \
    BODY (default), MAST, PROBE.
    :type mountType: str or MountType
    """
    def __init__(self, name=None, acronym=None, agency=None, mass=None,
            volume=None, power=None, orientation=Orientation(),
            dataRate=None, bitsPerPixel=None, pulseWidth=None, antennaDimensionAlongTrack=None,
            antennaDimensionCrossTrack=None, antennaApertureEfficiency=None,
            operatingFrequency=None, peakTransmitPower=None, chirpBandwidth=None,
            minPulseRepetitionFrequency=None, maxPulseRepetitionFrequency=None,
            sceneNoiseTemp=None, systemNoiseFigure=None, radarLosses=None,
            thresholdSigmaNEZ0=None, techReadinessLevel=9, mountType="BODY", _id=None):
        self.pulseWidth = pulseWidth
        self.antennaDimensionAlongTrack = antennaDimensionAlongTrack
        self.antennaDimensionCrossTrack = antennaDimensionCrossTrack
        self.antennaApertureEfficiency = antennaApertureEfficiency
        self.operatingFrequency = operatingFrequency
        self.peakTransmitPower = peakTransmitPower
        self.chirpBandwidth = chirpBandwidth
        self.minPulseRepetitionFrequency = minPulseRepetitionFrequency
        self.maxPulseRepetitionFrequency = maxPulseRepetitionFrequency
        self.sceneNoiseTemp = sceneNoiseTemp
        self.systemNoiseFigure = systemNoiseFigure
        self.radarLosses = radarLosses
        self.thresholdSigmaNEZ0 = thresholdSigmaNEZ0
        super(SyntheticApertureRadar,self).__init__(name=name, acronym=acronym,
                agency=agency, mass=mass, volume=volume, power=power,
                orientation=orientation, fieldOfView=None,
                dataRate=dataRate, bitsPerPixel = bitsPerPixel, techReadinessLevel=techReadinessLevel,
                mountType=mountType, _id=_id, _type="Synthetic Aperture Radar")
