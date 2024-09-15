import tatc
import argparse
import demo_tse

def build_example_tradespace_search():
    mission = tatc.MissionConcept(
        name="Defense Meteorological Satellite Program",
        acronym="DMSP",
        agency=tatc.Agency(
            agencyType=tatc.AgencyType.GOVERNMENT,
            name="Department of Defense",
            acronym="DOD"),
        start="2014-04-03T00:00:00Z",
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
                orbit=tatc.Orbit(orbitType=tatc.OrbitType.SUN_SYNCHRONOUS, altitude=860),
            )
        ],
        satellites=[
            tatc.Satellite(
                name="F19",
                mass=1200,
                volume=7.098,
                power=1864,
                commBand=[tatc.CommunicationBand.S, tatc.CommunicationBand.X],
                payload=[
                #https://data.noaa.gov/metaview/page?xml=NOAA/NESDIS/NGDC/STP/DMSP/iso/xml/G01119.xml&view=getDataView&header=none
                #https://www.wmo-sat.info/oscar/instruments/view/376
                tatc.OpticalScanner(
                    name="Operational Linescan System",
                    acronym="OLS",
                    agency=tatc.Agency(
                        agencyType=tatc.AgencyType.GOVERNMENT,
                        name="Department of Defense",
                        acronym="DOD"),
                    mass=25,
                    power=170,
                    scanTechnique="WHISKBROOM",
                    orientation=tatc.Orientation(
                        convention="SIDE_LOOK",
                        sideLookAngle=0
                    ),
                    dataRate=.266,
                    numberOfDetectorsRowsAlongTrack=1,
                    numberOfDetectorsColsCrossTrack=1,
                    operatingWavelength=.71,
                    bandwidth=.48,
                    bitsPerPixel=6, #0-63
                    fieldOfView=tatc.FieldOfView(
                            sensorGeometry="RECTANGULAR",
                            alongTrackFieldOfView=.033,
                            crossTrackFieldOfView=119.68
                        ),
                    #numberPixels=7325
                ),
                tatc.Instrument(
                    name="Special Sensor Microwave - Imager/Sounder",
                    acronym="SSMIS",
                    agency=tatc.Agency(
                        agencyType=tatc.AgencyType.GOVERNMENT,
                        name="Department of Defense",
                        acronym="DOD"),
                    mass=96,
                    power=135
                ),
                tatc.Instrument(
                    name="- ultraviolet limb imager",
                    acronym="SSULI",
                ),
                tatc.Instrument(
                    name="- ultaviolet spectrographic imager and nadir airglow photometer",
                    acronym="SSUSI",
                ),
                tatc.Instrument(
                    name="- thermal plasma instrument",
                    acronym="SSI/ES-3",
                ),
                tatc.Instrument(
                    name="- precipitating particle spectrometer",
                    acronym="SSJ/5",
                ),
                tatc.Instrument(
                    name="- laser threat warning sensor",
                    acronym="SSF",
                )]
            )
        ],
        #launchers=[
            #tatc.LaunchVehicle(
                #name="Atlas V",
                #dryMass=29581,
                #propellantMass=304919,
                #specificImpulse=311, 
                #massToLEO=10470,
                #reliability=1,
                #cost=164,
                #meanTimeBetweenLaunches="P0Y0M75D"
            #)
        #],
        groundSegment=[
            tatc.GroundNetwork(numberStations=5)
        ],
        groundStations=[
            #Kaena Point, HI
            tatc.GroundStation(
                latitude=21.5633971,
                longitude=-158.2573336,
                commBand=[tatc.CommunicationBand.S,tatc.CommunicationBand.X]
            ),
            #Thule Air Base, Greenland
            tatc.GroundStation(
                latitude=76.5333076,
                longitude=-68.7020988,
                commBand=[tatc.CommunicationBand.S,tatc.CommunicationBand.X]
            ),
            #Alaska Satellite Facility, Fairbanks, AK
            tatc.GroundStation(
                latitude=64.858875394,
                longitude=-147.854115119,
                elevation=186,
                commBand=[tatc.CommunicationBand.S,tatc.CommunicationBand.X]
            ),
            #New Boston, NH
            tatc.GroundStation(
                latitude=42.9467089,
                longitude=-71.6306339,
                elevation=1363,
                commBand=[tatc.CommunicationBand.S,tatc.CommunicationBand.X]
            ),
            #McMurdo, Antarctica
            tatc.GroundStation(
                latitude=-77.8401191,
                longitude=166.6445298,
                elevation=1363,
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
        default = 'dmsp.json',
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
