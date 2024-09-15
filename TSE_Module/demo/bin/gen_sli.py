import tatc
import argparse
import os
import demo_tse

def build_example_tradespace_search():
    """ Builds an example TradespaceSearch based on SLI mission."""
    mission = tatc.MissionConcept(
        name="Sustainable Land Imaging",
        acronym="SLI",
        agency=tatc.Agency(tatc.AgencyType.GOVERNMENT),
        start="2019-08-01T00:00:00Z",
        duration="P0Y0M90D",
        target=tatc.Region(
            latitude=tatc.QuantitativeValue(-90, 90),
            longitude=tatc.QuantitativeValue(-180, 180)
        )
    )

    aliceSpringsStation = tatc.GroundStation(
        name="Alice Springs",
        acronym="ASN",
        agency=tatc.Agency(tatc.AgencyType.GOVERNMENT),
        latitude=-23.7589,
        longitude=133.8822,
        elevation=0,
        commBand=["X"]
    )
    neustrelitzStation = tatc.GroundStation(
        name="Neustrelitz",
        acronym="NSN",
        agency=tatc.Agency(tatc.AgencyType.GOVERNMENT),
        latitude=53.3297,
        longitude=13.0725,
        elevation=0,
        commBand=["X", "S"]
    )
    svalbardStation = tatc.GroundStation(
        name="Svalbard Satellite Station",
        acronym="SGS",
        agency=tatc.Agency(tatc.AgencyType.GOVERNMENT),
        latitude=78.229772,
        longitude=15.407786,
        elevation=400,
        commBand=["X", "S"]
    )
    siouxFallsStation = tatc.GroundStation(
        name="Sioux Falls",
        acronym="LGS",
        agency=tatc.Agency(tatc.AgencyType.GOVERNMENT),
        latitude=43.7361,
        longitude=-96.6225,
        elevation=0,
        commBand=["X", "S"]
    )
    gilmoreCreekStation = tatc.GroundStation(
        name="Gilmore Creek",
        acronym="GLC",
        agency=tatc.Agency(tatc.AgencyType.GOVERNMENT),
        latitude=64.9764,
        longitude=-147.5208,
        elevation=306.418,
        commBand=["X"]
    )

    # https://landsat.usgs.gov/igs-network
    landsatNetwork = tatc.GroundNetwork(
        name="Landsat Ground Network",
        numberStations=5,
        groundStations=[
            aliceSpringsStation,
            neustrelitzStation,
            svalbardStation,
            siouxFallsStation,
            gilmoreCreekStation,
        ]
    )

    alaskaStation = tatc.GroundStation(
        name="Alaska Satellite Facility",
        acronym="ASF",
        agency=tatc.Agency(tatc.AgencyType.GOVERNMENT),
        latitude=64.859460942,
        longitude=-147.849742331,
        elevation=230,
        commBand=["X", "S"]
    )
    materaStation = tatc.GroundStation(
        name="Matera Space Centre",
        acronym="MTI",
        agency=tatc.Agency(tatc.AgencyType.GOVERNMENT),
        latitude=40.64913158,
        longitude=16.70445927,
        elevation=535.6515,
        commBand=["X"]
    )
    maspalomasStation = tatc.GroundStation(
        name="Maspalomas Station",
        agency=tatc.Agency(tatc.AgencyType.GOVERNMENT),
        latitude=27.7633,
        longitude=-15.6342,
        elevation=205.1,
        commBand=["X", "S"]
    )

    # https://sentinel.esa.int/web/sentinel/missions/sentinel-2/ground-segment/core-ground-segment/pdgs
    sentinel2Network = tatc.GroundNetwork(
        name="Sentinel 2 Ground Network",
        numberStations=4,
        groundStations=[
            svalbardStation,
            alaskaStation,
            materaStation,
            maspalomasStation
        ]
    )

    combinedStations = list(set(landsatNetwork.groundStations) | set(sentinel2Network.groundStations))

    combinedNetwork = tatc.GroundNetwork(
        name="Landsat/Sentinel-2 Ground Network",
        numberStations=len(combinedStations),
        groundStations=combinedStations
    )

    oliInstrument = tatc.OpticalScanner(
        name="OLI Blue Band",
        mass=1, #FIXME
        volume=1, #FIXME
        power=1, #FIXME
        fieldOfView=tatc.FieldOfView(
            sensorGeometry=tatc.SensorGeometry.RECTANGULAR,
            alongTrackFieldOfView=0.00244080020725731,
            crossTrackFieldOfView=15
        ),
        scanTechnique=tatc.ScanTechnique.PUSHBROOM,
        orientation=tatc.Orientation(
            convention=tatc.OrientationConvention.SIDE_LOOK,
            sideLookAngle=0
        ),
        dataRate=384,
        numberOfDetectorsRowsAlongTrack=1,
        numberOfDetectorsColsCrossTrack=6146,
        detectorWidth=36e-6,
        focalLength=845.1e-3,
        operatingWavelength=482e-9,
        bandwidth=65e-9,
        quantumEff=0.9,
        targetBlackBodyTemp=290,
        bitsPerPixel=12,
        numOfReadOutE=20,
        apertureDia=0.1320,
        Fnum=6.4,
        maxDetectorExposureTime=3.6e-3,
        snrThreshold=25
    )

    landsatConstellation = tatc.Constellation(
        constellationType="EXISTING",
        satellites=[
            tatc.Satellite(
                name="Landsat 8",
                mass=2750,
                dryMass=1512,
                volume=43.2,
                power=1550,
                commBand=["X"],
                orbit=tatc.Orbit(
                    orbitType=tatc.OrbitType.KEPLERIAN,
                    eccentricity=0.0001481,
                    inclination=98.2216,
                    semimajorAxis=7080,
                    periapsisArgument=95.2561,
                    rightAscensionAscendingNode=270.9458,
                    trueAnomaly=264.8806,
                    epoch="2017-07-19T21:55:36Z"
                ),
                payload=[oliInstrument]
            ),
            tatc.Satellite(
                name="Landsat 9",
                mass=2750,
                dryMass=1512,
                volume=43.2,
                power=1550,
                commBand=["X"],
                orbit=tatc.Orbit(
                    orbitType=tatc.OrbitType.KEPLERIAN,
                    eccentricity=0.0000749,
                    inclination=98.2091,
                    semimajorAxis=7080,
                    periapsisArgument=77.7435,
                    rightAscensionAscendingNode=271.3832,
                    trueAnomaly=282.3843,
                    epoch="2017-07-19T16:12:17Z"
                ),
                payload=[oliInstrument]
            )
        ]
    )

    msiInstrument = tatc.OpticalScanner(
        name="MSI Blue Band",
        mass=290,
        volume=1, #FIXME
        power=266,
        fieldOfView=tatc.FieldOfView(
            sensorGeometry=tatc.SensorGeometry.RECTANGULAR,
            alongTrackFieldOfView=0.000716197243913529,
            crossTrackFieldOfView=20.6
        ),
        scanTechnique=tatc.ScanTechnique.PUSHBROOM,
        orientation=tatc.Orientation(
            convention=tatc.OrientationConvention.SIDE_LOOK,
            sideLookAngle=0
        ),
        dataRate=450,
        numberOfDetectorsRowsAlongTrack=1,
        numberOfDetectorsColsCrossTrack=28763,
        detectorWidth=7.5e-6,
        focalLength=600e-3,
        operatingWavelength=492.4e-9,
        bandwidth=66e-9,
        quantumEff=0.85,
        targetBlackBodyTemp=290,
        bitsPerPixel=12,
        numOfReadOutE=40,
        apertureDia=150e-3,
        Fnum=4,
        snrThreshold=154
    )

    sentinel2Constellation = tatc.Constellation(
        constellationType=tatc.ConstellationType.EXISTING,
        satellites=[
            tatc.Satellite(
                name="Sentinel-2A",
                mass=1140,
                dryMass=1016,
                volume=14.382,
                power=1400,
                commBand=["X"],
                orbit=tatc.Orbit(
                    orbitType=tatc.OrbitType.KEPLERIAN,
                    eccentricity=0.0001033,
                    inclination=98.5662,
                    semimajorAxis=7167,
                    periapsisArgument=82.2486,
                    rightAscensionAscendingNode=275.3332,
                    trueAnomaly=277.8809,
                    epoch="2017-07-19T20:05:07Z"
                ),
                payload=[msiInstrument]
            ),
            tatc.Satellite(
                name="Sentinel-2B",
                mass=1140,
                dryMass=1016,
                volume=14.382,
                power=1400,
                commBand=["X"],
                orbit=tatc.Orbit(
                    orbitType=tatc.OrbitType.KEPLERIAN,
                    eccentricity=0.0001277,
                    inclination=98.5657,
                    semimajorAxis=7167,
                    periapsisArgument=83.4916,
                    rightAscensionAscendingNode=275.2148,
                    trueAnomaly=276.6412,
                    epoch="2017-07-19T17:34:03Z"
                ),
                payload=[msiInstrument]
            )
        ]
    )

    planetScopeInstrument = tatc.OpticalScanner(
        name="PlanetScope Blue Band",
        mass=4,
        volume=1.5,
        power=5,
        fieldOfView=tatc.FieldOfView(
            sensorGeometry=tatc.SensorGeometry.RECTANGULAR,
            alongTrackFieldOfView=1.9773,
            crossTrackFieldOfView=2.9662
        ),
        scanTechnique=tatc.ScanTechnique.PUSHBROOM,
        orientation=tatc.Orientation(
            convention=tatc.OrientationConvention.SIDE_LOOK,
            sideLookAngle=0
        ),
        dataRate=1, #FIXME
        numberOfDetectorsRowsAlongTrack=4452,
        numberOfDetectorsColsCrossTrack=6644,
        detectorWidth=5.5e-6,
        focalLength=0.7095,
        operatingWavelength=475e-9,
        bandwidth=110e-9,
        quantumEff=0.37,
        targetBlackBodyTemp=290,
        bitsPerPixel=12,
        numOfReadOutE=10,
        apertureDia=89e-3,
        Fnum=8,
        snrThreshold=30
    )

    doveConstellation = tatc.Constellation(
        constellationType=tatc.ConstellationType.AD_HOC,
        numberSatellites=50,
        satellites=[
            tatc.Satellite(
                name="Dove",
                mass=4.6,
                dryMass=4.6,
                volume=0.0034,
                power=1000,
                commBand=["X"],
                orbit=tatc.Orbit(
                    orbitType=tatc.OrbitType.SUN_SYNCHRONOUS
                ),
                payload=[planetScopeInstrument]
            )
        ]
    )

    designSpace1 = tatc.DesignSpace(
        spaceSegment=[
            landsatConstellation,
            sentinel2Constellation,
            tatc.Constellation(
                constellationType=tatc.ConstellationType.DELTA_HOMOGENEOUS,
                numberSatellites=1,
                orbit=tatc.Orbit(
                    orbitType=tatc.OrbitType.SUN_SYNCHRONOUS,
                    altitude=[600,650,700,750,800]
                ),
                satellites=[
                    tatc.Satellite(
                        name="Landsat 10",
                        mass=2750, #FIXME
                        dryMass=1512, #FIXME
                        volume=43.2, #FIXME
                        power=1550, #FIXME
                        commBand=["X"], #FIXME
                        payload=[
                            tatc.OpticalScanner(
                                name="Landsat 10 Blue Band",
                                mass=1, #FIXME
                                volume=1, #FIXME
                                power=1, #FIXME
                                fieldOfView=tatc.FieldOfView(
                                    sensorGeometry=tatc.SensorGeometry.RECTANGULAR,
                                    alongTrackFieldOfView=0.00244080020725731, #FIXME
                                    crossTrackFieldOfView=15 #FIXME
                                ),
                                scanTechnique=tatc.ScanTechnique.PUSHBROOM,
                                orientation=tatc.Orientation(
                                    convention=tatc.OrientationConvention.SIDE_LOOK,
                                    sideLookAngle=0
                                ),
                                dataRate=384, #FIXME
                                numberOfDetectorsRowsAlongTrack=1, #FIXME
                                numberOfDetectorsColsCrossTrack=6146, #FIXME
                                detectorWidth=36e-6, #FIXME
                                focalLength=845.1e-3, #FIXME
                                operatingWavelength=482e-9, #FIXME
                                bandwidth=65e-9, #FIXME
                                quantumEff=0.9, #FIXME
                                targetBlackBodyTemp=290, #FIXME
                                bitsPerPixel=12, #FIXME
                                numOfReadOutE=20, #FIXME
                                apertureDia=0.1320, #FIXME
                                Fnum=6.4, #FIXME
                                maxDetectorExposureTime=3.6e-3, #FIXME
                                snrThreshold=25 #FIXME
                            )
                        ]
                    )
                ]
            )
        ],
        groundSegment=[combinedNetwork]
    )

    designSpace2 = tatc.DesignSpace(
        spaceSegment=[
            landsatConstellation,
            sentinel2Constellation,
            tatc.Constellation(
                constellationType=tatc.ConstellationType.DELTA_HOMOGENEOUS,
                numberSatellites=[2,4,6,8,10],
                numberPlanes=[1,2],
                orbit=tatc.Orbit(
                    orbitType=tatc.OrbitType.SUN_SYNCHRONOUS,
                    altitude=[400,500,600,700,800]
                ),
                satellites=[
                    tatc.Satellite(
                        name="Landsat 10",
                        mass=2750, #FIXME
                        dryMass=1512, #FIXME
                        volume=43.2, #FIXME
                        power=1550, #FIXME
                        commBand=["X"], #FIXME
                        payload=[
                            tatc.OpticalScanner(
                                name="Landsat 10 Blue Band",
                                mass=1, #FIXME
                                volume=1, #FIXME
                                power=1, #FIXME
                                fieldOfView=tatc.FieldOfView(
                                    sensorGeometry=tatc.SensorGeometry.RECTANGULAR,
                                    alongTrackFieldOfView=0.00037, #FIXME
                                    crossTrackFieldOfView=3.0 #FIXME
                                ),
                                scanTechnique=tatc.ScanTechnique.PUSHBROOM,
                                orientation=tatc.Orientation(
                                    convention=tatc.OrientationConvention.SIDE_LOOK,
                                    sideLookAngle=0
                                ),
                                dataRate=384, #FIXME
                                numberOfDetectorsRowsAlongTrack=1, #FIXME
                                numberOfDetectorsColsCrossTrack=6146, #FIXME
                                detectorWidth=36e-6, #FIXME
                                focalLength=845.1e-3, #FIXME
                                operatingWavelength=485e-9,
                                bandwidth=65e-9, #FIXME
                                quantumEff=0.9, #FIXME
                                targetBlackBodyTemp=290, #FIXME
                                bitsPerPixel=12, #FIXME
                                numOfReadOutE=20, #FIXME
                                apertureDia=0.1320, #FIXME
                                Fnum=6.4, #FIXME
                                maxDetectorExposureTime=3.6e-3, #FIXME
                                snrThreshold=25 #FIXME
                            )
                        ]
                    )
                ]
            )
        ],
        groundSegment=[combinedNetwork]
    )

    designSpace3 = tatc.DesignSpace(
        spaceSegment=[
            landsatConstellation,
            sentinel2Constellation,
            doveConstellation,
            tatc.Constellation(
                constellationType=tatc.ConstellationType.DELTA_HOMOGENEOUS,
                numberSatellites=[1,2],
                numberPlanes=[1,2],
                orbit=tatc.Orbit(
                    orbitType=tatc.OrbitType.SUN_SYNCHRONOUS,
                    altitude=[600,650,700,750,800]
                ),
                satellites=[
                    tatc.Satellite(
                        name="Landsat 10 (Calibration)",
                        payload=[
                            tatc.OpticalScanner(
                                name="Landsat 10 Blue Band",
                                mass=1, #FIXME
                                volume=1, #FIXME
                                power=1, #FIXME
                                fieldOfView=tatc.FieldOfView(
                                    sensorGeometry=tatc.SensorGeometry.RECTANGULAR,
                                    alongTrackFieldOfView=0.00037, #FIXME
                                    crossTrackFieldOfView=3.0 #FIXME
                                ),
                                scanTechnique=tatc.ScanTechnique.PUSHBROOM,
                                orientation=tatc.Orientation(
                                    convention=tatc.OrientationConvention.SIDE_LOOK,
                                    sideLookAngle=0
                                ),
                                dataRate=384, #FIXME
                                numberOfDetectorsRowsAlongTrack=1, #FIXME
                                numberOfDetectorsColsCrossTrack=6146, #FIXME
                                detectorWidth=36e-6, #FIXME
                                focalLength=845.1e-3, #FIXME
                                operatingWavelength=485e-9,
                                bandwidth=65e-9, #FIXME
                                quantumEff=0.9, #FIXME
                                targetBlackBodyTemp=290, #FIXME
                                bitsPerPixel=12, #FIXME
                                numOfReadOutE=20, #FIXME
                                apertureDia=0.1320, #FIXME
                                Fnum=6.4, #FIXME
                                maxDetectorExposureTime=3.6e-3, #FIXME
                                snrThreshold=25 #FIXME
                            )
                        ]
                    )
                ]
            )
        ],
        groundSegment=[combinedNetwork]
    )

    settings = tatc.AnalysisSettings(
        includePropulsion=False,
        proxyMaintenance=True
    )
    return [
        tatc.TradespaceSearch(
            mission=mission,
            designSpace=designSpace1,
            settings=settings
        ),
        tatc.TradespaceSearch(
            mission=mission,
            designSpace=designSpace2,
            settings=settings
        ),
        tatc.TradespaceSearch(
            mission=mission,
            designSpace=designSpace3,
            settings=settings
        )
    ]

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='Writes an example application case')
    parser.add_argument(
        'outdir',
        nargs = '?',
        default = 'sli',
        help = 'Application case output directory'
    )
    parser.add_argument(
        '--execute',
        '-x',
        action='store_true',
        default=False,
        help = 'Execute tradespace search executive (TSE)'
    )
    args = parser.parse_args()
    for i, search in enumerate(build_example_tradespace_search()):
        arch_dir = os.path.join(args.outdir, 'sli-{:}'.format(i+1))
        if not os.path.exists(arch_dir):
            os.mkdir(arch_dir)
        with open(os.path.join(arch_dir, 'sli-{:}.json'.format(i+1)), 'w+') as out_file:
            search.to_json(out_file, indent=2)
            if args.execute:
                out_file.seek(0) # reset reading from start of file
                try:
                    demo_tse.execute(out_file)
                except NotImplementedError:
                    pass
