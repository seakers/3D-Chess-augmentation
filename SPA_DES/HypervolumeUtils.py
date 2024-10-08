import numpy as np
import time

class HypervolumeGrid:

    def __init__(self,refPoint):
        if isinstance(refPoint,list):
            refPoint = np.array(refPoint)
        self.refPoint = refPoint
        self.initializeGrid()

    def initializeGrid(self):
        """
        Function to initialize the hypervolume grid with the reference point
        """
        self.gridSize = 11 # maybe change this to have different resolution closer to the best point
        self.HVMax = np.prod(self.refPoint)

        exp = np.arange(len(self.refPoint)-1,-1,-1)
        self.mult = np.power(self.gridSize,exp)

        self.numPoints = (self.gridSize)**len(self.refPoint)
        self.dominated = np.zeros(int(self.numPoints), dtype=bool)

        self.paretoFrontPoint = []
        self.paretoFrontSolution = []

    def updateHV(self,point,solution):
        """
        Function to update the hypervolume grid and pareto front with a new point and index. 
        Index for each call should be unique and able to reference the solution.
        """
        if isinstance(point,list):
            point = np.array(point)
        pointCalc = self.refPoint - point
        gridPoint = np.floor(pointCalc/self.refPoint * (self.gridSize-1))
        pointIdx = int(np.dot(gridPoint,self.mult))

        if not self.dominated[pointIdx]:
            dominatedInd = np.arange(self.numPoints,dtype=int)
            newDominated = np.ones(self.numPoints, dtype=bool)
            for dim in range(len(self.refPoint)):
                dimList = np.floor_divide(np.mod(dominatedInd,self.mult[dim]*self.gridSize,dtype=int),self.mult[dim],dtype=int)
                fitDim = dimList <= gridPoint[dim]
                newDominated = np.logical_and(newDominated,fitDim, dtype=bool)
            
            self.updateParetoFront(point,solution)

            self.dominated = np.logical_or(self.dominated, newDominated, dtype=bool)

    def updateParetoFront(self,newPoint,solution):
        # Function to update the pareto front
        if not self.paretoFrontPoint:
            self.paretoFrontPoint = [newPoint]
            self.paretoFrontSolution = [solution]
        else:
            newDominated = [i for i,point in enumerate(self.paretoFrontPoint) if np.all(point >= newPoint)]
            for j in sorted(newDominated, reverse=True):
                del self.paretoFrontPoint[j]
                del self.paretoFrontSolution[j]
            self.paretoFrontPoint.append(newPoint)
            self.paretoFrontSolution.append(solution)

    def getHV(self):
        return np.sum(self.dominated)/self.numPoints * self.HVMax


# class HypervolumeGrid:

#     def __init__(self,refPoint):
#         if isinstance(refPoint,list):
#             refPoint = np.array(refPoint)
#         self.refPoint = refPoint
#         self.initializeGrid()

#     def initializeGrid(self):
#         # Function to initialize the grid with the reference point
#         self.gridSize = 11
#         dims = np.zeros([len(self.refPoint),self.gridSize])
#         for i,a in enumerate(self.refPoint):
#             dims[i] = (np.linspace(0,a,self.gridSize))
        
#         mesh = np.meshgrid(*dims)
#         self.grid = np.vstack(list(map(np.ravel,mesh))).T
#         self.dominated = np.zeros(len(self.grid))

#     def updateGrid(self,point):
#         if isinstance(point,list):
#             point = np.array(point)
#         pointCalc = self.refPoint - point
#         if not self.isDominated(pointCalc):
#             newDominated = np.prod((self.grid <= pointCalc), axis=1)
#             self.dominated = np.logical_or(self.dominated, newDominated)
#         else:
#             print("Point is dominated")

#     def isDominated(self,pointCalc):
#         # Function to check if a point is dominated by the grid
#         gridPoint = np.floor(pointCalc/self.refPoint * (self.gridSize-1))
#         exp = np.arange(len(pointCalc)-1,-1,-1)
#         mult = np.power(self.gridSize,exp)
#         idx = np.dot(gridPoint,mult)
#         dom = self.dominated[int(idx)]
#         return dom

#     def getHV(self):
#         return np.sum(self.dominated)
