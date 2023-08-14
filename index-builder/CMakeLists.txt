#
# Create the index builder libraries
#
#add_library(index-builder src/main/cpp/mfem-mesh-reader.cpp)

#
# Create an index builder for fastbit
#
add_executable(fastbit-builder src/fastbit-builder.cpp)


target_include_directories(fastbit-builder PUBLIC ${FASTBIT_INCLUDE_DIR})
target_include_directories(fastbit-builder PUBLIC ${MFEM_INCLUDE_DIR})
target_include_directories(fastbit-builder PUBLIC ${MFEM_UTILS_INCLUDE_DIR})
target_include_directories(fastbit-builder PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include)
target_include_directories(fastbit-builder PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/src)
