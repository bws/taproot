#ifndef MFEM_MESH_READER_H
#define MFEM_MESH_READER_H

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/** 
 * A basic flattened representation of a point of MFEM FE data 
 */
typedef struct laghos_mesh_point {
    double x, y, z;
    double e, rho, v_x, v_y, v_z;
    size_t vertex_id;
    size_t element_id;
} laghos_mesh_point_t;

/** 
 * A vertex within an MFEM mesh 
 */
typedef struct laghos_mesh_vertex {
    size_t vertex_id;
    double x, y, z;
} laghos_mesh_vertex_t;

/** 
 * The attributes of a single vertex within an MFEM element
 * 
 * Note: attributes belong to a vertex-element *pair* 
 */
typedef struct laghos_mesh_element_attr {
    double e, rho, v_x, v_y, v_z;
    size_t vertex_id;
    size_t element_id;
} laghos_mesh_element_attr_t;

/** 
 * Geometry data about MFEM mesh element 
 */
typedef struct laghos_mesh_element_geom {
    size_t vertexes[8];
    size_t element_id;
} laghos_mesh_element_geom_t;

/**
 * Iterator for an MFEM mesh 
 */
typedef struct mfem_mesh_iterator {
   size_t cur_idx;
   uint8_t *visited_verts;
   size_t nvv;
} mfem_mesh_iterator_t;

/**
 * Initialize MFEM mesh iterator resources
 * @return 0 on success
 */
int mfem_mesh_iterator_init(mfem_mesh_iterator_t* cur);

/** 
 * Destroy MFEM mesh iterator resources 
 */
void mfem_mesh_iterator_destroy(mfem_mesh_iterator_t* cur);

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
int mfem_laghos_mesh_read_points(int mhandle, mfem_mesh_iterator_t* cur, laghos_mesh_point_t* points, size_t npoints);

/**
 * Read a portion of the globally distributed MFEM mesh as vertexes, element attrs, and element geometry
 *
 * @param[in] mhandle the mesh handle
 * @param[in,out] cur the mesh iterator pointing at the current element
 * @param[in,out] pointer to the vertex array to store mesh data into
 * @param[in,out] pointer to the element attr array to store mesh data into
 * @param[in,out] pointer to the element geometry array to store mesh data into
 * @param[in,out] pointer to the element array to store mesh data into
 * @param[in,out] neles the number of elements
 * @return the number of points stored in the in/out array
 * Note: An MFEM mesh element contains multiple vertices. Those
 *       vertices are laghos_mesh_points. This function will only read
 *       entire elements. 
 */
int mfem_laghos_mesh_read_ve(int mhandle, mfem_mesh_iterator_t *cur, 
                             laghos_mesh_vertex_t *verts, size_t *nverts,
                             laghos_mesh_element_attr_t *eattrs, 
                             laghos_mesh_element_geom_t *egeoms, 
                             size_t *neles);

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