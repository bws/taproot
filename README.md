Prerequisites

Install Gradle

Install MFEM
  cp config/default.mk config/user/mk
  edit PREFIX, SHARED=yes
  make serial
  make install

  CMake installation does not work, these instructions will not enable -fPIC
  mkdir build
  cd build
  cmake -DCMAKE_INSTALL_PREFIX=$swhome -DCMAKE_INSTALL_RPATH=$swhome/lib -DMFEM_USE_MPI=no ..
  make install


Build MFEM Utils
  * Note mfem cannot yet reliably build shared libraries, so we will create a shared object to use MFEM


Build Lucene for Science
