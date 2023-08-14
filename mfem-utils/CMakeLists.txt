#
# Set locations for this package
#
set(MFEM_UTILS_INCLUDE_DIR "${PROJECT_SOURCE_DIR}/mfem-utils/src/main/public")
set(MFEM_UTILS_LIB_DIR "${CMAKE_BINARY_DIR}/mfem-utils")
set(MFEM_UTILS_LIB "mfem-utils")

#
# Define the MFEM to Parquet executable
#
set(MFEM_TO_PARQUET_EXE "mfem2parquet")
add_executable(${MFEM_TO_PARQUET_EXE} src/main/cpp/mfem2parquet.cpp)
target_include_directories(${MFEM_TO_PARQUET_EXE} PUBLIC ${MFEM_UTILS_INCLUDE_DIR})
target_include_directories(${MFEM_TO_PARQUET_EXE} PUBLIC ${MFEM_INCLUDE_DIRS})
target_include_directories(${MFEM_TO_PARQUET_EXE} PUBLIC ${PARQUET_INCLUDE_DIRS})
target_link_libraries(${MFEM_TO_PARQUET_EXE} PUBLIC ${MFEM_UTILS_LIB})
target_link_libraries(${MFEM_TO_PARQUET_EXE} PUBLIC Arrow::arrow_static)
target_link_libraries(${MFEM_TO_PARQUET_EXE} PUBLIC Parquet::parquet_static)
target_link_libraries(${MFEM_TO_PARQUET_EXE} PUBLIC ${MFEM_LIB})

#
# Define the mfem-utils library
#
add_library(${MFEM_UTILS_LIB} src/main/cpp/mfem-mesh-reader.cpp)
target_include_directories(${MFEM_UTILS_LIB} PUBLIC ${MFEM_UTILS_INCLUDE_DIR})
target_include_directories(${MFEM_UTILS_LIB} PUBLIC ${MFEM_INCLUDE_DIR})