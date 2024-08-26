import tatc
import argparse
import demo_tse

def build_example_tradespace_search():
    mission = tatc.MissionConcept(
        name="Joint Polar Satellite System",
        acronym="JPSS",
        agency=tatc.Agency(
            agencyType=tatc.AgencyType.GOVERNMENT,
            name="National Oceanic and Atmospheric Administration",
            acronym="NOAA"),
        start="2017-11-18T09:47:00Z",
        duration="P0Y0M90D",
        target=tatc.Region(
            latitude=tatc.QuantitativeValue(35, 45),
            longitude=tatc.QuantitativeValue(-115,-100)
        )
    )
    designSpace = tatc.DesignSpace(
        spaceSegment=[
            tatc.Constellation(
                constellationType="DELTA_HOMOGENOUS",
                numberSatellites=[2],
                numberPlanes=[1],
                orbit=tatc.Orbit(orbitType=tatc.OrbitType.SUN_SYNCHRONOUS, altitude=833),
            )
        ],
        satellites=[
            tatc.Satellite(
                name="S-NPP",
                mass=2128,
                volume=7.098,
                power=1864,
                commBand=[tatc.CommunicationBand.S, tatc.CommunicationBand.X],
                payload=[
                tatc.OpticalScanner(
                    name="Visible Infrared Imaging Radiometer Suite",
                    acronym="VIIRS",
                    agency=tatc.Agency(
                        agencyType=tatc.AgencyType.GOVERNMENT,
                        name="National Aeronautics and Space Administration",
                        acronym="NASA"),
                    mass=275,
                    power=240,
                    scanTechnique="WHISKBROOM",
                    orientation=tatc.Orientation(
                        convention="SIDE_LOOK",
                        sideLookAngle=0
                    ),
                    numberOfDetectorsRowsAlongTrack=32,
                    numberOfDetectorsColsCrossTrack=1,
                    operatingWavelength=.7,
                    bandwidth=.4,
                    fieldOfView=tatc.FieldOfView(
                            sensorGeometry="RECTANGULAR",
                            alongTrackFieldOfView=121.92,
                            crossTrackFieldOfView=163.98
                        ),
                    #numberPixels=4096,
                    dataRate=5.9,
                    focalLength=0.114,
                    apertureDia=0.191
                ),
                tatc.Instrument(
                    name="Cross-track Infrared Sounder",
                    acronym="CrIS",
                ),
                tatc.Instrument(
                    name="Advanced Technology Microwave Sounder",
                    acronym="ATMS",
                ),
                tatc.Instrument(
                    name="Ozone Mapper/Profiler Suite",
                    acronym="OMPS",
                ),
                tatc.Instrument(
                    name="Clouds and the Earth Radiant Energy System",
                    acronym="CERES",
                )]
            )
        ],
        #launchers=[
            #tatc.LaunchVehicle(
                #name="Delta-7920-10C",
                #dryMass=20882,
                #propellantMass=207138.8,
                #specificImpulse=274, 
                #massToLEO=3470,
                #reliability=1,
                #cost=60,
                #meanTimeBetweenLaunches="P0Y0M107D"
            #)
        #],
        groundSegment=[
            tatc.GroundNetwork(numberStations=4)
        ],
        groundStations=[
            #Svalbard Satellite Station, Longyearbyen, Svalbard, Norway
            tatc.GroundStation(
                latitude=78.22972222,
                longitude=15.40777778,
                elevation=455,
                commBand=[tatc.CommunicationBand.S,tatc.CommunicationBand.X]
            ),
            #McMurdo Station, Antarctica
            tatc.GroundStation(
                latitude=-77.84638889,
                longitude=166.66833333,
                elevation=100,
                commBand=[tatc.CommunicationBand.S,tatc.CommunicationBand.X]
            ),
            #Alaska Satellite Facility, Fairbanks, AK
            tatc.GroundStation(
                latitude=64.858875394,
                longitude=-147.854115119,
                elevation=186,
                commBand=[tatc.CommunicationBand.S,tatc.CommunicationBand.X]
            ),
            #Troll Satellite Station, Toll, Antarctica
            tatc.GroundStation(
                latitude=-72.01666667,
                longitude=2.53333333,
                elevation=1363,
                commBand=[tatc.CommunicationBand.S,tatc.CommunicationBand.X]
            ),
        ]
    )
    settings = tatc.AnalysisSettings(
        includePropulsion=False,
        proxyInstrument=True,
        proxyCostRisk=True,
        proxyLaunch=True,
        proxyOrbits=True,
        proxyValue=True,
        proxyMaintenance=True,
        outputs=tatc.AnalysisOutputs(keepLowLevelData=True)
    )
    search = tatc.TradespaceSearch(
        mission=mission,
        designSpace=designSpace,
        settings=settings
    )
    return search

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='Writes an example tradespace search file')
    parser.add_argument(
        'outfile',
        nargs = '?',
        type = argparse.FileType('w+'),
        default = 'jpss.json',
        help = 'Tradespace search output file'
    )
    parser.add_argument(
        '--execute',
        '-x',
        action='store_true',
        default=False,
        help = 'Execute tradespace search executive (TSE)'
    )
    args = parser.parse_args()
    build_example_tradespace_search().to_json(args.outfile, indent=2)
    if args.execute:
        args.outfile.seek(0) # reset reading from start of file
        demo_tse.execute(args.outfile)
