package sci.lucene;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.sun.jna.NativeLibrary;
import sci.mfem.LaghosMeshReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.bkd.BKDConfig;
import org.apache.lucene.util.bkd.BKDWriter;


public class IndexedMesh {

    final static int TOTAL_LAGHOS_POINTS = 64 * 1024 * 8;
    final static double SECS_PER_NANOSECOND = 1e-9;
    final int LAGHOS_POINT_NDIMS = 9;
    final int BKD_LEAF_POINTS = 128;


    static void reportTime(String desc, long[] times) {
        double elapsedNanos = (double)times[1] - (double)times[0];
        double elapsedSecs = elapsedNanos * SECS_PER_NANOSECOND;
        System.out.println(desc + ": " + elapsedSecs + " secs");
    }

    static long[] runLuceneMultiTrial(LaghosMeshReader lmr) {
        long[] times = new long[2];

        try {
            Directory tmp = FSDirectory.open(Paths.get("/tmp"));
            BKDConfig bCfg = new BKDConfig(LAGHOS_POINT_NDIMS, 8, 8, BKD_LEAF_POINTS);
            BKDWriter bWriter = new BKDWriter(1, tmp, "laghos", bCfg, 4, TOTAL_LAGHOS_POINTS);

            times[0] = System.nanoTime();
            for (int i = 0; i < TOTAL_LAGHOS_POINTS; i++) {
                byte[] pointBuffer = lmr.getNextLaghosPointAsBytes();
                bWriter.add(pointBuffer, 0);
            }
            bWriter.close();
            times[1] = System.nanoTime();
        } catch(IOException e) {
            System.out.println("IOException while adding to BKD tree");
            //throw e;
        }
        System.out.println("Completed indexing the mesh");
        return times;
    }

    static long[] runLuceneSingleTrial(LaghosMeshReader lmr) {
        long[] times = new long[2];

        try {
            Directory tmp = FSDirectory.open(Paths.get("/tmp"));
            BKDConfig bCfg = new BKDConfig(LAGHOS_POINT_NDIMS, 1, 8, BKD_LEAF_POINTS);
            BKDWriter bWriter = new BKDWriter(1, tmp, "laghos", bCfg, 4, TOTAL_LAGHOS_POINTS);

            times[0] = System.nanoTime();
            for (int i = 0; i < TOTAL_LAGHOS_POINTS; i++) {
                byte[] pointBuffer = lmr.getNextLaghosPointAsBytes();
                bWriter.add(pointBuffer, 0);
            }
            bWriter.close();
            times[1] = System.nanoTime();
        } catch(IOException e) {
            System.out.println("IOException while adding to 1 dimension BKD tree");
            //throw e;
        }
        System.out.println("Completed indexing the mesh");
        return times;
    }

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

        long[] times = runLuceneSingleTrial(lmr);
        reportTime("Lucene BKWriter Dims=9 IdxDims=1 MaxPts=1024", times);

        //long[] times = runLuceneMultiTrial(lmr);
        //reportTime("Lucene BKWriter Dims=9 IdxDims=8 MaxPts=1024", times);

    }
}
