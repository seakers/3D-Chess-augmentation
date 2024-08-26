import tatc
import argparse
import demo_tse

def build_example_tradespace_search():
    mission = tatc.MissionConcept(
        name="Polar Operational Environmental Satellites",
        acronym="POES",
        agency=tatc.Agency(
            agencyType=tatc.AgencyType.GOVERNMENT,
            name="National Oceanic and Atmospheric Administration",
            acronym="NOAA"),
        start="2015-01-24T13:51:02Z",
        duration="P0Y0M90D",
        target=tatc.Region(
            latitude=tatc.QuantitativeValue(35, 45),
            longitude=tatc.QuantitativeValue(-115,-100)
        )
    )
    designSpace = tatc.DesignSpace(
        spaceSegment=[
            tatc.Constellation(
                constellationType=tatc.ConstellationType.DELTA_HOMOGENEOUS,
                numberSatellites=1,
                orbit=tatc.Orbit(orbitType=tatc.OrbitType.SUN_SYNCHRONOUS, altitude=833),
                satellites=[
                    tatc.Satellite(
                        name="NOAA-15",
                        mass=1457,
                        volume=7.098,
                        power=830,
                        commBand=[tatc.CommunicationBand.S, tatc.CommunicationBand.X],
                        payload=[
                        tatc.OpticalScanner(
                            name="Advanced Very High Resolution Radiometer",
                            acronym="AVHRR/3",
                            agency=tatc.Agency(
                                agencyType=tatc.AgencyType.GOVERNMENT,
                                name="National Aeronautics and Space Administration",
                                acronym="NASA"),
                            mass=33,
                            power=27,
                            scanTechnique="WHISKBROOM",
                            orientation=tatc.Orientation(
                                convention="SIDE_LOOK",
                                sideLookAngle=0
                            ),
                            numberOfDetectorsRowsAlongTrack=6,
                            numberOfDetectorsColsCrossTrack=1,
                            operatingWavelength=.63,
                            bandwidth=.1,
                            fieldOfView=tatc.FieldOfView(
                                    sensorGeometry="RECTANGULAR",
                                    alongTrackFieldOfView=.0745,
                                    crossTrackFieldOfView=120.24
                                ),
                            #numberPixels=2048,
                            dataRate=.6213
                        )]
                    )
                ],
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
            tatc.GroundNetwork(numberStations=2)
        ],
        groundStations=[
            #Wallops Island, VA
            tatc.GroundStation(
                latitude=37.93861111,
                longitude=-75.45722222,
                elevation=3,
                commBand=[tatc.CommunicationBand.S,tatc.CommunicationBand.X]
            ),
            #Alaska Satellite Facility, Fairbanks, AK
            tatc.GroundStation(
                latitude=64.858875394,
                longitude=-147.854115119,
                elevation=186,
                commBand=[tatc.CommunicationBand.S,tatc.CommunicationBand.X]
            )
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
        default = 'poes.json',
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
