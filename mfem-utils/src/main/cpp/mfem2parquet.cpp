
#include <iostream>
#include <string>
#include <arrow/api.h>
#include <arrow/io/api.h>
#include <parquet/arrow/reader.h>
#include <parquet/arrow/writer.h>
#include <parquet/exception.h>
#include "mfem-mesh-reader.h"
using namespace std;
using namespace arrow;

void show_usage(const string& exeName) {
    cerr << "Usage: " << exeName << " mfem_dir parquet_dir\n";
}

int open_mfem_dir(const string& mfemDir) {
    string meshFilename = mfemDir + "/" + "mesh";
    string energyFilename = mfemDir + "/" + "e";
    string rhoFilename = mfemDir + "/" + "rho";
    string velocityFilename = mfemDir + "/" + "v";

    int mh = mfem_laghos_mesh_open(meshFilename.c_str(), 
                                   energyFilename.c_str(), 
                                   rhoFilename.c_str(), 
                                   velocityFilename.c_str());
    return mh;
}

void* create_flat_parquet_dataset(const string& outputDir) {

    // Create the schema
    arrow::FieldVector fields = {arrow::field("x", arrow::int64()),
                                 arrow::field("y", arrow::float64()),
                                 arrow::field("z", arrow::float64()),
                                 arrow::field("e", arrow::float64()),
                                 arrow::field("rho", arrow::float64()),
                                 arrow::field("v_x", arrow::float64()),
                                 arrow::field("v_y", arrow::float64()),
                                 arrow::field("v_z", arrow::float64()),
                                 arrow::field("elementId", arrow::uint64())};
    std::shared_ptr<Schema> flatSchema = arrow::schema( {arrow::field("x", arrow::int64()),
                                                    arrow::field("y", arrow::float64()),
                                                    arrow::field("z", arrow::float64()),
                                                    arrow::field("e", arrow::float64()),
                                                    arrow::field("rho", arrow::float64()),
                                                    arrow::field("v_x", arrow::float64()),
                                                    arrow::field("v_y", arrow::float64()),
                                                    arrow::field("v_z", arrow::float64()),
                                                    arrow::field("elementId", arrow::uint64()),
                                                    });

    // Create the table
    auto table = arrow::Table::MakeEmpty(flatSchema);
    return nullptr;
}

bool processChunk(int mfemHandle, void* parqDS) {

    // Fill the buffer with mfem data

    // Write the data with arrow
}

int main(int argc, char **argv) {

    string inputDir = argv[1];
    string outputDir = argv[2];

    if (argc < 3 || 0 == inputDir.length() || 0 == outputDir.length()) {
        show_usage(argv[0]);
        return 1;
    }

    // Open the mfem dataset
    int mh = open_mfem_dir(inputDir);
    if (mh < 0) {
        cerr << "Invalid MFEM Mesh directory\n";
        return 2;
    }

    // Create the parquet dataset
    auto ds = create_flat_parquet_dataset(outputDir);

    // Process the data to convert MFEM data to Parquet data
    bool moreData = processChunk(mh, ds);
    while (moreData) {
        moreData = processChunk(mh, ds);
    }

    cout << "Parquet dataset written to: " << outputDir << endl;
    return 0;
}