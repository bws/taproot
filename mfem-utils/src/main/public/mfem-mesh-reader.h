#ifndef MFEM_MESH_READER_H
#define MFEM_MESH_READER_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct laghos_mesh_point {
    double x, y, z;
    double e, rho, v_x, v_y, v_z;
    size_t element_id;
} laghos_mesh_point_t;

typedef size_t mfem_mesh_iterator_t;

/**
 * @return a handle to an MFEM mesh from Laghos
 */
int mfem_laghos_mesh_open(const char* mesh_file, const char* e_gf_file,
                          const char* rho_gf_file, const char* v_gf_file);


/**
 * Close an MFEM mesh from Laghos
 * 
 * @return 0 on success
 */
int mfem_laghos_mesh_close(int lmhandle);

/**
 * Read a portion of the globally distributed MFEM mesh
 *
 * @param[in] mhandle the mesh handle
 * @param[in,out] cur the mesh iterator pointing at the current element
 * @param[in,out] points the point array to store mesh data into
 * @param[in] npoints the number of points in the passed in point array
 * @return the number of points stored in the in/out array
 * Note: An MFEM mesh element contains multiple vertices. Those
 *       vertices are laghos_mesh_points. This function will only read
 *       entire elements. 
 */
int mfem_laghos_mesh_read(int mhandle, mfem_mesh_iterator_t* cur, laghos_mesh_point_t* points, size_t npoints);

/**
 * @return true if all elements have been iterator over
 * 
 * @param[in] mhandle the mesh handle
 * @param[in] cur the mesh iterator
 */
int mfem_laghos_mesh_at_end(int mhandle, const mfem_mesh_iterator_t* cur);

/**
 * @return the number of mesh elements
 * 
 * @param[in] mhandle the mesh handle
 */
size_t mfem_laghos_mesh_get_num_elements(int mhandle);

/**
 * @return the number of mesh points
 * 
 * @param[in] mhandle the mesh handle
 */
size_t mfem_laghos_mesh_get_num_points(int mhandle);

#ifdef __cplusplus
}
#endif

#endif