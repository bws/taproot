#ifndef MFEM_MESH_READER_H
#define MFEM_MESH_READER_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct laghos_mesh_point {
    int element_id;
    double x, y, z;
    double e, rho, v_x, v_y, v_z;
} laghos_mesh_point_t;

typedef int mfem_mesh_iterator_t;

/**
 * @return a handle to an MFEM mesh from laghos
 */
int mfem_open_laghos_mesh(const char* mesh_file, const char* e_gf_file,
                          const char* rho_gf_file, const char* v_gf_file);


/**
 * Close an MFEM mesh
 * 
 * @return 0 on success
 */
int mfem_close_laghos_mesh(int lmhandle);

/**
 * Read a portion of the globally distributed MFEM mesh
 *
 * @param[in] the mesh handle
 * @param[in,out] the mesh iterator pointing at the current element
 * @param[in,out] the point array to store mesh data into
 * @param[in] the number of points in the passed in point array
 * @return the number of points stored in the in/out array
 * Note: An MFEM mesh element contains multiple vertices. Those
 *       vertices are laghos_mesh_points. This function will only read
 *       entire elements. 
 */
int mfem_read_laghos_mesh(int mhandle, mfem_mesh_iterator_t* cur, laghos_mesh_point_t* points, size_t npoints);

#ifdef __cplusplus
}
#endif

#endif