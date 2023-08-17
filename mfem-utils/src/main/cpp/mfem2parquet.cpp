#include <cassert>
#include <filesystem>
#include <iostream>
#include <string>
#include <arrow/api.h>
#include <arrow/io/api.h>
#include <arrow/io/file.h>
#include <parquet/arrow/reader.h>
#include <parquet/arrow/writer.h>
#include <parquet/exception.h>
#include <parquet/stream_writer.h>
#include "mfem-mesh-reader.h"
using namespace std;
using namespace arrow;

const size_t DEFAULT_BUF_SZ = 16*1024*1024;

void show_usage(const string& exeName) {
    cerr << "Usage: " << exeName << " mfem_dir parquet_dir\n";
}

int open_mfem_mesh(const string& mfemDirString) {
    std::filesystem::path mfemDir(mfemDirString);
    string mfemBase = mfemDir.filename().string();
    std::filesystem::path meshFilename = mfemDir / (mfemBase + "_mesh");
    std::filesystem::path energyFilename = mfemDir / (mfemBase + "_e");
    std::filesystem::path rhoFilename = mfemDir / (mfemBase + "_rho");
    std::filesystem::path velocityFilename = mfemDir / (mfemBase + "_v");

    cerr << "Filenames are: " 
         << meshFilename << " "
         << velocityFilename << endl;
    int mh = mfem_laghos_mesh_open(meshFilename.c_str(), 
                                   energyFilename.c_str(), 
                                   rhoFilename.c_str(), 
                                   velocityFilename.c_str());
    assert(mh >= 0);
    return mh;
}

/** @return the flat Arrow schema */
shared_ptr<arrow::Schema> get_arrow_flat_schema() {
    arrow::FieldVector fields = {arrow::field("x", arrow::float64()),
                                 arrow::field("y", arrow::float64()),
                                 arrow::field("z", arrow::float64()),
                                 arrow::field("e", arrow::float64()),
                                 arrow::field("rho", arrow::float64()),
                                 arrow::field("v_x", arrow::float64()),
                                 arrow::field("v_y", arrow::float64()),
                                 arrow::field("v_z", arrow::float64()),
                                 arrow::field("elementId", arrow::uint64())};
    shared_ptr<Schema> flatSchema = arrow::schema(fields);
    return flatSchema;
}

/** @return the flat Parquet schema */
shared_ptr<parquet::schema::GroupNode> get_parquet_flat_schema() {
    // Create the parquet schema
    parquet::schema::NodeVector fields;
    fields.push_back(parquet::schema::PrimitiveNode::Make(
        "x", parquet::Repetition::REQUIRED, parquet::Type::DOUBLE));
    fields.push_back(parquet::schema::PrimitiveNode::Make(
        "y", parquet::Repetition::REQUIRED, parquet::Type::DOUBLE));
    fields.push_back(parquet::schema::PrimitiveNode::Make(
        "z", parquet::Repetition::REQUIRED, parquet::Type::DOUBLE));
    fields.push_back(parquet::schema::PrimitiveNode::Make(
        "e", parquet::Repetition::REQUIRED, parquet::Type::DOUBLE));
    fields.push_back(parquet::schema::PrimitiveNode::Make(
        "rho", parquet::Repetition::REQUIRED, parquet::Type::DOUBLE));
    fields.push_back(parquet::schema::PrimitiveNode::Make(
        "v_x", parquet::Repetition::REQUIRED, parquet::Type::DOUBLE));
    fields.push_back(parquet::schema::PrimitiveNode::Make(
        "v_y", parquet::Repetition::REQUIRED, parquet::Type::DOUBLE));
    fields.push_back(parquet::schema::PrimitiveNode::Make(
        "v_z", parquet::Repetition::REQUIRED, parquet::Type::DOUBLE));
    fields.push_back(parquet::schema::PrimitiveNode::Make(
        "elementId", parquet::Repetition::REQUIRED, parquet::LogicalType::Int(64, false), parquet::Type::INT64));

    parquet::schema::NodePtr schemaNode = parquet::schema::GroupNode::Make("flat", parquet::Repetition::REQUIRED, fields);
    return static_pointer_cast<parquet::schema::GroupNode>(schemaNode);
}

parquet::StreamWriter create_flat_parquet_writer(const string& outputDir) {

    // Set the Parquet RowGroup properties
    shared_ptr<parquet::WriterProperties> props = parquet::WriterProperties::Builder()
        .max_row_group_length(DEFAULT_BUF_SZ / sizeof(laghos_mesh_point_t))
        ->created_by("Taproot")
        ->version(parquet::ParquetVersion::PARQUET_2_6)
        ->data_page_version(parquet::ParquetDataPageVersion::V2)
        ->compression(parquet::Compression::SNAPPY)
        ->build();

    // Create the Parquet schema
    shared_ptr<parquet::schema::GroupNode> schema = get_parquet_flat_schema();

    // Create the parquet output stream
    shared_ptr<arrow::io::FileOutputStream> outfile;
    PARQUET_ASSIGN_OR_THROW(outfile, arrow::io::FileOutputStream::Open(outputDir));
    parquet::StreamWriter os{parquet::ParquetFileWriter::Open(outfile, schema, props)};
    return os;
}

bool processChunk(int mfemHandle, mfem_mesh_iterator_t* iter, parquet::StreamWriter& pWriter, laghos_mesh_point_t * points, size_t npoints) {

    // Fill the buffer with mfem data
    int rpoints = 0;
    rpoints = mfem_laghos_mesh_read(mfemHandle, iter, points, npoints);

    // Write the data with Arrow Parquet (this must be the slowest possible way)
    for (int i = 0; i < rpoints; i++) {
        laghos_mesh_point_t* pt = points + i;
        pWriter << pt->x << pt->y << pt->z 
                << pt->e << pt->rho 
                << pt->v_x << pt->v_y << pt->v_z
                << pt->element_id 
                << parquet::EndRow;
    }

    // Check for completion
    if (mfem_laghos_mesh_at_end(mfemHandle, iter)) {
        return false;
    }
    return true;
}

int main(int argc, char **argv) {

    string inputDir = argv[1];
    string outputDir = argv[2];

    if (argc < 3 || 0 == inputDir.length() || 0 == outputDir.length()) {
        show_usage(argv[0]);
        return 1;
    }

    // Open the mfem dataset
    int mh = open_mfem_mesh(inputDir);
    if (mh < 0) {
        cerr << "Invalid MFEM Mesh directory\n";
        return 2;
    }

    // Create the empty parquet dataset
    parquet::StreamWriter pWriter = create_flat_parquet_writer(outputDir);

    // Create the processing buffer
    size_t numPoints = DEFAULT_BUF_SZ / sizeof(laghos_mesh_point_t);
    laghos_mesh_point *pointsBuf = new laghos_mesh_point[numPoints];

    // Process the data to convert MFEM data to Parquet data
    mfem_mesh_iterator_t iter;
    bool moreData = processChunk(mh, &iter, pWriter, pointsBuf, numPoints);
    while (moreData) {
        moreData = processChunk(mh, &iter, pWriter, pointsBuf, numPoints);
    }
    pWriter << parquet::EndRowGroup;

    // Cleanup the mesh point buffer
    delete [] pointsBuf;

    cout << "Parquet dataset written to: " << outputDir << endl;
    return 0;
}