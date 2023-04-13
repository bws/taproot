/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package sci.mfem;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.sun.jna.NativeLibrary;
import sci.mfem.LaghosMeshReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LaghosMeshReaderTest {
    public final static String PROJECT_DATA_DIR = "../data";
    public final static String EXTERNAL_DATA_DIR = "../mesh";

    @Test void TestLaghosMeshReader() {
        System.err.println("Working Directory = " + System.getProperty("user.dir"));
        String meshFile = PROJECT_DATA_DIR + "/1m_mesh/snoise_60_mesh";
        String eFile = PROJECT_DATA_DIR + "/1m_mesh/snoise_60_e";
        String rhoFile = PROJECT_DATA_DIR + "/1m_mesh/snoise_60_rho";
        String vFile = PROJECT_DATA_DIR + "/1m_mesh/snoise_60_v";
        LaghosMeshReader lmr = new LaghosMeshReader(meshFile, eFile, rhoFile, vFile);
        lmr.close();
        assertEquals(0, 0);
    }
    
    @Test void TestGetNextPoint() {
        System.err.println("Working Directory = " + System.getProperty("user.dir"));
        String meshFile = PROJECT_DATA_DIR + "/1m_mesh/snoise_60_mesh";
        String eFile = PROJECT_DATA_DIR + "/1m_mesh/snoise_60_e";
        String rhoFile = PROJECT_DATA_DIR + "/1m_mesh/snoise_60_rho";
        String vFile = PROJECT_DATA_DIR + "/1m_mesh/snoise_60_v";
        LaghosMeshReader lmr = new LaghosMeshReader(meshFile, eFile, rhoFile, vFile);

        // There should be 65536 point
        int count = 0;
        long begin = System.nanoTime();
        LaghosMeshReader.LaghosPoint lp = lmr.getNextLaghosPoint();
        while (null != lp) {
            lp = lmr.getNextLaghosPoint();
            count++;
        }
        count++;
        assertEquals(65536, count);

        long end = System.nanoTime();
        long secs = end - begin;
        System.out.println("Time to iterate over points: " + secs);

        lmr.close();
        assertEquals(0, 0);
    }
    
}
