import os
import sys
import socket
import json
import pprint
import redis
import pickle
import pandas               as pd
import plotly.graph_objs    as go
import numpy                as np

# GLOBALS
value_file_name = '/value_output.json'
global_file_name = '/gbl.json'
cost_file_name = '/CostRisk_Output.json'

cost_objectives = ["groundCost",
                   "hardwareCost",
                   "iatCost",
                   "launchCost",
                   "lifecycleCost",
                   "nonRecurringCost",
                   "operationsCost",
                   "programCost",
                   "recurringCost"]

orbital_objectives = ["MinTime",
                      "MaxTime",
                      "MinTimeToCoverage",
                      "MaxTimeToCoverage",
                      "MeanTimeToCoverage",
                      "MinAccessTime",
                      "MaxAccessTime",
                      "MeanAccessTime",
                      "MinRevisitTime",
                      "MaxRevisitTime",
                      "MeanRevisitTime",
                      "MinResponseTime",
                      "MeanResponseTime",
                      "Coverage",
                      "MinNumOfPOIpasses",
                      "MaxNumOfPOIpasses",
                      "MeanNumOfPOIpasses",
                      "MaxDataLatency",
                      "MeanDataLatency",
                      "NumGSpassesPD",
                      "TotalDownlinkTimePD",
                      "MinDownlinkTimePerPass",
                      "MaxDownlinkTimePerPass",
                      "MeanDownlinkTimePerPass"]

instrument_objectives = ["Mean of Mean of Incidence angle [deg]",
                         "SD of Mean of Incidence angle [deg]",
                         "Mean of SD of Incidence angle [deg]",
                         "Mean of Mean of Look angle [deg]",
                         "SD of Mean of Look angle [deg]",
                         "Mean of SD of Look angle [deg]",
                         "Mean of Mean of Observation Range [km]",
                         "SD of Mean of Observation Range [km]",
                         "Mean of SD of Observation Range [km]",
                         "Mean of Mean of Noise-Equivalent delta T [K]",
                         "SD of Mean of Noise-Equivalent delta T [K]",
                         "Mean of SD of Noise-Equivalent delta T [K]",
                         "Mean of Mean of DR",
                         "SD of Mean of DR",
                         "Mean of SD of DR",
                         "Mean of Mean of SNR",
                         "SD of Mean of SNR",
                         "Mean of SD of SNR",
                         "Mean of Mean of Ground Pixel Along-Track Resolution [m]",
                         "SD of Mean of Ground Pixel Along-Track Resolution [m]",
                         "Mean of SD of Ground Pixel Along-Track Resolution [m]",
                         "Mean of Mean of Ground Pixel Cross-Track Resolution [m]",
                         "SD of Mean of Ground Pixel Cross-Track Resolution [m]",
                         "Mean of SD of Ground Pixel Cross-Track Resolution [m]",
                         "Mean of Mean of Swath-Width [m]",
                         "SD of Mean of Swath-Width [m]",
                         "Mean of SD of Swath-Width [m]",
                         "Mean of Mean of Sigma NEZ Nought [dB]",
                         "SD of Mean of Sigma NEZ Nought [dB]",
                         "Mean of SD of Sigma NEZ Nought [dB]"]

value_objectives = ["Total Architecture Value [Mbits]",
                    "Total Data Collected [Mbits]",
                    "EDA Scaling Factor"]


time_objectives = ["MinTime",
                   "MaxTime",
                   "MinTimeToCoverage",
                   "MaxTimeToCoverage",
                   "MeanTimeToCoverage",
                   "MinAccessTime",
                   "MaxAccessTime",
                   "MeanAccessTime",
                   "MinRevisitTime",
                   "MaxRevisitTime",
                   "MeanRevisitTime",
                   "MinResponseTime",
                   "MeanResponseTime",
                   "MaxDataLatency",
                   "MeanDataLatency",
                   "TotalDownlinkTimePD",
                   "MinDownlinkTimePerPass",
                   "MaxDownlinkTimePerPass",
                   "MeanDownlinkTimePerPass"]  # --> All initially in seconds

data_objectives = ["Total Architecture Value [Mbits]",
                   "Total Data Collected [Mbits]"]  # --> All initially in Mbits



# --> This function maps an objective to that objective's position in the architecture files
def objective_to_tokens(objective):
    # ----------------------------------
    # ------------> Global <------------
    # ----------------------------------
    if (objective == "MinTime"):
        return ["global", "Time", "min"]
    elif (objective == "MaxTime"):
        return ["global", "Time", "max"]
    elif (objective == "MinTimeToCoverage"):
        return ["global", "TimeToCoverage", "min"]
    elif (objective == "MaxTimeToCoverage"):
        return ["global", "TimeToCoverage", "max"]
    elif (objective == "MeanTimeToCoverage"):
        return ["global", "TimeToCoverage", "avg"]
    elif (objective == "MinAccessTime"):
        return ["global", "AccessTime", "min"]
    elif (objective == "MaxAccessTime"):
        return ["global", "AccessTime", "max"]
    elif (objective == "MeanAccessTime"):
        return ["global", "AccessTime", "avg"]
    elif (objective == "MinRevisitTime"):
        return ["global", "RevisitTime", "min"]
    elif (objective == "MaxRevisitTime"):
        return ["global", "RevisitTime", "max"]
    elif (objective == "MeanRevisitTime"):
        return ["global", "RevisitTime", "avg"]
    elif (objective == "MinResponseTime"):
        return ["global", "ResponseTime", "min"]
    elif (objective == "MeanResponseTime"):
        return ["global", "ResponseTime", "avg"]
    elif (objective == "Coverage"):
        return ["global", "Coverage"]
    elif (objective == "MinNumOfPOIpasses"):
        return ["global", "NumOfPOIpasses", "min"]
    elif (objective == "MaxNumOfPOIpasses"):
        return ["global", "NumOfPOIpasses", "max"]
    elif (objective == "MeanNumOfPOIpasses"):
        return ["global", "NumOfPOIpasses", "avg"]
    elif (objective == "MaxDataLatency"):
        return ["global", "DataLatency", "max"]
    elif (objective == "MeanDataLatency"):
        return ["global", "DataLatency", "avg"]
    elif (objective == "NumGSpassesPD"):
        return ["global", "NumGSpassesPD"]
    elif (objective == "TotalDownlinkTimePD"):
        return ["global", "TotalDownlinkTimePD"]
    elif (objective == "MinDownlinkTimePerPass"):
        return ["global", "DownlinkTimePerPass", "min"]
    elif (objective == "MaxDownlinkTimePerPass"):
        return ["global", "DownlinkTimePerPass", "max"]
    elif (objective == "MeanDownlinkTimePerPass"):
        return ["global", "DownlinkTimePerPass", "avg"]

    # --------------------------------
    # ------------> Cost <------------
    # --------------------------------
    elif (objective == "groundCost"):
        return ["cost", "groundCost", "estimate"]
    elif (objective == "hardwareCost"):
        return ["cost", "hardwareCost", "estimate"]
    elif (objective == "iatCost"):
        return ["cost", "iatCost", "estimate"]
    elif (objective == "launchCost"):
        return ["cost", "launchCost", "estimate"]
    elif (objective == "lifecycleCost"):
        return ["cost", "lifecycleCost", "estimate"]
    elif (objective == "nonRecurringCost"):
        return ["cost", "nonRecurringCost", "estimate"]
    elif (objective == "operationsCost"):
        return ["cost", "operationsCost", "estimate"]
    elif (objective == "programCost"):
        return ["cost", "programCost", "estimate"]
    elif (objective == "recurringCost"):
        return ["cost", "recurringCost", "estimate"]

    # ---------------------------------
    # ------------> Value <------------
    # ---------------------------------
    elif (objective == "Total Architecture Value [Mbits]"):
        return ["value", "Total Architecture Value [Mbits]"]
    elif (objective == "Total Data Collected [Mbits]"):
        return ["value", "Total Data Collected [Mbits]"]
    elif (objective == "EDA Scaling Factor"):
        return ["value", "EDA Scaling Factor"]


# --> Based on objective and value, return a pair --> (converted_val, units)
# --> Time units - inputs always in seconds --> Hours or Days
# --> Data units - inputs always in Megabits --> Gigabits
def convert_value(value, objective, *args):
    converted = float(value)
    units = ''

    if objective in time_objectives:
        if converted > 86400:
            converted = converted / 86400.0  # --> Days
            units = 'Days'
        elif converted > 3600:
            converted = converted / 3600.0  # --> Hours
            units = 'Hours'
        else:
            units = 'Seconds'
    if objective in data_objectives:
        if converted > 1000000:
            converted = converted / 1000000.0  # --> Tbits
            units = 'Tbits'
        elif converted > 1000:
            converted = converted / 1000.0  # --> Gbits
            units = 'Gbits'
        else:
            units = 'Mbits'
    if objective in cost_objectives:
        if converted > 1000000:
            converted = converted / 1000000.0  # --> Trillions
            units = 'Trillions'
        elif converted > 1000:
            converted = converted / 1000.0  # --> Billions
            units = 'Billions'

    return str(converted), units


# --> Used by Overview Tab
class RedisInterface:

    def __init__(self, path):
        self.path = path
        self.mission_file = path + '/mission.json'
        self.cache = redis.Redis(host='redis', port=6379)

        # --> Clear the database before re-indexing
        self.cache.flushdb()
        self.arch_list = []
        self.index_database()


    def flush_database(self):
        self.cache.flushdb()
        self.arch_list = []
        self.index_database()


    # --> Each architecture "arch-x" has 3 elements in the database
    # --> "arch-x-gbl"  - holds gbl.json
    # --> "arch-x-val"  - holds value_output.json
    # --> "arch-x-cost" - holds CostRisk_Output.json
    def index_database(self):
        for subdir, dirs, files in os.walk(self.path):
            dirs = [x for x in dirs if "arch" in x]
            for d in sorted(dirs, key=dir_sort):
                archdir = self.path + '/' + str(d)
                if (os.path.exists(archdir + global_file_name) and os.path.exists(
                        archdir + value_file_name) and os.path.exists(archdir + cost_file_name)):
                    self.arch_list.append(str(d))
                    # Global File
                    gbl_file = open(str(archdir + global_file_name))
                    gbl_data_json = json.load(gbl_file)
                    gbl_data = pickle.dumps(gbl_data_json)
                    self.cache.set(str(d) + '-gbl', gbl_data)
                    # Value File
                    val_file = open(str(archdir + value_file_name))
                    val_data_json = json.load(val_file)
                    val_data = pickle.dumps(val_data_json)
                    self.cache.set(str(d) + '-val', val_data)
                    # Cost File
                    cost_file = open(str(archdir + cost_file_name))
                    cost_data_json = json.load(cost_file)
                    cost_data = pickle.dumps(cost_data_json)
                    self.cache.set(str(d) + '-cost', cost_data)

    # --> Check for new arch-y directories in the mission directory
    # --> For every new arch-y, create an entry in the database
    def update_database(self):
        new_dirs = []
        for subdir, dirs, files in os.walk(self.path):
            dirs = [x for x in dirs if "arch" in x]
            for d in sorted(dirs, key=dir_sort):
                if (d not in self.arch_list):
                    new_dirs.append(d)
        for d in new_dirs:
            if (os.path.exists('/mission/' + d + global_file_name) and os.path.exists(
                    '/mission/' + d + value_file_name) and os.path.exists('/mission/' + d + cost_file_name)):
                self.arch_list.append(str(d))
                # Global File
                gbl_file = open(str('/mission/' + d + global_file_name))
                gbl_data_json = json.load(gbl_file)
                gbl_data = pickle.dumps(gbl_data_json)
                self.cache.set(str(d) + '-gbl', gbl_data)
                # Value File
                val_file = open(str('/mission/' + d + value_file_name))
                val_data_json = json.load(val_file)
                val_data = pickle.dumps(val_data_json)
                self.cache.set(str(d) + '-val', val_data)
                # Cost File
                cost_file = open(str('/mission/' + d + cost_file_name))
                cost_data_json = json.load(cost_file)
                cost_data = pickle.dumps(cost_data_json)
                self.cache.set(str(d) + '-cost', cost_data)

    # --> Overview chart calls this function for data
    def query_global_data(self, x_data, y_data, marker_color=None, marker_size=None):
        x_datalist = []
        y_datalist = []
        marker_colorlist = []
        marker_sizelist = []
        return_arches = self.arch_list

        # --> Check if our objectives to plot are in the list of objectives
        if x_data not in cost_objectives and x_data not in orbital_objectives and x_data not in value_objectives:
            x_data = None
            return_arches = []
        if y_data not in cost_objectives and y_data not in orbital_objectives and y_data not in value_objectives:
            y_data = None
            return_arches = []
        if marker_color not in cost_objectives and marker_color not in orbital_objectives and marker_color not in value_objectives:
            marker_color = None
        if marker_size not in cost_objectives and marker_size not in orbital_objectives and marker_size not in value_objectives:
            marker_size = None

        # --> Get the data for each of the architectures we have indexed
        units_list = []
        for arch in return_arches:
            # --> x_data parsing
            if x_data is not None:
                tokens = objective_to_tokens(x_data)
                temp_data = float(self.query_arch(arch, x_data, *tokens))
                x_datalist.append(temp_data)

            # --> y_data parsing
            if y_data is not None:
                tokens = objective_to_tokens(y_data)
                temp_data = float(self.query_arch(arch, y_data, *tokens))
                y_datalist.append(temp_data)

            # --> marker_color parsing
            if marker_color is not None:
                tokens = objective_to_tokens(marker_color)
                temp_data = float(self.query_arch(arch, marker_color, *tokens))
                marker_colorlist.append(temp_data)

            # --> marker_size parsing
            if marker_size is not None:
                tokens = objective_to_tokens(marker_size)
                temp_data = float(self.query_arch(arch, marker_size, *tokens))
                marker_sizelist.append(temp_data)

        return return_arches, x_datalist, y_datalist, marker_colorlist, marker_sizelist

    # --> metric_type will either be: global, value, or cost
    # --> Here we will convert values to something readable
    # --> seconds --> hours
    def query_arch(self, arch, objective, *args):
        if args[0] != 'global' and args[0] != 'value' and args[0] != 'cost':
            print("Invalid metric type passed to query_global in RedisInterface", args[0])
            return None

        addon = ''
        if args[0] == 'global':
            addon = '-gbl'
        elif args[0] == 'value':
            addon = '-val'
        elif args[0] == 'cost':
            addon = '-cost'

        # --> This is the name of the key in the redis database
        base_query = arch + addon

        # --> Get information from the redis database and load into json format
        data = self.cache.get(base_query)
        parser = pickle.loads(data)

        # Parse function arguments
        to_return = 0
        if len(args) == 2:
            to_return = parser[args[1]]
        elif len(args) == 3:
            to_return = parser[args[1]][args[2]]
        elif len(args) == 4:
            to_return = parser[args[1]][args[2]][args[3]]
        elif len(args) == 5:
            to_return = parser[args[1]][args[2]][args[3]][args[4]]
        else:
            to_return = parser[args]

        # converted_tuple = convert_value(to_return, objective, *args)
        # to_return = converted_tuple[0]  # --> Value
        # units = converted_tuple[1]  # --> Units

        return to_return


def dir_sort(x):
    return float(x[5:])


# This class will hold all the architecture interfaces in a mission directory
class TradespaceInterface:

    def __init__(self, path):  # Pass the path to the mission directory
        self.path = path  # This will always be: '/mission'
        self.mission_file = path + '/mission.json'
        self.arch_interfaces = []
        self.update_interfaces()

    # Validate the tradespace (at least 1 completly evaluated architecture)
    def is_valid_tradespace(self):
        if (self.arch_interfaces == []):
            return False
        else:
            return True

    # Update all the architecture interfaces in the class
    def update_interfaces(self):
        self.arch_interfaces = []
        for subdir, dirs, files in os.walk(self.path):
            dirs = [x for x in dirs if "arch" in x]
            for d in sorted(dirs, key=dir_sort):
                archdir = self.path + '/' + str(d)
                temp = ArchitectureInterface(archdir)
                if (temp.is_valid()):
                    self.arch_interfaces.append(temp)

    # Get all of the satellite info for one architecture
    def get_arch_satellite_info(self, arch):
        arch = self.get_architecture(arch)
        if (arch is None):
            return None
        sat_list = arch.get_satellite_info()
        return sat_list

    # Gets satellite information by ID
    def get_satellite_by_id(self, arch, id_sat):
        arch = self.get_architecture(arch)
        if (arch is None):
            return None
        sat_list = arch.get_satellite_info()
        for sat in sat_list:
            if (sat['@id'] == id_sat):
                return sat
        return None

    # Get any value in the mission.json file
    def get_mission_info(self, category, param1=None, param2=None, param3=None, param4=None, param5=None):
        parser = pd.read_json(self.mission_file)
        if (param1 == None and param2 == None and param3 == None and param4 == None and param5 == None):
            return parser[category]
        elif (param2 == None and param3 == None and param4 == None and param5 == None):
            return parser[category][param1]
        elif (param3 == None and param4 == None and param5 == None):
            return parser[category][param1][param2]
        elif (param4 == None and param5 == None):
            return parser[category][param1][param2][param3]
        elif (param5 == None):
            return parser[category][param1][param2][param3][param4]
        else:
            return parser[category][param1][param2][param3][param4][param5]

    # Get an architecture interface given the name
    def get_architecture(self, arch_name):
        for arch in self.arch_interfaces:
            if (arch_name == arch.get_name()):
                return arch
        return None

    # Get min or max of all the globals in the mission
    def get_min_global(self, category, value=None):
        values = []
        for arch in self.arch_interfaces:
            values.append(arch.get_global(category, value))
        return min(values)

    def get_max_global(self, category, value=None):
        values = []
        for arch in self.arch_interfaces:
            values.append(arch.get_global(category, value))
        return max(values)

    # Param 1: name of architecture to get data on
    # Param 2: metric type to get data on
    def get_arch_heatmap_data(self, arch, metric_type):
        analysis_arch = self.get_architecture(arch)
        if (analysis_arch is None):
            return None
        latitudes = analysis_arch.get_local('lat')
        longitudes = analysis_arch.get_local('lon')
        point_data = analysis_arch.get_local(metric_type)
        cmin = 0
        cmax = 0

        if (metric_type == 'ATavg'):
            cmin = self.get_min_global('AccessTime', 'avg')
            cmax = self.get_max_global('AccessTime', 'avg')
        elif (metric_type == 'ATmin'):
            cmin = self.get_min_global('AccessTime', 'min')
            cmax = self.get_max_global('AccessTime', 'avg')
        elif (metric_type == 'ATmax'):
            cmin = self.get_min_global('AccessTime', 'avg')
            cmax = self.get_max_global('AccessTime', 'max')
        elif (metric_type == 'RvTavg'):
            cmin = self.get_min_global('RevisitTime', 'avg')
            cmax = self.get_max_global('RevisitTime', 'avg')
        elif (metric_type == 'RvTmin'):
            cmin = self.get_min_global('RevisitTime', 'min')
            cmax = self.get_max_global('RevisitTime', 'avg')
        elif (metric_type == 'RvTmax'):
            cmin = self.get_min_global('RevisitTime', 'avg')
            cmax = self.get_max_global('RevisitTime', 'max')
        elif (metric_type == 'TCcov'):
            cmin = self.get_min_global('TimeToCoverage', 'avg')
            cmax = self.get_max_global('TimeToCoverage', 'avg')
        elif (metric_type == 'numPass'):
            cmin = self.get_min_global('NumOfPOIpasses', 'avg')
            cmax = self.get_max_global('NumOfPOIpasses', 'avg')

        return latitudes, longitudes, point_data, cmin, cmax


# This class will interact with an architecture directory and retrieve information about it
class ArchitectureInterface:

    def __init__(self, path):
        self.path = path
        self.global_path = path + '/gbl.json'
        self.local_path = path + '/lcl.csv'
        self.cost_path = path + '/CostRisk_Output.json'
        self.arch_path = path + '/arch.json'
        self.level_2_path = path + '/level2_data_metrics.csv'
        self.level_1_path = path + '/level1_data_metrics.csv'
        self.value_path = path + '/value_output.json'

        last_index = path.rfind('/') + 1
        self.name = path[(last_index):]

    # Determines if a path to an architecture is valid or not, if it is --> add it to the list of ArchitectureInterfaces
    def is_valid(self):
        if not (os.path.isdir(self.path)):
            return False
        if not (os.path.isfile(self.global_path)):
            return False
        if not (os.path.isfile(self.local_path)):
            return False
        if not (os.path.isfile(self.cost_path)):
            return False
        return True

    def has_local_file(self):
        if not (os.path.isfile(self.local_path)):
            return False
        return True

    def get_global(self, category, value=None):
        parser = pd.read_json(self.global_path)
        if (value == None):
            return parser[category]
        else:
            return parser[category][value]

    def get_cost(self, category, param1=None, param2=None):
        parser = json.load(open(self.cost_path))
        if (param1 == None and param2 == None):
            return parser[category]
        elif (param1 != None and param2 == None):
            return parser[category][param1]
        else:
            return parser[category][param1][param2]

    def get_level_2_data(self, category):
        parser = pd.read_csv(self.level_2_path)
        return parser.at[0, category]

    def get_level_1_data(self, column):
        parser = pd.read_csv(self.level_1_path)
        return_data = parser[column].values
        return return_data

    def get_value(self, category):
        parser = json.load(open(self.value_path))
        return parser[category]

    def get_arch_info(self, category, param1=None, param2=None, param3=None, param4=None, param5=None):
        parser = pd.read_json(self.arch_path)
        if (param1 == None and param2 == None and param3 == None and param4 == None and param5 == None):
            return parser[category]
        elif (param2 == None and param3 == None and param4 == None and param5 == None):
            return parser[category][param1]
        elif (param3 == None and param4 == None and param5 == None):
            return parser[category][param1][param2]
        elif (param4 == None and param5 == None):
            return parser[category][param1][param2][param3]
        elif (param5 == None):
            return parser[category][param1][param2][param3][param4]
        else:
            return parser[category][param1][param2][param3][param4][param5]

    def get_satellite_info(self):
        parser = pd.read_json(self.arch_path)
        spaceSegment = parser['spaceSegment']
        sat_list = []
        for segment in spaceSegment:
            satellites = segment['satellites']
            for sat in satellites:
                sat_list.append(sat)
        return sat_list

    ###----- FUNCITONS TO GET LOCAL DATA -----###
    def get_local(self,
                  column):  # --> TAKES: t0	t1	POI	lat	lon	alt	ATavg	ATmin	ATmax	RvTavg	RvTmin	RvTmax	TCcov	numPass
        parser = pd.read_csv(self.local_path, skiprows=[0])
        return_data = parser[column].values
        return return_data

    def get_local_dataframe(self):
        parser = pd.read_csv(self.local_path, skiprows=[0])
        return parser

    def get_local_access(self):
        parser = pd.read_csv(self.local_path, skiprows=[0])
        latitudes = parser['lat'].values
        longitudes = parser['lon'].values
        access_avg = parser['ATavg'].values
        access_min = parser['ATmin'].values
        access_max = parser['ATmax'].values
        return latitudes, longitudes, access_avg, access_min, access_max

    def get_local_revisit(self):
        parser = pd.read_csv(self.local_path, skiprows=[0])
        latitudes = parser['lat'].values
        longitudes = parser['lon'].values
        revisit_avg = parser['RvTavg'].values
        revisit_min = parser['RvTmin'].values
        revisit_max = parser['RvTmax'].values
        return latitudes, longitudes, revisit_avg, revisit_min, revisit_max

    def get_local_time_to_cov(self):
        parser = pd.read_csv(self.local_path, skiprows=[0])
        latitudes = parser['lat'].values
        longitudes = parser['lon'].values
        time_to_coverage = parser['TCcov'].values
        return latitudes, longitudes, time_to_coverage

    def get_local_passes(self):
        parser = pd.read_csv(self.local_path, skiprows=[0])
        latitudes = parser['lat'].values
        longitudes = parser['lon'].values
        num_passes = parser['numPass'].values
        return latitudes, longitudes, num_passes

    ### FUNCITONS TO GET LOCAL DATA ###

    def get_objective_data(self, objective):
        if (objective == "MinTime"):
            return self.get_global("Time", "min")
        elif (objective == "MaxTime"):
            return self.get_global("Time", "max")
        elif (objective == "MinTimeToCoverage"):
            return self.get_global("TimeToCoverage", "min")
        elif (objective == "MaxTimeToCoverage"):
            return self.get_global("TimeToCoverage", "max")
        elif (objective == "MeanTimeToCoverage"):
            return self.get_global("TimeToCoverage", "avg")
        elif (objective == "MinAccessTime"):
            return self.get_global("AccessTime", "min")
        elif (objective == "MaxAccessTime"):
            return self.get_global("AccessTime", "max")
        elif (objective == "MeanAccessTime"):
            return self.get_global("AccessTime", "avg")
        elif (objective == "MinRevisitTime"):
            return self.get_global("RevisitTime", "min")
        elif (objective == "MaxRevisitTime"):
            return self.get_global("RevisitTime", "max")
        elif (objective == "MeanRevisitTime"):
            return self.get_global("RevisitTime", "avg")
        elif (objective == "MinResponseTime"):
            return self.get_global("ResponseTime", "min")
        elif (objective == "MeanResponseTime"):
            return self.get_global("ResponseTime", "avg")
        elif (objective == "Coverage"):
            return self.get_global("Coverage")
        elif (objective == "MinNumOfPOIpasses"):
            return self.get_global("NumOfPOIpasses", "min")
        elif (objective == "MaxNumOfPOIpasses"):
            return self.get_global("NumOfPOIpasses", "max")
        elif (objective == "MeanNumOfPOIpasses"):
            return self.get_global("NumOfPOIpasses", "avg")
        elif (objective == "MaxDataLatency"):
            return self.get_global("DataLatency", "max")
        elif (objective == "MeanDataLatency"):
            return self.get_global("DataLatency", "avg")
        elif (objective == "NumGSpassesPD"):
            return self.get_global("NumGSpassesPD")
        elif (objective == "TotalDownlinkTimePD"):
            return self.get_global("TotalDownlinkTimePD")
        elif (objective == "MinDownlinkTimePerPass"):
            return self.get_global("DownlinkTimePerPass", "min")
        elif (objective == "MaxDownlinkTimePerPass"):
            return self.get_global("DownlinkTimePerPass", "max")
        elif (objective == "MeanDownlinkTimePerPass"):
            return self.get_global("DownlinkTimePerPass", "avg")
        elif (objective == "groundCost"):
            return self.get_cost("groundCost", "estimate")
        elif (objective == "hardwareCost"):
            return self.get_cost("hardwareCost", "estimate")
        elif (objective == "iatCost"):
            return self.get_cost("iatCost", "estimate")
        elif (objective == "launchCost"):
            return self.get_cost("launchCost", "estimate")
        elif (objective == "lifecycleCost"):
            return self.get_cost("lifecycleCost", "estimate")
        elif (objective == "nonRecurringCost"):
            return self.get_cost("nonRecurringCost", "estimate")
        elif (objective == "operationsCost"):
            return self.get_cost("operationsCost", "estimate")
        elif (objective == "programCost"):
            return self.get_cost("programCost", "estimate")
        elif (objective == "recurringCost"):
            return self.get_cost("recurringCost", "estimate")
        elif (objective == "Total Architecture Value [Mbits]"):
            return self.get_value("Total Architecture Value [Mbits]")
        elif (objective == "Total Lifecycle Cost [$M]"):
            return self.get_value("Total Lifecycle Cost [$M]")
        elif (objective == "Ratio of Value to Cost [Mbits/$M]"):
            return self.get_value("Ratio of Value to Cost [Mbits/$M]")
        elif (objective == "Total Data Collected [Mbits]"):
            return self.get_value("Total Data Collected [Mbits]")
        elif (objective == "EDA Scaling Factor"):
            return self.get_value("EDA Scaling Factor")
        elif (objective == "Average GP Resolution [km^2]"):
            return self.get_value("Average GP Resolution [km^2]")
        elif (objective == "Average GP Resolution [m^2]"):
            return self.get_value("Average GP Resolution [m^2]")

    def get_name(self):
        return self.name


# Holds the mission.json file
class MissionInterface:

    def __init__(self, path):
        self.path = path

    def get_element(self, category, param1=None, param2=None, param3=None, param4=None, param5=None):
        parser = pd.read_json(self.path)
        if (param1 == None and param2 == None and param3 == None and param4 == None and param5 == None):
            return parser[category]
        elif (param2 == None and param3 == None and param4 == None and param5 == None):
            return parser[category][param1]
        elif (param3 == None and param4 == None and param5 == None):
            return parser[category][param1][param2]
        elif (param4 == None and param5 == None):
            return parser[category][param1][param2][param3]
        elif (param5 == None):
            return parser[category][param1][param2][param3][param4]
        else:
            return parser[category][param1][param2][param3][param4][param5]


# Overview plot on the "Overview" page
class OverviewPlotData:
    def __init__(self):
        self.arch_names = []
