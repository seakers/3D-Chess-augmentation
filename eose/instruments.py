from typing import Optional, Literal, Union
from pydantic import BaseModel, Field, model_validator
from enum import Enum

from eose.utils import Quaternion


class CircularGeometry(BaseModel):
    """Class to handle spherical circular geometries which define a closed angular space of interest.
    The pointing axis is fixed to the sensor Z-axis.
    """

    type: Literal["CircularGeometry"] = Field("CircularGeometry")
    diameter: float = Field(
        ...,
        gt=0,
        lt=180,
        description="Angular diameter of circular geometry about the sensor Z-axis in degrees.",
    )

class RectangularGeometry(BaseModel):
    """Class to handle spherical rectangular geometries which define a closed angular space of interest.
    The pointing axis is fixed to the sensor Z-axis.
    """

    type: Literal["RectangularGeometry"] = Field("RectangularGeometry")
    angle_height: float = Field(
        ...,
        gt=0,
        lt=180,
        description="Angular height (about the sensor X-axis) of the rectangular geometry in degrees.",
    )
    angle_width: float = Field(
        ...,
        gt=0,
        lt=180,
        description="Angular width (about the sensor Y-axis) of the rectangular geometry in degrees.",
    )

class BasicSensor(BaseModel):
    type: Literal["BasicSensor"] = Field("BasicSensor")
    name: Optional[str] = Field(None, description="Sensor name.")
    id: Optional[str] = Field(None, description="Sensor identifier.")
    mass: Optional[float] = Field(
        None, gt=0, description="Mass of the sensor in kilograms."
    )
    volume: Optional[float] = Field(
        None, gt=0, description="Volume of the sensor in cubic centimeter."
    )
    power: Optional[float] = Field(
        None, gt=0, description="(Average) Power consumption of the sensor in watts."
    )
    orientation: Quaternion = Field(
        default_factory=lambda: list([0, 0, 0, 1]),
        description="Orientation of the sensor body-fixed frame, relative to the spacecraft body-fixed frame. It is assumed that the sensor field of view (FOV) is aligned to the sensor body-fixed frame, and the sensor's FOV axis is aligned with its z-axis.",
    )
    field_of_view: Union[CircularGeometry, RectangularGeometry] = Field(
        default_factory=lambda: CircularGeometry(diameter=30),
        description="Field of view of the sensor.",
    )
    data_rate: Optional[float] = Field(
        None, gt=0, description="Data rate of the sensor in megabits per second."
    )
    bits_per_pixel: Optional[int] = Field(
        None, ge=1, description="Bits per pixel for the sensor's data output."
    )

class OpticalInstrumentScanTechnique(str, Enum):
    """Enumeration of recognized SAR scanning techniques."""
    PUSHBROOM = "PUSHBROOM"
    WHISKBROOM = "WHISKBROOM"
    MATRIX_IMAGER = "MATRIX_IMAGER"

class PassiveOpticalScanner(BaseModel):
    type: Literal["PassiveOpticalScanner"] = Field("PassiveOpticalScanner")
    name: Optional[str] = Field(None, description="Sensor name.")
    id: Optional[str] = Field(None, description="Sensor identifier.")
    mass: Optional[float] = Field(
        None, gt=0, description="Mass of the sensor in kilograms."
    )
    volume: Optional[float] = Field(
        None, gt=0, description="Volume of the sensor in cubic centimeter."
    )
    power: Optional[float] = Field(
        None, gt=0, description="(Average) Power consumption of the sensor in watts."
    )
    orientation: Quaternion = Field(
        default_factory=lambda: list([0, 0, 0, 1]),
        description="Orientation of the sensor body-fixed frame, relative to the spacecraft body-fixed frame. It is assumed that the sensor field of view (FOV) is aligned to the sensor body-fixed frame, and the sensor's FOV axis is aligned with its z-axis.",
    )
    field_of_view: Union[CircularGeometry, RectangularGeometry] = Field(
        default_factory=lambda: CircularGeometry(diameter=30),
        description="Field of view of the sensor."
    )
    scene_field_of_view: Optional[Union[CircularGeometry, RectangularGeometry]] = Field(
        None, description="Scene field of view of the sensor."
    )
    data_rate: Optional[float] = Field(
        None, gt=0, description="Data rate of the sensor in megabits per second."
    )
    scan_technique: Union[OpticalInstrumentScanTechnique, str] = Field(
        OpticalInstrumentScanTechnique.MATRIX_IMAGER,
        description="Scan technique"
    )
    number_detector_rows: Optional[int] = Field(
        None, ge=1, description="Number of detector rows (along the Y-axis of the sensor body-fixed frame). If the sensor body-fixed frame is aligned to the nadir-pointing frame, then this direction corresponds to the along-track direction."
    )
    number_detector_cols: Optional[int] = Field(
        None, ge=1, description="Number of detector columns (along the X-axis of the sensor body-fixed frame). If the sensor body-fixed frame is aligned to the nadir-pointing frame, then this direction corresponds to the cross-track direction."
    )
    aperture_dia: float = Field(
        ..., ge=0, description="Telescope aperture diameter in meters."
    )
    F_num: float = Field(
        ..., gt=0, description="F-number/ F# of lens."
    )
    focal_length: float = Field(
        ..., gt=0, description="Focal length of lens in meters."
    )
    operating_wavelength: float = Field(
        ..., gt=0, description="Center operating wavelength in meters."
    )
    bandwidth: float = Field(
        ..., gt=0, description="Bandwidth of operation in meters. It is assumed that the detector element supports the entire bandwidth with same quantum efficiency for all wavelengths. Assumption maybe reasonable for narrow-bandwidths."
    )
    quantum_efficiency: float = Field(
        ..., ge=0, le=1, description="Quantum efficiency of the detector element."
    )
    optics_sys_eff: Optional[float] = Field(
        1, gt=0, le=1, description="Optical systems efficiency."
    )
    number_of_read_out_E: float = Field(
        ..., ge=0, description="Number of read out electrons of the detector."
    )
    target_black_body_temp: float = Field(
        ..., ge=0, description="Target body's equivalent black-body temperature in Kelvin."
    )
    bits_per_pixel: Optional[int] = Field(
        None, ge=1, description="Bits per pixel for the sensor's data output."
    )
    detector_width: float = Field(
        ..., ge=0, description="Width of detector element in meters."
    )
    max_detector_exposure_time: Optional[float] = Field(
        None, ge=0, description="Maximum exposure time of detector in seconds."
    )


#class SinglePolStripMapSAR(BaseModel):
#    type: Literal["SinglePolStripMapSAR"] = Field("SinglePolStripMapSAR")

class Antenna(BaseModel):

    class CircularAntennaShape(BaseModel):
        """ Class to handle circular antenna shape.
        """
        type: Literal["CircularAntennaShape"] = Field("CircularAntennaShape")
        diameter: float = Field(
            ...,
            gt=0,
            description="Diameter of the circular antenna in meters.",
        )
    
    class RectangularAntennaShape(BaseModel):
        """ Class to handle rectangular antenna shape.
        """
        type: Literal["RectangularAntennaShape"] = Field("RectangularAntennaShape")
        height: float = Field(
            ...,
            gt=0,
            description="Antenna height (about the sensor X-axis) in meters.",
        )
        width: float = Field(
            ...,
            gt=0,
            description="Antenna width (about the sensor Y-axis) in meters.",
        )

    class ApertureExcitationProfile(str, Enum):
        """Enumeration of recognized antenna aperture excitation profiles.
            
        :cvar UNIFORM: Uniform excitation profile.
        :vartype UNIFORM: str

        :cvar COSINE: Cosine excitation profile.
        :vartype COSINE: str
        
        """
        UNIFORM = "UNIFORM",
        COSINE = "COSINE"

    shape: Union[CircularAntennaShape, RectangularAntennaShape] = Field(
        ..., 
        description="Antenna shape.")
    aperture_excitation_profile: ApertureExcitationProfile = Field(
        ApertureExcitationProfile.UNIFORM, 
        description="Antenna aperture excitation profile."
    )

class SinglePolStripMapSAR(BaseModel):
    """ Single polarization, strip map synthetic aperture radar.

        Field of view is calculated from the antenna specifications.

        Reference for the inline described parameters: 
            1. Performance Limits for Synthetic Aperture Radar - second edition SANDIA Report 2006. ----> Main reference.

    """
    type: Literal["SinglePolStripMapSAR"] = Field("SinglePolStripMapSAR")
    name: Optional[str] = Field(None, description="Sensor name.")
    id: Optional[str] = Field(None, description="Sensor identifier.")
    mass: Optional[float] = Field(
        None, gt=0, description="Mass of the sensor in kilograms."
    )
    volume: Optional[float] = Field(
        None, gt=0, description="Volume of the sensor in cubic centimeter."
    )
    power: Optional[float] = Field(
        None, gt=0, description="(Average) Power consumption of the sensor in watts."
    )
    orientation: Quaternion = Field(
        default_factory=lambda: list([0, 0, 0, 1]),
        description="Orientation of the sensor body-fixed frame, relative to the spacecraft body-fixed frame. It is assumed that the sensor field of view (FOV) is aligned to the sensor body-fixed frame, and the sensor's FOV axis is aligned with its z-axis.",
    )
    scene_field_of_view: Optional[RectangularGeometry] = Field(
        None, description="Scene field of view of the sensor."
    )
    data_rate: Optional[float] = Field(
        None, gt=0, description="Data rate of the sensor in megabits per second."
    )
    bits_per_pixel: Optional[int] = Field(
        None, ge=1, description="Bits per pixel for the sensor's data output."
    )
    pulse_Width: float = Field(
        None, gt=0, description="(Actual pulse width in (seconds) (per channel/polarization)."
    )
    antenna: Antenna = Field(
        ..., description="Antenna specifications."
    )
    operating_frequency: float = Field(
        ..., gt=0, description="Operating radar center frequency in (Hertz)."
    )
    peak_transmit_power: float = Field(
        ..., gt=0, description="Peak transmit power in (Watts)."
    )
    chirp_bandwidth: float = Field(
        ..., gt=0, description="""Chirp bandwidth of radar operation in (Hertz) (per channel/polarization)."""
    )
    minimum_prf: float = Field(
        ..., gt=0, description="""The minimum allowable pulse-repetition-frequency of operation in (Hertz).  
                                  If dual-pol with alternating pol pulses, the PRF specification is considered taking all pulses into account (i.e. is considered as the PRFmaster)."""
    )
    minimum_prf: int = Field(
        ..., gt=0, description="The minimum allowable pulse-repetition-frequency of operation in (Hertz).  If dual-pol with alternating pol pulses, the PRF specification is considered taking all pulses into account (i.e. is considered as the PRFmaster)."
    )
    maximum_prf: int = Field(
        ..., gt=0, description="The maximum allowable pulse-repetition-frequency of operation in (Hertz).  If dual-pol with alternating pol pulses, the PRF specification is considered taking all pulses into account (i.e. is considered as the PRFmaster)."
    )
    scene_noise_temp: float = Field(
        ..., gt=0, description="Nominal scene noise temperature in (Kelvin)."
    )
    system_noise_figure: float = Field(
        ..., gt=0, description="""(decibels) System noise figure for the receiver. The system noise figure includes primarily the noise figure of the front-end Low-Noise
                                  Amplifier (LNA) and the losses between the antenna and the LNA. Typical system noise figures for sub-kilowatt radar systems are 3.0 dB to 3.5 dB
                                  at X-band, 3.5 dB to 4.5 dB at Ku-band, and perhaps 6 dB at Ka-band. See [Pg.15, 1]."""
    )
    radar_loss: float = Field(
        ..., gt=0, description="""(decibels) These include a variety of losses primarily over the microwave signal path, but doesn't include the atmosphere. Included are a power loss from transmitter power amplifier
                           output to the antenna port, and a two-way loss through the radome. Typical numbers might be 0.5 dB to 2 dB from TX amplifier to the
                           antenna port, and perhaps an additional 0.5 dB to 1.5 dB two-way through the radome. See [Pg.15, 1]."""
    )
    atmos_loss: float = Field(
        ..., gt=0, description="2-way atmospheric loss of electromagnetic energy (see [Pg.16, 1])."
    )




