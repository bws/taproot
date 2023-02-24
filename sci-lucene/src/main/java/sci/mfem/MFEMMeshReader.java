package sci.mfem;

import sci.mfem.MFEMUtils;

public class MFEMMeshReader {
    

    public MFEMMeshReader() {
    }

    public void open(String mesh_filename) {
        MFEMUtils lib = MFEMUtils.INSTANCE;
        this.mh = lib.mfem_open_mesh(mesh_filename);
    }

    public void close() {
    }

    private int mh;
}
