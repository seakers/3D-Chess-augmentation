#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""Object models and methods for tradespace search analysis.
"""

import json

from .util import Entity, EnumEntity
from .mission import MissionConcept, DesignSpace

class SearchStrategy(EnumEntity):
    """Enumeration of recognized search strategies.

    :var FF: Full-factorial.
    :type FF: SearchStrategy
    :varvar GA: Genetic algorithm.
    :vartype GA: SearchStrategy
    :var KDO: Knowledge-driven optimization.
    :vartype KDO: SearchStrategy
    """
    FF = "FF"
    GA = "GA"
    KDO = "KDO"

class SearchParameters(Entity):
    """Aggregates search parameters needed to set up the genetic algorithm.

    :param maxNFE: Maximum number of function evaluations.
    :type maxNFE: int
    :param populationSize: Size of the initial population of solutions.
    :type populationSize: int
    :param epsilons: List of epsilons for the dominance archive (one per objective).
    :type epsilons: list[float]
    :param sizeTournament: Size of the tournament selection.
    :type sizeTournament: int
    :param pCrossover: Probability of crossover.
    :type pCrossover: float
    :param pMutation: Probability of mutation.
    :type pMutation: float
    :param alpha: Learning rate parameter for credit updates in adaptive operator selection.
    :type alpha: float
    :param beta: Learning rate paraemter for probability updates in adaptive operator selection.
    :type beta: float
    :param pMin:  Minimum probability of selection for adaptive operator selection.
    :type pMin: float
    :param iOperators: List of domain-independent operators.
    :type iOperators: list[str]
    :param dOperators: List of domain-dependent operators.
    :type dOperators: list[str]
    :param nfeTriggerDM: Number of evaluations between successive rule mining algorithm applications.
    :type nfeTriggerDM: int
    :param nOperRepl: Number of operators to replace after each rule mining.
    :type nOperRepl: int
    """
    def __init__(self, maxNFE=None, populationSize=None, epsilons=None,
            sizeTournament=None, pCrossover=None, pMutation=None, alpha=None,
            beta=None, pMin=None, iOperators=None, dOperators=None,
            nfeTriggerDM=None, nOperRepl=None, _id=None):
        self.maxNFE = maxNFE
        self.populationSize = populationSize
        self.epsilons = epsilons
        self.sizeTournament = sizeTournament
        self.pCrossover = pCrossover
        self.pMutation = pMutation
        self.alpha = alpha
        self.beta = beta
        self.pMin = pMin
        self.iOperators = iOperators
        self.dOperators = dOperators
        self.nfeTriggerDM = nfeTriggerDM
        self.nOperRepl = nOperRepl
        super(SearchParameters,self).__init__(_id, "SearchParameters")

    @staticmethod
    def from_dict(d):
        """Parses search parameters from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the search parameters
        :rtype: SearchParameters
        """
        return SearchParameters(
            maxNFE = d.get("maxNFE", None),
            populationSize = d.get("populationSize", None),
            epsilons = d.get("epsilons", None),
            sizeTournament = d.get("sizeTournament", None),
            pCrossover = d.get("pCrossover", None),
            pMutation = d.get("pMutation", None),
            alpha = d.get("alpha", None),
            beta = d.get("beta", None),
            pMin = d.get("pMin", None),
            iOperators = d.get("iOperators", None),
            dOperators = d.get("dOperators", None),
            nfeTriggerDM = d.get("nfeTriggerDM", None),
            nOperRepl = d.get("nOperRepl", None),
            _id = d.get("@id", None)
        )

class AnalysisOutputs(Entity):
    """Configuration options to filter analysis outputs based on ranges of parameters.

    :param obsTimeStep: Desired time step to record spacecraft state \
    observations. True uses minimum simulation time step. False toggles \
    outputs off. (default: True)
    :type obsTimeStep: bool
    :param orbitsGlobal: Optionally toggles orbits module outputs for global \
    (per target region) coverage metrics (default: None, equivalent to True).
    :type orbitsGlobal: bool
    :param orbitsLocal: Optionally toggles orbits module outputs for local \
    (per point of interest) metrics (default: None, equivalent to True).
    :type orbitsLocal: bool
    :param orbitsStates: Optionally toggles orbits module outputs for states \
    (spacecraft state variables) metrics (default: None, equivalent to True).
    :type orbitsStates: bool
    :param orbitsAccess: Optionally toggles orbits module outputs for access \
    (per point of interest) metrics (default: None, equivalent to False).
    :type orbitsAccess: bool
    :param orbitsInstrumentAccess: Optionally toggles orbits module outputs \
    for instrument access (per point of interset) metrics (default: None, \
    equivalent to True).
    :type orbitsInstrumentAccess: bool
    :param keepLowLevelData: Optionally preserve low-level data outputs \
    including satellite access information, satellite state varibles, and \
    level 0 instrument data metrics (default: None, equivalent to False). \
    :type keepLowLevelData: bool
    """
    def __init__(self, obsTimeStep=True, orbitsGlobal=None, orbitsLocal=None,
            orbitsStates=None, orbitsAccess=None, orbitsInstrumentAccess=None,
            keepLowLevelData=None, _id=None):
        self.obsTimeStep = obsTimeStep
        self.orbitsGlobal = orbitsGlobal
        self.orbitsLocal = orbitsLocal
        self.orbitsStates = orbitsStates
        self.orbitsAccess = orbitsAccess
        self.orbitsInstrumentAccess = orbitsInstrumentAccess
        self.keepLowLevelData = keepLowLevelData
        super(AnalysisOutputs,self).__init__(_id, "AnalysisOutputs")

    def to_dict(self):
        """Convert this entity to a JSON-formatted dictionary.

        :return: the JSON-formatted dictionary
        :rtype: dict
        """
        json_dict = super(AnalysisOutputs, self).to_dict()
        # translate special python to json keys: e.g. orbitsGlobal to orbits.global
        if "orbitsGlobal" in json_dict:
            json_dict["orbits.global"] = json_dict.pop("orbitsGlobal")
        if "orbitsLocal" in json_dict:
            json_dict["orbits.local"] = json_dict.pop("orbitsLocal")
        if "orbitsStates" in json_dict:
            json_dict["orbits.states"] = json_dict.pop("orbitsStates")
        if "orbitsAccess" in json_dict:
            json_dict["orbits.access"] = json_dict.pop("orbitsAccess")
        if "orbitsInstrumentAccess" in json_dict:
            json_dict["orbits.instrumentAccess"] = json_dict.pop("orbitsInstrumentAccess")
        return json_dict

    @staticmethod
    def from_dict(d):
        """Parses analysis outputs from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the analysis outputs
        :rtype: AnalysisOutputs
        """
        return AnalysisOutputs(
            obsTimeStep = d.get("obsTimeStep", True),
            orbitsGlobal = d.get("orbits.global", None),
            orbitsLocal = d.get("orbits.local", None),
            orbitsStates = d.get("orbits.states", None),
            orbitsAccess = d.get("orbits.access", None),
            orbitsInstrumentAccess = d.get("orbits.instrumentAccess", None),
            keepLowLevelData = d.get("keepLowLevelData", None),
            _id = d.get("@id", None)
        )

class AnalysisSettings(Entity):
    """Configuration options specific to TAT-C analysis tool.

    :param includePropulsion: Toggles satellite propulsion. True mitigates \
    effects of drag. False triggers drag effects. (default: True)
    :type includePropulsion: bool
    :param outputs: Set of intermediate or internal outputs to toggle on or \
    off or specify bounds.
    :type outputs: AnalysisOutputs
    :param searchStrategy: Specifies preferences for the search (default: FF). \
    Recognized case-insensitive values include: FF (full factorial), \
    GA (genetic algorithm), KDO (knowledge-driven optimization).
    :type searchStrategy: str or SearchStrategy
    :param searchParameters: Parameters for the intelligent search strategy.
    :type searchParameters: str
    :param proxyInstrument: flag to use a proxy instrument model (default: None, equivalent to False).
    :type proxyInstrument: bool
    :param proxyCostRisk: flag to use a proxy cost and risk model (default: None, equivalent to False).
    :type proxyCostRisk: bool
    :param proxyLaunch: flag to use a proxy launch model (default: None, equivalent to False).
    :type proxyLaunch: bool
    :param proxyOrbits: flag to use a proxy orbits model (default: None, equivalent to False).
    :type proxyOrbits: bool
    :param proxyValue: flag to use a proxy value model (default: None, equivalent to False).
    :type proxyValue: bool
    :param useCache: Flag to use a cache to improve computational performance (default: True).
    :type useCache: bool
    :param maxCacheSize: Set the maximum cache size in gigabytes (default: None, no limit).
    :type maxCacheSize: float
    :param overrideLaunchDatabase: Toggle to either override (True) or append \
    (False) to the launch vehicle database (default: None, equivalent to False).
    :type overrideLaunchDatabase: bool
    :param minLaunchReliability: Minimum launch reliability (probability \
    between 0.0 and 1.0) required for launch vehicle selection (default: None, equivalent to 0.0).
    :type minLaunchReliability: float
    :param maxGridSize: Maximum number of grid points to generate for \
    coverage analysis (default: 10,000).
    :type maxGridSize: int
    """

    def __init__(self, includePropulsion=True, outputs=AnalysisOutputs(), searchStrategy="FF",
            searchParameters=None, proxyInstrument=None, proxyCostRisk=None,
            proxyLaunch=None, proxyOrbits=None, proxyValue=None,
            useCache=True, maxCacheSize=None, overrideLaunchDatabase=None,
            minLaunchReliability=None, maxGridSize=10000, _id=None):
        self.includePropulsion = includePropulsion
        self.outputs = outputs
        self.searchStrategy = SearchStrategy.get(searchStrategy)
        self.searchParameters = searchParameters
        self.proxyInstrument = proxyInstrument #FIXME temporary for development
        self.proxyCostRisk = proxyCostRisk #FIXME temporary for development
        self.proxyLaunch = proxyLaunch #FIXME temporary for development
        self.proxyOrbits = proxyOrbits #FIXME temporary for development
        self.proxyValue = proxyValue #FIXME temporary for development
        self.useCache = useCache
        self.maxCacheSize = maxCacheSize
        self.overrideLaunchDatabase = overrideLaunchDatabase
        self.minLaunchReliability = minLaunchReliability
        self.maxGridSize = maxGridSize
        super(AnalysisSettings,self).__init__(_id, "AnalysisSettings")

    @staticmethod
    def from_dict(d):
        """Parses analysis settings from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :returns: the analysis settings
        :rtype: AnalysisSettings
        """
        return AnalysisSettings(
                includePropulsion = d.get("includePropulsion", True),
                outputs = AnalysisOutputs.from_json(d.get("outputs", AnalysisOutputs())),
                searchStrategy = d.get("searchStrategy", "FF"),
                searchParameters = SearchParameters.from_json(d.get("searchParameters", None)),
                proxyInstrument = d.get("proxyInstrument", None), #FIXME temporary for development
                proxyCostRisk = d.get("proxyCostRisk", None), #FIXME temporary for development
                proxyLaunch = d.get("proxyLaunch", None), #FIXME temporary for development
                proxyOrbits = d.get("proxyOrbits", None), #FIXME temporary for development
                proxyValue = d.get("proxyValue", None), #FIXME temporary for development
                useCache = d.get("useCache", True),
                maxCacheSize = d.get("maxCacheSize", None),
                overrideLaunchDatabase = d.get("overrideLaunchDatabase", None),
                minLaunchReliability = d.get("minLaunchReliability", None),
                maxGridSize = d.get("maxGridSize", 10000),
                _id = d.get("@id", None)
            )

class TradespaceSearch(Entity):
    """Set of constraints and parameters to bound and define a tradespace search.

    :param mission: Context and objectives for mission.
    :type mission: Mission
    :param designSpace: Constraints and requirements for available architectures.
    :type designSpace: DesignSpace
    :param settings: Settings specific to TAT-C analysis.
    :type settings: AnalysisSettings
    """

    def __init__(self, mission=MissionConcept(), designSpace=DesignSpace(), settings=AnalysisSettings(), _id=None):
        """Initialize a tradespace search object.
        """
        self.mission = mission
        self.designSpace = designSpace
        self.settings = settings
        super(TradespaceSearch,self).__init__(_id, "TradespaceSearch")

    @staticmethod
    def from_dict(d):
        """Parses a tradespace search from a normalized JSON dictionary.

        :param d: the JSON dictionary.
        :type d: dict
        :param d: the JSON dictionary.
        :type d: dict
        :returns: the tradespace search
        :rtype: TradespaceSearch
        """
        return TradespaceSearch(
                mission = MissionConcept.from_json(d.get("mission", MissionConcept())),
                designSpace = DesignSpace.from_json(d.get("designSpace", DesignSpace())),
                settings = AnalysisSettings.from_json(d.get("settings", AnalysisSettings())),
                _id = d.get("@id", None)
            )
