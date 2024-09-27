package tatc.tradespaceiterator;

import org.hipparchus.util.FastMath;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;
import seakers.conmop.util.Bounds;
import seakers.conmop.variable.ConstellationVariable;
import seakers.conmop.variable.SatelliteVariable;
import tatc.ResultIO;
import tatc.architecture.constellations.*;
import tatc.architecture.ArchitectureCreator;
import tatc.architecture.specifications.*;
import tatc.architecture.variable.*;
import tatc.util.*;
import tatc.architecture.StandardFormArchitecture;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class to define the TAT-C constellation design optimization problem.
 */
public class ProblemGA extends AbstractProblem {

    /**
     * Problem properties (includes the tradespace search request)
     */
    ProblemProperties properties;

    /**
     * Counter of architectures evaluated
     */
    private int counter = 0;

    /**
     * Creates a TAT-C constellation design optimization problem
     * @param properties the problem properties
     */
    public ProblemGA(ProblemProperties properties){
        super(properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().size() -
                        properties.getTradespaceSearch().getNumberExistingConstellations() + 1,
                properties.getObjectives().size() + properties.getTradespaceSearch().getMission().areThereSoftConstraints(),
                properties.getTradespaceSearch().getMission().getHardConstraints().size());
        this.properties=properties;
        ResultIO.createSummaryFile(new File(System.getProperty("tatc.output") + File.separator + "summary.csv")
                                            ,properties.getObjectives().size() + properties.getTradespaceSearch().getMission().areThereSoftConstraints());
    }

    @Override
    public void evaluate(Solution solution) throws IllegalArgumentException{
        StandardFormArchitecture soln = null;
        if (solution instanceof StandardFormArchitecture) {
            soln = (StandardFormArchitecture) solution;
        } else {
            throw new IllegalArgumentException(
                    String.format("Expected a TATCArchitecture."
                            + " Found %s", solution.getClass()));
        }

        String epoch = properties.getTradespaceSearch().getMission().getStart();

        /*
        1. From the gene, create a ArchitectureCreator object and add the ground network and all the constellations
        in the chromosome
         */
        int variableCounter=0;
        GroundNetwork groundNetwork = ((GroundNetworkVariable) soln.getVariable(variableCounter)).getGroundNetwork();
        variableCounter++;

        ArchitectureCreator architecture = new ArchitectureCreator();
        architecture.addGroundNetwork(groundNetwork);
        for (int i=1;i<soln.getNumberOfVariables();i++){

            if (soln.getVariable(i) instanceof HomogeneousWalkerVariable) {
                double altitudeHomo = ((HomogeneousWalkerVariable) soln.getVariable(variableCounter)).getAlt();
                Object inclinationHomo = ((HomogeneousWalkerVariable) soln.getVariable(variableCounter)).getInc();
                double inclinationModified;
                if (inclinationHomo.equals("SSO")) {
                    inclinationModified = FastMath.toDegrees(Utilities.incSSO(altitudeHomo * 1000,
                            ((HomogeneousWalkerVariable) soln.getVariable(variableCounter)).getEccentricity()));
                } else if (inclinationHomo instanceof Double) {
                    inclinationModified = (Double) inclinationHomo;
                } else {
                    throw new IllegalArgumentException("Inclination not identified");
                }
                int nsatHomo = ((HomogeneousWalkerVariable) soln.getVariable(variableCounter)).getT();
                double planeRealHomo = ((HomogeneousWalkerVariable) soln.getVariable(variableCounter)).getpReal();
                double phaseRealHomo = ((HomogeneousWalkerVariable) soln.getVariable(variableCounter)).getfReal();
                Satellite satelliteHomo = ((HomogeneousWalkerVariable) soln.getVariable(variableCounter)).getSatellite();

                //do not add constellation when nsat=0
                if (nsatHomo != 0) {
                    List<Integer> tradespacePlanes = ((HomogeneousWalkerVariable) soln.getVariable(variableCounter)).getpAllowed();
                    ArrayList<Integer> planeAndPhase = Utilities.obtainPlanesAndPhasingFromChromosome(nsatHomo, tradespacePlanes, planeRealHomo, phaseRealHomo);
                    int plane = planeAndPhase.get(0);
                    int phase = planeAndPhase.get(1);
                    HomogeneousWalkerParameters constellationHomo = new HomogeneousWalkerParameters(altitudeHomo, inclinationModified, nsatHomo, plane, phase, satelliteHomo);
                    constellationHomo.setSecondaryPayload(((HomogeneousWalkerVariable) soln.getVariable(variableCounter)).getSecondaryPayload());
                    constellationHomo.setEccentricity(((HomogeneousWalkerVariable) soln.getVariable(variableCounter)).getEccentricity());
                    architecture.addHomogeneousWalker(constellationHomo.getA()+ Utilities.EARTH_RADIUS_KM, constellationHomo.getI(), constellationHomo.getT(),
                            constellationHomo.getP(), constellationHomo.getF(), constellationHomo.getSatellite(), constellationHomo.getSecondaryPayload(), epoch, constellationHomo.getEccentricity());
                }
                variableCounter++;

            }else if (soln.getVariable(i) instanceof HeterogeneousWalkerVariable) {
                int nsatHetero = ((HeterogeneousWalkerVariable) soln.getVariable(variableCounter)).getT();
                double fRealHetero = ((HeterogeneousWalkerVariable) soln.getVariable(variableCounter)).getfReal();

                if (nsatHetero!=0){
                    int pHetero = ((HeterogeneousWalkerVariable) soln.getVariable(variableCounter)).getNumberOfPlanes();
                    List<PlaneVariable> pListHetero = ((HeterogeneousWalkerVariable) soln.getVariable(variableCounter)).getPlaneVariables();

                    List<WalkerPlane> pListWalkerPlanes = new ArrayList<>();
                    int planeNum = 0;
                    List<Integer> rangePhasing = IntStream.rangeClosed(0, pListHetero.size()-1).boxed().collect(Collectors.toList());
                    int fHetero = Utilities.obtainValueFromListAndRealValue(new ArrayList<>(rangePhasing),fRealHetero);
                    final int s = nsatHetero / pHetero; //number of satellites per plane
                    final double pu = 2 * FastMath.PI / nsatHetero; //pattern unit
                    final double delAnom = pu * pHetero; //in plane spacing between satellites
                    final double delRaan = pu * s; //node spacing
                    final double phasing = pu * fHetero;
                    final double refAnom = 0;
                    final double refRaan = 0;
                    for (PlaneVariable pv : pListHetero) {
                        double inclinationModifiedHetero;
                        if (pv.getInc().equals("SSO")) {
                            inclinationModifiedHetero = FastMath.toDegrees(Utilities.incSSO(pv.getAlt() * 1000,
                                    ((HeterogeneousWalkerVariable) soln.getVariable(variableCounter)).getEccentricity()));
                        } else if (pv.getInc() instanceof Double) {
                            inclinationModifiedHetero = (Double) pv.getInc();
                        } else {
                            throw new IllegalArgumentException("Inclination not identified");
                        }
                        double raan = FastMath.toDegrees(refRaan + planeNum * delRaan);
                        List<Double> tas = new ArrayList<>();
                        for (int satNum = 0; satNum < s; satNum++) {
                            tas.add(FastMath.toDegrees((refAnom + satNum * delAnom + phasing * planeNum) % (2. * FastMath.PI)));
                        }
                        pListWalkerPlanes.add(new WalkerPlane(pv.getAlt(), inclinationModifiedHetero, raan, tas));
                        planeNum++;
                    }
                    Satellite satelliteHetero = ((HeterogeneousWalkerVariable) soln.getVariable(variableCounter)).getSatellite();

                    HeterogeneousWalkerParameters constellationHet = new HeterogeneousWalkerParameters(nsatHetero, pHetero, fHetero, pListWalkerPlanes, satelliteHetero);
                    constellationHet.setSecondaryPayload(((HeterogeneousWalkerVariable) soln.getVariable(variableCounter)).getSecondaryPayload());
                    constellationHet.setEccentricity(((HeterogeneousWalkerVariable) soln.getVariable(variableCounter)).getEccentricity());
                    architecture.addHeterogenousWalker(constellationHet.getT(), constellationHet.getPlanes(), constellationHet.getSatellite(), constellationHet.getSecondaryPayload(), epoch, constellationHet.getEccentricity());
                }

                variableCounter++;

            }else if (soln.getVariable(i) instanceof TrainVariable) {
                int nsatTrain = ((TrainVariable) soln.getVariable(variableCounter)).getT();
                if (nsatTrain!=0){
                    double altitudeTrain = ((TrainVariable) soln.getVariable(variableCounter)).getAlt();
                    String LTAN = ((TrainVariable) soln.getVariable(variableCounter)).getLTAN();
                    String satelliteInterval = ((TrainVariable) soln.getVariable(variableCounter)).getSatInterval();
                    Satellite satelliteTrain = ((TrainVariable) soln.getVariable(variableCounter)).getSatellite();

                    TrainParameters constellationTrain = new TrainParameters(altitudeTrain, nsatTrain, LTAN, satelliteInterval, satelliteTrain);
                    constellationTrain.setSecondaryPayload(((TrainVariable) soln.getVariable(variableCounter)).getSecondaryPayload());
                    constellationTrain.setEccentricity(((TrainVariable) soln.getVariable(variableCounter)).getEccentricity());
                    AbsoluteDate startDate = Utilities.DateTimeToAbsoluteDate(properties.getTradespaceSearch().getMission().getStart());
                    architecture.addTrain(startDate, constellationTrain.getA(), constellationTrain.getNsat(), constellationTrain.getLTANref(),
                            constellationTrain.getSatelliteInterval(), constellationTrain.getSatellite(), constellationTrain.getSecondaryPayload(), epoch, constellationTrain.getEccentricity());
                }

                variableCounter++;

            }else if (soln.getVariable(i) instanceof AdHocVariable) {
                int nsatAdHoc = ((AdHocVariable) soln.getVariable(variableCounter)).getT();
                if (nsatAdHoc!=0){
                    Satellite satelliteAdHoc = ((AdHocVariable) soln.getVariable(variableCounter)).getSatellite();

                    AdHocParameters constellationAdHoc = new AdHocParameters(nsatAdHoc, satelliteAdHoc);
                    constellationAdHoc.setSecondaryPayload(((AdHocVariable) soln.getVariable(variableCounter)).getSecondaryPayload());
                    architecture.addAdHoc(constellationAdHoc.getNsat(),constellationAdHoc.getSatellite(), constellationAdHoc.getSecondaryPayload(), epoch);
                }

                variableCounter++;

            }else if (soln.getVariable(i) instanceof StringOfPearlsVariable) {
                    double altitudePearl = ((StringOfPearlsVariable) soln.getVariable(variableCounter)).getAlt();
                    Object inclinationPearl = ((StringOfPearlsVariable) soln.getVariable(variableCounter)).getInc();
                    double inclinationModified;
                    if (inclinationPearl.equals("SSO")) {
                        inclinationModified = FastMath.toDegrees(Utilities.incSSO(altitudePearl * 1000,
                                ((StringOfPearlsVariable) soln.getVariable(variableCounter)).getEccentricity()));
                    } else if (inclinationPearl instanceof Double) {
                        inclinationModified = (Double) inclinationPearl;
                    } else {
                        throw new IllegalArgumentException("Inclination not identified");
                    }
                    int nsatPearl = ((StringOfPearlsVariable) soln.getVariable(variableCounter)).getT();
                double raanPearl = ((StringOfPearlsVariable) soln.getVariable(variableCounter)).getRaan();
                    String satelliteInterval = ((StringOfPearlsVariable) soln.getVariable(variableCounter)).getSatInterval();
                    Satellite satelliteHomo = ((StringOfPearlsVariable) soln.getVariable(variableCounter)).getSatellite();

                    //do not add constellation when nsat=0
                    if (nsatPearl != 0) {
                        StringOfPearlsParameters constellationPearl = new StringOfPearlsParameters(altitudePearl, inclinationModified, nsatPearl, raanPearl,satelliteInterval, satelliteHomo);
                        constellationPearl.setSecondaryPayload(((StringOfPearlsVariable) soln.getVariable(variableCounter)).getSecondaryPayload());
                        constellationPearl.setEccentricity(((StringOfPearlsVariable) soln.getVariable(variableCounter)).getEccentricity());
                        architecture.addStringOfPearls(constellationPearl.getA()+ Utilities.EARTH_RADIUS_KM, constellationPearl.getI(), constellationPearl.getT(), constellationPearl.getRAAN(),
                                constellationPearl.getSatelliteInterval(), constellationPearl.getSatellite(), constellationPearl.getSecondaryPayload(), epoch, constellationPearl.getEccentricity());
                    }
                    variableCounter++;

            }else if (soln.getVariable(i) instanceof ConstellationVariable) {
                Collection<SatelliteVariable> satelliteVariables = ((ConstellationVariable) soln.getVariable(variableCounter)).getSatelliteVariables();
                Satellite satSpecification = null;
                for (Constellation c : properties.getTradespaceSearch().getDesignSpace().getSpaceSegment()){
                    if (c.getConstellationType().equalsIgnoreCase("GENERAL")) {
                        satSpecification = c.getSatellites().get(0);
                    }
                }
                architecture.addGeneral(satelliteVariables,satSpecification,epoch);
            }else{
                    throw new IllegalArgumentException("Constellation type has to be either DELTA_HOMOGENEOUS, DELTA_HETEROGENOUS, TRAIN OR AD_HOC.");
            }
        }

        double execTime = 0.0;
        if (!architecture.getConstellations().isEmpty()){
            // 2. create the Architecture JSON file
            File architectureJsonFile = architecture.toJSON(this.getCounter());
            // 3. Evaluate architecture
            long startTime = System.nanoTime();
            try{
                TradespaceSearchExecutive.evaluateArchitecture(architectureJsonFile, properties);
            }catch(IOException e){
            System.out.println("Error reading the JSON file: " + e.getMessage());
            e.printStackTrace();

            }catch(InterruptedException e){
                System.out.println("Error reading the JSON file: " + e.getMessage());
                e.printStackTrace();
    
                };
            long endTime = System.nanoTime();
            execTime = (endTime - startTime) / Math.pow(10, 9);
        }

        // 4. Read the objectives from the output files generated by the different modules
        int objectiveIndex = 0;
        //TODO: Important part where we read the objectives for the GA
        double[] objectives = new double[getNumberOfObjectives()];
        for (CompoundObjective obj : this.properties.getObjectives()){
            double valueObjective;
            if (!architecture.getConstellations().isEmpty()){
                valueObjective = obj.getObjectiveValue(this.getCounter());
                //valueObjective = 0;
            }else{
                valueObjective = Double.POSITIVE_INFINITY;
            }
            solution.setObjective(objectiveIndex, valueObjective);
            objectives[objectiveIndex]=valueObjective;
            objectiveIndex++;
        }

        // 5. Add the soft constraint feasibility objective (if there are any soft constraints)
        if (this.properties.getTradespaceSearch().getMission().areThereSoftConstraints()==1){
            double softConsTraintObjective = 0;
            if (architecture.getConstellations().isEmpty()){
                softConsTraintObjective = Double.POSITIVE_INFINITY;
            }else{
                double sumWeights = 0;
                for (MissionConstraint constraint : this.properties.getTradespaceSearch().getMission().getSoftConstraints()){
                    softConsTraintObjective += constraint.getWeight()*constraint.getConstraintValueNormalized(this.getCounter());
                    sumWeights += constraint.getWeight();
                }
                //Normalize the objective
                softConsTraintObjective=softConsTraintObjective/sumWeights;
            }
            solution.setObjective(objectiveIndex, softConsTraintObjective);
            objectives[objectiveIndex] = softConsTraintObjective;
        }

        // 6. Read the hard constraints from the output files generated by the different modules
        int constraintIndex = 0;
        for (MissionConstraint constraint : this.properties.getTradespaceSearch().getMission().getHardConstraints()){
            double valueConstraint;
            if (!architecture.getConstellations().isEmpty()){
                valueConstraint = constraint.getConstraintValue(this.getCounter());
            }else{
                valueConstraint = Double.POSITIVE_INFINITY;
            }
            solution.setConstraint(constraintIndex, valueConstraint);
            constraintIndex++;
        }

        if (!architecture.getConstellations().isEmpty()){
            //Add line in summary.csv
            ResultIO.addSummaryLine(new File(System.getProperty("tatc.output") + File.separator + "summary.csv"),ResultIO.getLineSummaryDataWithObjectives(architecture,this.getCounter(),execTime,objectives));
            // increment the counter for the arch id at each architecture evaluation
            this.incrementCounter();
        }

    }


    /**
     * Gets the counter of architectures evaluated
     * @return the counter of architectures evaluated
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Increments 1 unit the architectures evaluated counter
     */
    private void incrementCounter(){
        this.counter++;
    }


    @Override
    @SuppressWarnings("unchecked")
    public final Solution newSolution() {
        /*
        Creates a hybrid constellation chromosome containing a first GroundNetworkVariable and 1 or more constellation
        variables (including HeterogeneousWalkerVariable, HomogeneousWalkerVariable, TrainVariable or AdHocVariable).
        There will be as many constellation variables as constellations specifications in the design space inside
        the tradespace search request.
         */
        HashMap<String, Decision<?>> decisions = this.properties.getDecisions();
        Solution sol = new StandardFormArchitecture(getNumberOfVariables(), getNumberOfObjectives(), getNumberOfConstraints());

        int variableCounter=0;

        Decision<?> groundNetwork = decisions.get("groundNetwork");

        HashMap<Integer, List<Integer>> mapIdAndNumberStations = new HashMap<>();
        for (int id = 0; id<this.properties.getTradespaceSearch().getDesignSpace().getGroundSegment().size(); id++){
            Decision<?>  numberStations = decisions.get(String.format("numberGroundStations%d",id));
            mapIdAndNumberStations.put(id,(List<Integer>)numberStations.getAllowedValues());
        }
        sol.setVariable(variableCounter, new GroundNetworkVariable((List<GroundNetwork>) groundNetwork.getAllowedValues(),
                                                                    this.properties.getTradespaceSearch().getDesignSpace().getGroundStations(),
                                                                    mapIdAndNumberStations));
        Decision<?> altitude;
        Decision<?> inclination;
        Decision<?> nsats;
        Decision<?> raans;
        Decision<?> nplanes;
        Decision<?> satellite;
        Decision<?> ltans;
        Decision<?> satIntervals;
        for (int i = 0; i<this.properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().size(); i++){
            if (!properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(i).getConstellationType().equalsIgnoreCase("EXISTING")){
                switch (properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(i).getConstellationType()) {
                    case "DELTA_HOMOGENEOUS":
                        variableCounter++;
                        altitude = decisions.get(String.format("HomoAltitude%d", i));
                        inclination = decisions.get(String.format("HomoInclination%d", i));
                        nsats = decisions.get(String.format("HomoNumberSatellites%d", i));
                        nplanes = decisions.get(String.format("HomoNumberPlanes%d", i));
                        satellite = decisions.get(String.format("HomoSatellite%d", i));
                        sol.setVariable(variableCounter, new HomogeneousWalkerVariable((List<Double>)altitude.getAllowedValues(),
                                (List<Object>)inclination.getAllowedValues(),
                                (List<Integer>)nsats.getAllowedValues(),
                                (List<Integer>)nplanes.getAllowedValues(),
                                (List<Satellite>)satellite.getAllowedValues(),
                                properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(i).isSecondaryPayload(),
                                properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(i).getOrbit().get(0).getEccentricity()));
                        break;
                    case "DELTA_HETEROGENEOUS":
                        variableCounter++;
                        altitude = decisions.get(String.format("HeteroAltitude%d", i));
                        inclination = decisions.get(String.format("HeteroInclination%d", i));
                        nsats = decisions.get(String.format("HeteroNumberSatellites%d", i));
                        nplanes = decisions.get(String.format("HeteroNumberPlanes%d", i));
                        satellite = decisions.get(String.format("HeteroSatellite%d", i));
                        sol.setVariable(variableCounter, new HeterogeneousWalkerVariable((List<Double>)altitude.getAllowedValues(),
                                (List<Object>)inclination.getAllowedValues(),
                                (List<Integer>)nsats.getAllowedValues(),
                                (List<Integer>)nplanes.getAllowedValues(),
                                (List<Satellite>)satellite.getAllowedValues(),
                                properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(i).isSecondaryPayload(),
                                properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(i).getOrbit().get(0).getEccentricity()));
                        break;
                    case "TRAIN":
                        variableCounter++;
                        altitude = decisions.get(String.format("TrainAltitude%d", i));
                        nsats = decisions.get(String.format("TrainNumberSatellites%d", i));
                        ltans = decisions.get(String.format("TrainLTANs%d", i));
                        satIntervals = decisions.get(String.format("TrainSatelliteInterval%d", i));
                        satellite = decisions.get(String.format("TrainSatellite%d", i));
                        sol.setVariable(variableCounter, new TrainVariable((List<Double>)altitude.getAllowedValues(),
                                (List<Integer>)nsats.getAllowedValues(),
                                (List<String>)ltans.getAllowedValues(),
                                (List<String>)satIntervals.getAllowedValues(),
                                (List<Satellite>)satellite.getAllowedValues(),
                                properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(i).isSecondaryPayload(),
                                properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(i).getOrbit().get(0).getEccentricity()));
                        break;
                    case "AD_HOC":
                        variableCounter++;
                        nsats = decisions.get(String.format("AdhocNumberSatellites%d", i));
                        satellite = decisions.get(String.format("AdhocSatellite%d", i));
                        sol.setVariable(variableCounter, new AdHocVariable((List<Integer>)nsats.getAllowedValues(),
                                (List<Satellite>)satellite.getAllowedValues(),
                                properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(i).isSecondaryPayload()));
                        break;
                    case "STRING_OF_PEARLS":
                        variableCounter++;
                        altitude = decisions.get(String.format("PearlAltitude%d", i));
                        inclination = decisions.get(String.format("PearlInclination%d", i));
                        nsats = decisions.get(String.format("PearlNumberSatellites%d", i));
                        raans = decisions.get(String.format("PearlRAAN%d", i));
                        satIntervals = decisions.get(String.format("PearlSatelliteInterval%d", i));
                        satellite = decisions.get(String.format("PearlSatellite%d", i));
                        sol.setVariable(variableCounter, new StringOfPearlsVariable((List<Double>)altitude.getAllowedValues(),
                                (List<Object>)inclination.getAllowedValues(),
                                (List<Integer>)nsats.getAllowedValues(),
                                (List<Double>)raans.getAllowedValues(),
                                (List<String>)satIntervals.getAllowedValues(),
                                (List<Satellite>)satellite.getAllowedValues(),
                                properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(i).isSecondaryPayload(),
                                properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(i).getOrbit().get(0).getEccentricity()));
                        break;
                    case "GENERAL":
                        variableCounter++;
                        Decision<?> nSatBound = decisions.get(String.format("nSatBounds%d", i));
                        Decision<?> smaBound = decisions.get(String.format("altitudeBounds%d", i));
                        Decision<?> incBound = decisions.get(String.format("inclinationBounds%d", i));
                        //satellite = decisions.get(String.format("generalSatellite%d", i));
                        sol.setVariable(variableCounter, new ConstellationVariable(new Bounds<>(((List<Integer>)nSatBound.getAllowedValues()).get(0),((List<Integer>)nSatBound.getAllowedValues()).get(1)),
                                new Bounds<>(((List<Double>)smaBound.getAllowedValues()).get(0),((List<Double>)smaBound.getAllowedValues()).get(1)),
                                new Bounds<>(0.0, 0.0),
                                new Bounds<>(((List<Double>)incBound.getAllowedValues()).get(0),((List<Double>)incBound.getAllowedValues()).get(1)),
                                new Bounds<>(0.0, 0.0),
                                new Bounds<>(0.0, 2 * Math.PI),
                                new Bounds<>(0.0, 2 * Math.PI)));
                        break;
                    default:
                        throw new IllegalArgumentException("Constellation type has to be either DELTA_HOMOGENEOUS, DELTA_HETEROGENOUS, TRAIN OR AD_HOC.");
                }
            }

        }
        return sol;
    }
}
