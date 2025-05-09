import numpy as np

# Earth's radius in meters
EARTH_RADIUS = 6378137.0  # meters

def calculate_ground_velocity(mission):
    """Calculate ground velocity based on orbital parameters
    
    Args:
        mission: Mission object containing orbital parameters
        
    Returns:
        float: Ground velocity in m/s
    """
    # Get orbital period from semi-major axis using Kepler's Third Law
    # T = 2π * sqrt(a³/μ) where μ = GM = 3.986004418e14 m³/s²
    mu = 3.986004418e14  # Earth's gravitational parameter
    a = mission.semiMajorAxis * 1000  # convert km to m
    T = 2 * np.pi * np.sqrt(a**3 / mu)  # orbital period in seconds
    
    # Calculate ground velocity
    # vg = 2πRe/T * cos(i) where Re is Earth's radius and i is inclination
    vg = (2 * np.pi * EARTH_RADIUS / T) * np.cos(np.radians(mission.inclination))
    
    return vg

class Mission:
    def __init__(self, satelliteDryMass, structureMass, propulsionMass, ADCSMass, avionicsMass, 
                 thermalMass, EPSMass, satelliteBOLPower, satDataRatePerOrbit, lifetime, numPlanes, 
                 numSats, instruments, launchVehicle, semiMajorAxis=None, inclination=None, **kwargs):
        self.satelliteDryMass = satelliteDryMass
        self.structureMass = structureMass
        self.propulsionMass = propulsionMass
        self.ADCSMass = ADCSMass
        self.avionicsMass = avionicsMass
        self.thermalMass = thermalMass
        self.EPSMass = EPSMass
        self.satelliteBOLPower = satelliteBOLPower
        self.satDataRatePerOrbit = satDataRatePerOrbit
        self.lifetime = lifetime
        self.numPlanes = numPlanes
        self.numSats = numSats
        self.instruments = instruments
        self.launchVehicle = launchVehicle
        self.semiMajorAxis = semiMajorAxis  # in km
        self.inclination = inclination     
        self.has_all_paper_params = False

        for k in kwargs.keys():
            self.__setattr__(k,kwargs[k])

class Instrument:
    def __init__(self, trl, mass=None, avgPower=None, dataRate=None, 
                 FOV=None, f=None, Nv=None, Ns=None, pv=None, ps=None, detectorWidth=None,T=None, D=None, 
                 b=16, height=None, inclination=None, **kwargs):
        """Initialize an instrument with either direct parameters or parameters to calculate them
        
        Args:
            trl: Technology Readiness Level
            mass: Direct mass input (kg)
            avgPower: Direct power input (W)
            dataRate: Direct data rate input (kbps)
            FOV: Field of view (degrees)
            f: Focal length (m)
            Nv: Number of VNIR spectral pixels
            Ns: Number of SWIR spectral pixels
            T: TIR presence (1 or 0)
            D: Aperture size (m)
            b: Bits per pixel (default 16)
            height: Satellite altitude in km
            inclination: Orbital inclination in degrees
        """
        self.trl = trl
        
        # Check if we have all parameters for the paper's methodology
        paper_params = [FOV, f, Nv, Ns, D, height, inclination, pv, ps, detectorWidth]
        self.has_all_paper_params = all(x is not None and x != 0 for x in paper_params)
        self.has_all_paper_params=False
        
        if self.has_all_paper_params:
            T = 0
            # Convert FOV from degrees to radians for calculations
            theta_p = np.radians(FOV)
            
            # Calculate spatial pixels
            Nx = calculate_spatial_pixels(ps, f, theta_p)
            
            # Calculate masses
            m_vnir = calculate_vnir_mass(Nx, Nv)
            m_swir = calculate_swir_mass(Nx, Ns)
            m_tir = calculate_tir_mass(T)
            m_lens = calculate_lens_mass(f, D)
            
            # Calculate total mass
            self.mass = calculate_total_mass(m_vnir, m_swir, m_tir, m_lens)
            
            # Calculate power
            self.avgPower = calculate_power(Nx, Nv, Ns, T)
            
            # Calculate ground velocity from orbital parameters
            vg = calculate_ground_velocity_from_height(height, inclination)
            delta_x = compute_delta_x(height_m=height*1000, pixel_size_m=ps, focal_length_m=f, aperture_m=D, has_SWIR=True)


            # Calculate data rate
            self.dataRate = calculate_data_rate(Nx, Nv, Ns, b, vg, delta_x)
        # Fall back to direct parameters if paper's methodology can't be used
        elif all(x is not None for x in [mass, avgPower, dataRate]):
            self.mass = mass
            self.avgPower = avgPower
            self.dataRate = dataRate
        else:
            raise ValueError("Must provide either all paper methodology parameters (FOV, f, Nv, Ns, D, height, inclination) or direct parameters (mass, avgPower, dataRate)")

        for k in kwargs.keys():
            self.__setattr__(k,kwargs[k])

class LaunchVehicle:
    def __init__(self, height, diameter, cost, **kwargs):
        self.height = height
        self.diameter = diameter
        self.cost = cost

        for k in kwargs.keys():
            self.__setattr__(k,kwargs[k])

def calculate_spatial_pixels(pixelSize, focalLength, detectorWidth):
    """Calculate number of spatial pixels using Eq. (8)"""
    N_x = math.floor(detectorWidth * focalLength / pixelSize)
    max_Nx=8192
    return min(N_x, max_Nx)

def calculate_vnir_mass(Nx, Nv):
    """Calculate VNIR imager mass using Eq. (9)"""
    return 0.363 + (1.40e-6) * Nx * Nv

def calculate_swir_mass(Nx, Ns):
    """Calculate SWIR imager mass using Eq. (10)"""
    return 0.618 + (2.26e-5) * Nx * Ns

def calculate_tir_mass(T):
    """Calculate TIR sensor mass based on TIR presence"""
    return 13.1 if T == 1 else 0

def calculate_lens_mass(f, D):
    """Calculate lens mass using Eq. (11)"""
    return np.exp(4.365 * f + 2.009 * D - 2.447)

def calculate_total_mass(m_vnir, m_swir, m_tir, m_lens):
    """Calculate total instrument mass using Eq. (12)"""
    return m_vnir + m_swir + m_tir + m_lens

def calculate_power(Nx, Nv, Ns, T):
    """Calculate instrument power using Eq. (13)"""
    base_power = (2.69e-5) * (Nv + Ns) * Nx + 1.14
    return base_power + (200 if T == 1 else 0)

def calculate_data_rate(Nx, Nv, Ns, b, vg, delta_x):
    """Calculate data rate using Eq. (14)"""
    return (Nv + Ns) * Nx * b * vg / delta_x/8

# def apply_NICM(m, p, rb):
#     """Apply original NASA Instrument Cost Model"""
#     cost = 25600 * ((p / 61.5) ** 0.32) * ((m / 53.8) ** 0.26) * ((1000 * rb / 40.4) ** 0.11)
#     cost = cost / 1.097  # correct for inflation and transform to $M
#     return cost
def apply_NICM(m, p, rb):
    """Apply NASA Instrument Cost Model from the paper using Eq. (15)"""
    cost = 979.9 * (m ** 0.328) * (p ** 0.357) * (rb ** 0.092)  # Cost in FY04$K
    return cost/1000  # to $M
def apply_NICM_paper(m, p, rb):
    """Apply NASA Instrument Cost Model from the paper using Eq. (15)"""
    cost = 979.9 * (m ** 0.328) * (p ** 0.357) * (rb ** 0.092)  # Cost in FY04$K
    return cost/1000  # to $M
import math

def compute_delta_x(height_m, pixel_size_m, focal_length_m, aperture_m, has_SWIR=True):
    """
    Compute spatial resolution delta x (in meters), considering both sampling and diffraction limits.

    Parameters:
    - height_m: satellite orbit height (h), in meters
    - pixel_size_m: pixel size (p), in meters
    - focal_length_m: focal length (f), in meters
    - aperture_m: aperture diameter (D), in meters
    - has_SWIR: if True, lambda = 2500nm; else 1000nm

    Returns:
    - delta_x (spatial resolution) in meters
    """
    wavelength = 2.5e-6 if has_SWIR else 1.0e-6

    # Sampling-limited resolution
    sampling_term = (height_m * pixel_size_m) / focal_length_m

    # Diffraction-limited resolution
    diffraction_term = (1.22 * height_m * wavelength) / aperture_m

    return max(sampling_term, diffraction_term)


def calculate_ground_velocity_from_height(height, inclination):
    """Calculate ground velocity based on height and inclination
    
    Args:
        height: Satellite altitude in km
        inclination: Orbital inclination in degrees
        
    Returns:
        float: Ground velocity in m/s
    """
    # Get orbital period from height using Kepler's Third Law
    # T = 2π * sqrt(a³/μ) where μ = GM = 3.986004418e14 m³/s²
    mu = 3.986004418e14  # Earth's gravitational parameter
    a = (height + EARTH_RADIUS/1000) * 1000  # convert km to m
    T = 2 * np.pi * np.sqrt(a**3 / mu)  # orbital period in seconds
    
    # Calculate ground velocity
    # vg = 2πRe/T * cos(i) where Re is Earth's radius and i is inclination
    vg = (2 * np.pi * EARTH_RADIUS / T) * np.cos(np.radians(inclination))
    
    return max(vg, 1000)

# Example usage:


def estimate_instrument_cost(instrument):
    """Estimate cost for a single instrument
    
    Args:
        instrument: Instrument object containing mass, power, and data rate
        use_paper_model: If True, use the paper's NICM model, otherwise use the original model
    """
    m = instrument.mass
    p = instrument.avgPower
    rb = instrument.dataRate
    
    if instrument.has_all_paper_params:
        cost = apply_NICM_paper(m, p, rb)
    else:
        cost = apply_NICM(m, p, rb)
    
    instrument.cost = cost

def estimate_payload_cost(mission):
    """Estimate total payload cost for all instruments
    
    Args:
        mission: Mission object containing instruments
        use_paper_model: If True, use the paper's NICM model, otherwise use the original model
    """
    instruments = mission.instruments
    
    costs = 0
    for instrument in instruments:
        estimate_instrument_cost(instrument)
        costs += instrument.cost
    
    total_cost = costs
    
    mission.payloadCost = total_cost
    mission.payloadNonRecurringCost = total_cost * 0.8
    mission.payloadRecurringCost = total_cost * 0.2

def estimate_bus_non_recurring_cost(mission):
    strm = mission.structureMass
    prm = mission.propulsionMass
    adcm = mission.ADCSMass
    comm = mission.avionicsMass
    thm = mission.thermalMass
    p = mission.satelliteBOLPower
    epsm = mission.EPSMass
    
    str_cost = 157 * (strm ** 0.83)
    prop_cost = 17.8 * (prm ** 0.75)
    adcs_cost = 464 * (adcm ** 0.867)
    comm_cost = 545 * (comm ** 0.761)
    therm_cost = 394 * (thm ** 0.635)
    pow_cost = 2.63 * ((epsm * p) ** 0.712)
    
    cost = str_cost + prop_cost + adcs_cost + comm_cost + therm_cost + pow_cost
    mission.busNonRecurringCost = cost

def estimate_bus_TFU_recurring_cost(mission):
    strm = mission.structureMass
    prm = mission.propulsionMass
    adcm = mission.ADCSMass
    comm = mission.avionicsMass
    thm = mission.thermalMass
    epsm = mission.EPSMass
    
    str_cost = 13.1 * strm
    prop_cost = 4.97 * (prm ** 0.823)
    adcs_cost = 293 * (adcm ** 0.777)
    comm_cost = 635 * (comm ** 0.568)
    therm_cost = 50.6 * (thm ** 0.707)
    pow_cost = 112 * (epsm ** 0.763)
    
    cost = str_cost + prop_cost + adcs_cost + comm_cost + therm_cost + pow_cost
    mission.busRecurringCost = cost

def estimate_spacecraft_cost_dedicated(mission):
    busnr = mission.busNonRecurringCost
    bus = mission.busRecurringCost
    payl = mission.payloadCost
    
    spacecraftnr = busnr + (payl * 0.6)
    spacecraft = bus + (payl * 0.4)
    sat = spacecraftnr + spacecraft
    
    mission.spacecraftNonRecurringCost = spacecraftnr
    mission.spacecraftRecurringCost = spacecraft
    mission.busCost = busnr + bus
    mission.satelliteCost = sat

def estimate_integration_and_testing_cost(mission):
    scnr = mission.spacecraftNonRecurringCost
    m = mission.satelliteDryMass
    
    iatnr = 989 + (scnr * 0.215)
    iatr = 10.4 * m
    iat = iatr + iatnr
    
    mission.IATNonRecurringCost = iatnr
    mission.IATRecurringCost = iatr
    mission.IATCost = iat

def estimate_program_overhead_cost(mission):
    scnr = mission.spacecraftNonRecurringCost
    scr = mission.spacecraftRecurringCost
    
    prognr = 1.963 * (scnr ** 0.841)
    progr = 0.341 * scr
    prog = progr + prognr
    
    mission.programNonRecurringCost = prognr
    mission.programRecurringCost = progr
    mission.programCost = prog

def estimate_operations_cost(mission):
    sat = mission.satelliteCost
    prog = mission.programCost
    iat = mission.IATCost
    rbo = mission.satDataRatePerOrbit
    life = mission.lifetime
    
    total_cost = sat + prog + iat
    total_cost = total_cost * 0.001097  # correct for inflation and transform to $M
    
    ops_cost = 0.035308 * (total_cost ** 0.928) * life  # NASA MOCM in FY04$M
    ops_cost = ops_cost / 0.001097  # back to FY00$k
    
    if rbo > (5 * 60 * 700 / 8192):
        pen = 10.0
    else:
        pen = 1.0
    
    mission.operationsCost = ops_cost * pen

def get_instrument_list_trls(instruments):
    trls = []
    for instrument in instruments:
        trl = instrument.trl
        trls.append(trl)
    return trls

def compute_cost_overrun(trls):
    min_trl = 10
    for trl in trls:
        if trl < min_trl:
            min_trl = trl
    rss = 8.29 * np.exp(-0.56 * min_trl)
    return 0.017 + (0.24 * rss)

def estimate_total_mission_cost_with_overruns(mission):
    sat = mission.satelliteCost
    prog = mission.programCost
    iat = mission.IATCost
    ops = mission.operationsCost
    launch = mission.launchVehicle.cost
    ins = mission.instruments
    
    mission_cost = sat + prog + iat + ops + (1000 * launch)
    mission_cost = mission_cost / 1000  # to $M
    
    over = compute_cost_overrun(get_instrument_list_trls(ins))
    
    mission.missionCost = mission_cost * (1 + over)

def estimate_total_mission_cost_with_overruns_when_partnership(mission):
    sat = mission.satelliteCost
    prog = mission.programCost
    iat = mission.IATCost
    ops = mission.operationsCost
    launch = mission.launchVehicle.cost
    payl = mission.payloadCost
    bus = mission.busCost
    ins = mission.instruments
    prt = mission.partnershipType
    
    costs = [payl, bus, (1000 * launch), prog, iat, ops]
    
    mission_cost = np.dot(costs, prt)
    mission_cost = mission_cost / 1000  # to $M
    
    over = compute_cost_overrun(get_instrument_list_trls(ins))
    
    mission.missionCost = mission_cost * (1 + over)

def estimate_total_mission_cost_non_recurring(mission):
    bus = mission.busNonRecurringCost
    payl = mission.payloadNonRecurringCost
    prog = mission.programNonRecurringCost
    iat = mission.IATNonRecurringCost
    
    mission_cost = (bus + payl + prog + iat) / 1000  # to $M
    
    mission.missionNonRecurringCost = mission_cost

def estimate_total_mission_cost_recurring(mission):
    bus = mission.busRecurringCost
    payl = mission.payloadRecurringCost
    prog = mission.programRecurringCost
    iat = mission.IATRecurringCost
    ops = mission.operationsCost
    launch = mission.launchVehicle.cost
    numPlanes = mission.numPlanes
    numSats = mission.numSats
    
    mission_cost = (bus + payl + prog + iat + ops) / 1000  # to $M
    
    S = 0.95  # 95% learning curve, means doubling N reduces average cost by 5%
    N = numSats # different from vassar bc pau's code gives total number of sats as opposed to number of sats per plane
    B = -1 / (np.log(1 / S) / np.log(2))
    L = N ** B
    
    total_cost = L * mission_cost
    
    mission.missionRecurringCost = total_cost + launch

def estimate_lifecycle_mission_cost(mission):
    rec = mission.missionRecurringCost
    nr = mission.missionNonRecurringCost
    
    mission.lifecycleCost = rec + nr

def costEstimationManager(mission):
    # Final Goal lifecycleCost
    estimate_bus_TFU_recurring_cost(mission)
    estimate_bus_non_recurring_cost(mission)
    estimate_payload_cost(mission)
    estimate_spacecraft_cost_dedicated(mission)
    estimate_program_overhead_cost(mission)
    estimate_integration_and_testing_cost(mission)
    estimate_operations_cost(mission)

    # total recurring and nonrecurring cost for total lifecycle cost
    estimate_total_mission_cost_recurring(mission)
    estimate_total_mission_cost_non_recurring(mission)

    # total lifecycle cost
    estimate_lifecycle_mission_cost(mission)

    return mission




