#include <iostream>
#include <vector>
#include <mfem.hpp>
#include <mfem/fem/fespace.hpp>
#include <mfem/fem/gridfunc.hpp>
#include <mfem-mesh-reader.h>
using namespace std;
using namespace mfem;


/** MFEM data required to create a Laghos Mesh */
typedef struct mfem_laghos_mesh {
    Mesh* mesh;
    GridFunction* e_gf;
    GridFunction* rho_gf;
    GridFunction* v_gf;
    GridFunction* x_gf;
    FiniteElementSpace* e_fes;
    FiniteElementSpace* rho_fes;
    FiniteElementSpace* v_fes;
} mfem_laghos_mesh_t;

static std::vector<Mesh*> meshes;
static std::vector<mfem_laghos_mesh_t> mlmv;

int mfem_mesh_open(const char* mesh_filename) {
    Mesh* m = new Mesh(mesh_filename, 1);
    int handle = meshes.size();
    meshes.push_back(m);
    return handle;
}

int mfem_mesh_close(int mesh_handle) {
    Mesh* m = meshes[mesh_handle];
    delete m;
    return 0;
}

int mfem_laghos_mesh_open(const char* mesh_file, const char* e_gf_file, const char* rho_gf_file, const char* v_gf_file) {
    mfem_laghos_mesh_t lm = {0};
    int handle = mlmv.size();

    // Create the mesh
    lm.mesh = new Mesh(mesh_file);
    
    // Create the grid functions
    ifstream e_gf_s, rho_gf_s, v_gf_s;
    e_gf_s.open(e_gf_file);
    lm.e_gf = new GridFunction(lm.mesh, e_gf_s);
    rho_gf_s.open(rho_gf_file);
    lm.rho_gf = new GridFunction(lm.mesh, rho_gf_s);
    v_gf_s.open(v_gf_file);
    lm.v_gf = new GridFunction(lm.mesh, v_gf_s);

    // Create the FESpaces for each of the mesh fields
    lm.e_fes = lm.e_gf->FESpace();
    lm.rho_fes = lm.rho_gf->FESpace();
    lm.v_fes = lm.v_gf->FESpace();
    
    mlmv.push_back(lm);
    return handle;
}

int mfem_laghos_mesh_close(int mesh_handle) {
    mfem_laghos_mesh mlm = mlmv[mesh_handle];
    delete mlm.mesh;
    delete mlm.e_gf;
    delete mlm.rho_gf;
    delete mlm.v_gf;
    return 0;
}

int mfem_laghos_mesh_read(int mlm_handle, mfem_mesh_iterator_t* begin, laghos_mesh_point_t* points, size_t npoints) {

    // Retrieve the mesh from the global mesh array
    mfem_laghos_mesh_t mlm = mlmv[mlm_handle];
    int eleCount = 0;
    int ptCount = 0;

    // Read an element and attempt to add its vertexes as points
    const Element* const* elements = mlm.mesh->GetElementsArray();
    long long nEles = mlm.mesh->GetNE();
    Array<double> energies, densities, v_xs, v_ys, v_zs;
    for (int i = *begin; i < nEles; i++) {
        mlm.e_gf->GetNodalValues(i, energies, 1);
        mlm.rho_gf->GetNodalValues(i, densities, 1);
        mlm.v_gf->GetNodalValues(i, v_xs, 1);
        mlm.v_gf->GetNodalValues(i, v_ys, 2);
        mlm.v_gf->GetNodalValues(i, v_zs, 3);

        // Count the vertexes for this element
        const Element* ele = elements[i];
        size_t nv = ele->GetNVertices();

        // If there is enough space for this element in the point array add it
        if ((npoints - ptCount) >= nv) {
            const int* vertArray = ele->GetVertices();
            for (int j = 0; j < nv; j++) {
                size_t dims = mlm.mesh->Dimension();
                double* pos = mlm.mesh->GetVertex(vertArray[j]);
                points[ptCount].element_id = i;
                if (1 <= dims)
                    points[ptCount].x = pos[0];
                if (2 <= dims)
                    points[ptCount].y = pos[1];
                if (3 <= dims)
                    points[ptCount].z = pos[2];
                points[ptCount].e = energies[j];
                points[ptCount].rho = densities[j];
                points[ptCount].v_x = v_xs[j];
                points[ptCount].v_y = v_ys[j];
                points[ptCount].v_z = v_zs[j];
                ptCount++;
            }
            eleCount += 1;
        }
        else {
            //cerr << "Warning: Unable to pack last cell. Cell points req: " << ptCount << " Space avail: " << eleCount << endl;
            break;
        }
    }
    *begin += eleCount;
    return ptCount;
}

int mfem_laghos_mesh_at_end(int mlm_handle, const mfem_mesh_iterator_t* iter) {
    mfem_laghos_mesh_t mlm = mlmv[mlm_handle];
    long long nEles = mlm.mesh->GetNE();
    return (*iter >= nEles);
}