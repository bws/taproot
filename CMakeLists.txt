# CMakeLists files in this project can
# refer to the root source directory of the project as ${TAPROOT_SOURCE_DIR} and
# to the root binary directory of the project as ${TAPROOT_BINARY_DIR}.
cmake_minimum_required (VERSION 3.6.3)
project (Taproot LANGUAGES C CXX)

#
# Enable C++17 (needed by Arrow)
#
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

#
# Enable extra compiler flags
#
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -Wall -Wextra -fmax-errors=1 -O0 -g3 -DDEBUG") # -D_GLIBCXX_DEBUG

#
# linker flags used internally
#
set(EXEC_LDFLAGS)

#
# Include CMake modules
#
find_package(PkgConfig REQUIRED)

#
# Need pthread
#
find_package(Threads REQUIRED)

#
# External projects
#
#find_package(MPI REQUIRED)
#if (CMAKE_SYSTEM_NAME STREQUAL "CrayLinuxEnvironment")
#    set(MPI_C_COMPILE_FLAGS "${MPI_C_COMPILE_FLAGS} -dynamic -craympich-mt")
#    set(MPI_CXX_COMPILE_FLAGS "${MPI_CXX_COMPILE_FLAGS} -dynamic -craympich-mt")
#endif()
#
#message(STATUS "MPI_CXX_INCLUDE_DIRS=" ${MPI_CXX_INCLUDE_DIRS})
#message(STATUS "MPI_CXX_LIBRARIES=" ${MPI_CXX_LIBRARIES})
#message(STATUS "MPI_CXX_LINK_FLAGS=" ${MPI_CXX_LINK_FLAGS})
#include_directories(SYSTEM ${MPI_CXX_INCLUDE_DIRS})
#
#list(APPEND EXEC_LDFLAGS ${MPI_CXX_LIBRARIES})
#foreach(INCLUDE_DIR ${MPI_CXX_INCLUDE_DIRS})
#  list(APPEND PC_CFLAGS "-I${INCLUDE_DIR}")
#endforeach()
#list(APPEND PC_LIBS ${MPI_CXX_LIBRARIES})
#list(APPEND PC_STATIC_LIBS ${MPI_CXX_LIBRARIES})

#
# Configure Boost
#
find_package(Boost REQUIRED COMPONENTS program_options)
if (Boost_FOUND)
  set(BOOST_INCLUDE_DIRS "${Boost_INCLUDE_DIRS}")
  set(BOOST_LIB_DIRS "${Boost_LIBRARY_DIRS}")
  message(STATUS "Boost enabled")
else()
  message(STATUS "Boost disabled")
endif()

#
# Arrow Support -- we rely on Arrow's Parquet support
#
option(TAPROOT_ENABLE_ARROW "Use Apache Arrow" On)
if(ARROW_DIR)
  # Import the arrow cmake modules and search for the relevant components
  list(APPEND CMAKE_MODULE_PATH "${ARROW_DIR}/lib/cmake/Arrow")
endif()
find_package(Arrow REQUIRED)
if (Arrow_FOUND)
  set(ARROW_INCLUDE_DIRS "${Arrow_DIR}/include")
  set(ARROW_LIB_DIR "${Arrow_DIR}/lib")
  message(STATUS "Arrow enabled")
else()
  message(STATUS "Arrow disabled")
endif()

find_package(Parquet REQUIRED)
if (Parquet_FOUND)
  set(PARQUET_INCLUDE_DIRS "${Parquet_DIR}/include")
  set(PARQUET_LIB_DIR "${Parquet_DIR}/lib")
  message(STATUS "Parquet enabled")
else()
  message(STATUS "Parquet disabled")
endif()

#
# MFEM Support
#
option(TAPROOT_ENABLE_MFEM "Use MFEM" On)
if(MFEM_DIR)
  set(MFEM_INCLUDE_DIRS "${MFEM_DIR}/include")
  set(MFEM_LIB_DIR "${MFEM_DIR}/lib")
  include_directories(AFTER SYSTEM ${MFEM_INCLUDE_DIRS})
  link_directories(AFTER ${MFEM_LIB_DIR})
endif()
find_library(MFEM_IMPORT_LIB NAMES mfem HINTS ${MFEM_LIB_DIR})
if (MFEM_IMPORT_LIB)
  message(STATUS "MFEM enabled")
else()
  message(SEND_ERROR "Could not find libmfem.so")
endif()

#
# FastBit Package Environment
#
option(TAPROOT_ENABLE_FASTBIT "Use Fastbit" Off)
if(FASTBIT_DIR)
  set(FASTBIT_INCLUDE_DIRS "${FASTBIT_DIR}/include")
  set(FASTBIT_LIB_DIR "${FASTBIT_DIR}/lib")
  include_directories(AFTER SYSTEM ${FASTBIT_INCLUDE_DIRS})
  link_directories(AFTER ${FASTBIT_LIB_DIR})
endif()
find_library(FASTBIT_IMPORT_LIB NAMES fastbit HINTS ${FASTBIT_LIB_DIR})
if (FASTBIT_IMPORT_LIB)
  message(STATUS "Fastbit found but not currently support.")
else()
  message(STATUS "Fastbit not currently support.")
endif()

#
# Build mfem utilities library
#
add_subdirectory(mfem-utils)

#
# Build the index builders
#
#add_subdirectory(index-builder)

#
# Add unit test executable
#
#enable_testing()
#add_subdirectory(test)

#
# Add example executables
#
#add_subdirectory(examples)
