

#include "ibis.h"
#include "mfem-utils.h"
#include <string>
using namespace std;

const int TAPROOT_MFEM_FETCH_SIZE = 4096;

void print_usage(int argc, char** argv) {
    cout << argc[0] << " mesh_directory";
    cout.flush();
}

int index_points_with_fastbit(int md, ) {

}

int main(int argc, char** argv) {

    string laghosMeshDir = argv[1];
    if (laghosMeshDir.size() == 0) {
        cerr << "ERROR: No mesh directory provided"
        print_usage(argc, argv);
        return 1;
    }

    // Get the mesh filenames
    string meshFile, energyFile, velocityFile, rhoFile;
    int success = get_mesh_files(laghosMeshDir);
    if (0 != success) {
        cerr << "ERROR: Unable to find all of the mesh file names"
        return 2;
    }


    // Open the Laghos Mesh
    int md = open_laghos_mesh(laghosMeshDir);

    // Index the first N mesh dimensions
    laghos_mesh_point* point_buf = new laghos_mesh_point[TAPROOT_MFEM_FETCH_SIZE];

    // Close the mesh
    close_laghos_mesh(md);

    return 0;
}