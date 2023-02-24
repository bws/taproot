package sci.lucene;

import com.sun.jna.NativeLibrary;
import sci.mfem.LaghosMeshReader;
import sci.mfem.MFEMUtils;

public class IndexedMesh {

    public static void main(String[] args) {

        // Set the load path for the native code
        NativeLibrary.addSearchPath("mfem-utils", "/home/bsettlemyer/workspace/glass-ceiling/mfem-utils/build/lib/main/debug");

        String meshDir = "/home/bsettlemyer/workspace/glass-ceiling/data";
        String meshFile = meshDir + "/snoise_60_mesh";
        String eFile = meshDir + "/snoise_60_e";
        String rhoFile = meshDir + "/snoise_60_rho";
        String vFile = meshDir + "/snoise_60_v";
        
        LaghosMeshReader lmr = new LaghosMeshReader();
        System.out.println("Created the reader");

        MFEMUtils.LaghosPoint[] buf = new MFEMUtils.LaghosPoint[16384];
    
        lmr.open(meshFile);
        int count = 0;
        do {
            count = lmr.getLaghosPoints(buf);
        } while (count > 0);
        lmr.close();
        System.out.println("Completed reading the mesh");
    }
}
