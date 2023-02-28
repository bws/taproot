package sci.lucene;

import java.io.IOException;
import com.sun.jna.NativeLibrary;
import sci.mfem.LaghosMeshReader;
import org.apache.lucene.util.bkd.BKDConfig;
import org.apache.lucene.util.bkd.BKDWriter;


public class IndexedMesh {

    public static void main(String[] args) {

        // Set the load path for the native code
        NativeLibrary.addSearchPath("mfem-utils", "/home/bsettlemyer/workspace/taproot/mfem-utils/build/lib/main/debug");

        String meshDir = "/home/bsettlemyer/workspace/taproot/data";
        String meshFile = meshDir + "/snoise_60_mesh";
        String eFile = meshDir + "/snoise_60_e";
        String rhoFile = meshDir + "/snoise_60_rho";
        String vFile = meshDir + "/snoise_60_v";
        
        LaghosMeshReader lmr = new LaghosMeshReader(meshFile, eFile, rhoFile, vFile);
        System.out.println("Created the reader");

        LaghosMeshReader.LaghosPoint lp = lmr.getNextLaghosPoint();
        //for (int i = 0; i < 65536; i++) {
        //    lp = lmr.getNextLaghosPoint();
        //}
        //System.out.println(lp);
        //lmr.close();

        BKDConfig bCfg = new BKDConfig(9, 8, 8, 10);
        BKDWriter bWriter = new BKDWriter(1024, null, vFile, bCfg, 4, 1024);
        byte[] pointBuffer = lmr.getNextLaghosPointAsBytes();
        try {
            bWriter.add(pointBuffer, 0);
        } catch(IOException e) {
            System.out.println("IOException while adding to BKD tree");
            //throw e;
        }
        System.out.println("Completed reading the mesh");
    }
}
