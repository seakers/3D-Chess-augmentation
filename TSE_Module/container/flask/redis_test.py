import redis
import json
import pickle
import time

from dashboard.PlotClasses import ArchitectureInterface, MissionInterface, TradespaceInterface, RedisInterface


if __name__ == "__main__":
	start_time = time.time()
	ri = RedisInterface("/mission")
	print("--- %s seconds ---" % (time.time() - start_time))

	arches, x, y, color, size = ri.query_global_data('MeanAccessTime','MinAccessTime','groundCost','Total Lifecycle Cost [$M]')
	print(arches, x, y, color, size)
	print(len(arches),len(x),len(y), len(color), len(size))








	print("--- %s seconds ---" % (time.time() - start_time))




