# Overview
Taproot contains code to evaluate the creation of a software stack for performing
scientific data analysis. The stack can currently be built to evaluate anlyzing
scientific data with various packages. Some of those packages require complex
build system configurations.

The available analysis stacks are:
Apache Lucene
Lawrence Berkeley Lab's Fastbit
LevelDB

# General Prerequisites
You will need to either build Laghos and all of its pre-requisites to generate
a Laghos-formatted MFEM mesh, or if you already have a Laghos mesh stored in
files you can simply build a serial version of MFEM and use that to construct 
arrays of Laghos mesh data.

## Building MFEM for use with previously generated mesh data
  * Note that to use MFEM with Lucene it is necessary to build MFEM so that it can
  * be linked dynamically. By default these steps do not occur.
  wget https://bit.ly/mfem-4-5
  tar xvfz mfem-4.5.tgz
  cd mfem-4.5
  cp config/default.mk config/user/mk
  edit PREFIX, SHARED=yes
  make serial
  make install

NOTE: The following CMake steps do not work with this version of MFEM.
  CMake installation does not work, these instructions will not enable -fPIC
  mkdir build
  cd build
  cmake -DCMAKE_INSTALL_PREFIX=$swhome -DCMAKE_INSTALL_RPATH=$swhome/lib -DMFEM_USE_MPI=no ..
  make install

# Building Apache Lucene Analysis Capability
The following steps describe how to build the taproot Lucene data analytics package

## Build and Install Gradle
  Install curl
  curl -s "https://get.sdkman.io" | bash
  sdk install gradle 8.1.1

## Download and Build Apache Lucene version 10.0
  Install OpenJDK 17 or 18
  git clone https://github.com/apache/lucene
  cd lucene
  ./gradlew

## Configure Taproot
  cd taproot
  Edit build.gradle to set the path to MFEM

## Build Taproot MFEM Utils
  ./gradlew build
  ./gradlew runTest

## Build Taproot Lucene for Science
  ./gradlew build
  ./gradlew test

## Run Taproot
  * Stop all existing gradle daemons
  ./gradlew --stop

  * Even though we specify no daemon, one daemon runs and exits at shutdown (we'd prefer no Daemon at all)
  ./gradlew run --no-daemon

# Building LBL Fastbit Analysis Capability

## Configure and build Taproot
  cd taproot
  mkdir build
  cd build
  ../cmake
  make

## Generate a fastbit index
  ./taproot-create-fastbit-index -i <mesh_dir> -o <index dir>
  ./taproot-select-fastbit -i <index dir> -q <query>

