# Set C++ standard and compiler flags
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -D_GLIBCXX_USE_CXX11_ABI=0 -fPIC")

# CMake version requirement
cmake_minimum_required(VERSION 3.4...3.18)

# Project definition
project(propcov)

# Find and require Pybind11 version 2.5
find_package(pybind11 2.5 REQUIRED)

# Link to pre-built GmatUtil
link_directories(/root/orbitpy/propcov/extern/gmatutil/build)
include_directories(/root/orbitpy/propcov/extern/gmatutil/include)

# Target libraries
#add_subdirectory(extern/pybind11)
add_subdirectory(extern/gmatutil)
add_subdirectory(lib/propcov-cpp)

# Include directories
include_directories(
    BEFORE
    extern/gmatutil/include/
    extern/gmatutil/util/interpolator
    extern/gmatutil/util/matrixoperations
    lib/propcov-cpp
    lib/propcov-cpp/polygon
)

# Define the Python module using Pybind11
pybind11_add_module(propcov MODULE src/main.cpp)

# Link libraries to propcov
target_link_libraries(propcov PRIVATE GmatUtil PropCovCpp)

# RPATH configuration for dynamic libraries
if(UNIX AND NOT APPLE)
    set_target_properties(propcov PROPERTIES INSTALL_RPATH "$ORIGIN/relative/path/to/supporting/library_dir")
elseif(APPLE)
    set_target_properties(propcov PROPERTIES INSTALL_RPATH "@loader_path/relative/path/to/supporting/library_dir")
endif()

set_target_properties(propcov PROPERTIES INSTALL_RPATH_USE_LINK_PATH TRUE)
