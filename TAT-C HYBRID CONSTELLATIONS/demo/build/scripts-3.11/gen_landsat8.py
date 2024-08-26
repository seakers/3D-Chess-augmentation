import tatc
import argparse
import demo_tse

def build_example_tradespace_search():
    """ Builds an example TradespaceSearch based on SLI mission."""
    mission = tatc.MissionConcept(
        name="Sustainable Land Imaging - Landsat 8",
        acronym="SLI-Landsat8",
        agency=tatc.Agency(tatc.AgencyType.GOVERNMENT),
        start="2017-08-01T00:00:00Z",
        duration="P0Y0M90D",
        target=tatc.Region(
            latitude=tatc.QuantitativeValue(35, 45),
            longitude=tatc.QuantitativeValue(-115,-100)
        )
    )
    designSpace = tatc.DesignSpace(
        spaceSegment=[
            tatc.Constellation(
                constellationType="DELTA_HOMOGENEOUS",
                numberSatellites=[1,2],
                numberPlanes=[1,2],
                orbit=tatc.Orbit(
                    orbitType=tatc.OrbitType.SUN_SYNCHRONOUS,
                    altitude=705
                )
            )
        ],
        satellites=[
            tatc.Satellite(
                name="Landsat 8",
                mass=2750,
                volume=43.2,
                power=1550,
                commBand=[tatc.CommunicationBand.X],
                payload=[
                    tatc.OpticalScanner(
                        name="Thermal Infrared Sensor Band 1",
                        acronym="TIRS Band 1",
                        agency=tatc.Agency(tatc.AgencyType.GOVERNMENT),
                        mass=236,
                        volume=0.261,
                        power=380,
                        fieldOfView=tatc.FieldOfView(
                            sensorGeometry="RECTANGULAR",
                            alongTrackFieldOfView=0.0081,
                            crossTrackFieldOfView=15
                        ),
                        scanTechnique="PUSHBROOM",
                        orientation=tatc.Orientation(
                            convention="SIDE_LOOK",
                            sideLookAngle=0
                        ),
                        dataRate=384,
                        numberOfDetectorsRowsAlongTrack=1,
                        numberOfDetectorsColsCrossTrack=1850,
                        detectorWidth=25e-6,
                        focalLength=0.178,
                        operatingWavelength=10.9e-6,
                        bandwidth=0.6e-6,
                        quantumEff=0.025,
                        targetBlackBodyTemp=290,
                        bitsPerPixel=12,
                        numOfReadOutE=20,
                        apertureDia=0.1085366,
                        Fnum=1.64,
                        maxDetectorExposureTime=3.49e-3,
                        snrThreshold=25
                    )
                ]
            )
        ],
        groundSegment=[
            tatc.GroundNetwork(numberStations=1)
        ],
        groundStations=[
            tatc.GroundStation(
                latitude=40.5974791834978,
                longitude=-104.83875274658203,
                elevation=1570,
                commBand=[tatc.CommunicationBand.X]
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
        default = 'landsat8.json',
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
