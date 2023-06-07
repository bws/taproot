package sci.lucene;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.sun.jna.NativeLibrary;
import sci.mfem.LaghosMeshReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.apache.lucene.store.OutputStreamIndexOutput;
import org.apache.lucene.util.bkd.BKDConfig;
import org.apache.lucene.util.bkd.BKDWriter;
import org.iq80.leveldb.*;
import org.fusesource.leveldbjni.JniDBFactory;


public class IndexedMesh {

    final static int TOTAL_LAGHOS_POINTS = 64 * 1024 * 8;
    final static double SECS_PER_NANOSECOND = 1e-9;
    final static int LAGHOS_POINT_NDIMS = 9;
    final static int BKD_LEAF_POINTS = 128;


    static void reportTime(String desc, long[] times) {
        double elapsedNanos = (double)times[1] - (double)times[0];
        double elapsedSecs = elapsedNanos * SECS_PER_NANOSECOND;
        System.out.println(desc + ": " + elapsedSecs + " secs");
    }

    static long writeIndex(BKDWriter w, String filename) {
        final File metaFile = new File(filename + ".tpm");
        final File dataFile = new File(filename + ".tpd");
        final File idxFile = new File(filename + ".tpi");
        try {
            metaFile.createNewFile();
            dataFile.createNewFile();
            idxFile.createNewFile();
            
            OutputStreamIndexOutput metaOut = new OutputStreamIndexOutput("metadata", 
                                                                         metaFile.getName(), 
                                                                         new FileOutputStream(metaFile),
                                                                         4*1024*1024);
            OutputStreamIndexOutput dataOut = new OutputStreamIndexOutput("data",
                                                                         dataFile.getName(),
                                                                         new FileOutputStream(dataFile),
                                                                         4*1024*1024);
            OutputStreamIndexOutput idxOut = new OutputStreamIndexOutput("resource", 
                                                                         filename, 
                                                                         new FileOutputStream(idxFile), 
                                                                         4*1024*1024);
            Runnable progress = w.finish(metaOut, idxOut, dataOut);
            Thread t = new Thread(progress);
            long begin, end;
            try {
                begin = System.nanoTime();
                t.start();
                t.join();
                end = System.nanoTime();
                double outputSecs = (double)(end - begin)/(1000*1000*1000);
                System.out.println("Index output time: " + outputSecs + " size: " + dataFile.length()/(1024*1024) + "MiB");
        
            }
            catch (InterruptedException e) {
                System.err.println("Output thread interrupted");
            }
        }
        catch (IOException e) {
            System.err.println("Exception during writeOutput");
        }

        return dataFile.length();
    }

    static long[] runLuceneMultiTrial(LaghosMeshReader lmr) {
        long[] times = new long[2];

        try {
            long totalPoints = lmr.getNumPoints();
            Directory tmp = FSDirectory.open(Paths.get("/tmp"));
            BKDConfig bCfg = new BKDConfig(LAGHOS_POINT_NDIMS, 8, 8, BKD_LEAF_POINTS);
            BKDWriter bWriter = new BKDWriter((int)totalPoints, tmp, "laghos", bCfg, 4, totalPoints);

            times[0] = System.nanoTime();
            byte[] pointBuffer = lmr.getNextLaghosPointAsBytes();
            int docId = 0;
            while (pointBuffer != null) {
                bWriter.add(pointBuffer, docId++);
                pointBuffer = lmr.getNextLaghosPointAsBytes();
            }
            // Now write the BKD to disk and determine its size on disk
            long numBytes = writeIndex(bWriter, "multi-trial");

            bWriter.close();
            times[1] = System.nanoTime();


        } catch(IOException e) {
            System.out.println("IOException while adding to BKD tree");
            //throw e;
        }
        System.out.println("Completed indexing the mesh");
        return times;
    }

    static long[] runLuceneUniTrial(LaghosMeshReader lmr) {
        long[] times = new long[2];

        try {
            long totalPoints = lmr.getNumPoints();
            Directory tmp = FSDirectory.open(Paths.get("/tmp"));
            BKDConfig bCfg = new BKDConfig(LAGHOS_POINT_NDIMS, 1, 8, BKD_LEAF_POINTS);
            BKDWriter bWriter = new BKDWriter((int)totalPoints, tmp, "laghos", bCfg, 4, totalPoints);

            times[0] = System.nanoTime();
            byte[] pointBuffer = lmr.getNextLaghosPointAsBytes();
            int dId = 0;
            while (pointBuffer != null) {
                bWriter.add(pointBuffer, dId++);
                pointBuffer = lmr.getNextLaghosPointAsBytes();
            }
            // Now write the BKD to disk and determine its size on disk
            //long numBytes = writeOutput(bWriter, "uni-trial");

            bWriter.close();
            times[1] = System.nanoTime();


        } catch(IOException e) {
            System.out.println("IOException while adding to BKD tree");
            //throw e;
        }
        System.out.println("Completed indexing the mesh");
        return times;
    }

    static long[] runLevelDBUniTrial(LaghosMeshReader lmr) {
        long[] times = new long[2];

        Options options = new Options();
        options.createIfMissing(true);
        options.blockSize(4*1024*1024);
        options.writeBufferSize(4*1024*1024);
        File dbFile = new File("/tmp/leveldb.tpl");
        try {
            dbFile.delete();
            DB db = JniDBFactory.factory.open(dbFile, options);
            try {
                long totalPoints = lmr.getNumPoints();
                byte[] pointBuffer = lmr.getNextLaghosPointAsBytes();
                times[0] = System.nanoTime();
                while (pointBuffer != null) {
                    ByteBuffer keyBuf = ByteBuffer.wrap(pointBuffer, 0, 8);
                    ByteBuffer valBuf = ByteBuffer.wrap(pointBuffer, 64, 8);
                    // Use energy as the key and element id as the value/pointer
                    db.put(keyBuf.array(), valBuf.array());
                    pointBuffer = lmr.getNextLaghosPointAsBytes();
                }
            } finally {
                db.close();
                times[1] = System.nanoTime();
            }

            System.out.println("Size of db: " + dbFile.length());
            //dbFile.delete();
        } catch (IOException e) {
            System.err.println("IOException during leveldb uni");
        } 

        return times;
    }

    static long[] runLevelDBMultiTrial(LaghosMeshReader lmr) {
        long[] times = new long[2];

        Options options = new Options();
        options.createIfMissing(true);
        options.blockSize(4*1024*1024);
        options.writeBufferSize(4*1024*1024);
        File dbFile = new File("/tmp/leveldb.tpl");
        try {
            dbFile.delete();
            DB db = JniDBFactory.factory.open(dbFile, options);
            try {
                long totalPoints = lmr.getNumPoints();
                byte[] pointBuffer = lmr.getNextLaghosPointAsBytes();
                times[0] = System.nanoTime();
                while (pointBuffer != null) {
                    ByteBuffer d1Buf = ByteBuffer.wrap(pointBuffer, 0, 8);
                    ByteBuffer d2Buf = ByteBuffer.wrap(pointBuffer, 8, 8);
                    ByteBuffer d3Buf = ByteBuffer.wrap(pointBuffer, 16, 8);
                    ByteBuffer d4Buf = ByteBuffer.wrap(pointBuffer, 24, 8);
                    ByteBuffer d5Buf = ByteBuffer.wrap(pointBuffer, 32, 8);
                    ByteBuffer d6Buf = ByteBuffer.wrap(pointBuffer, 40, 8);
                    ByteBuffer d7Buf = ByteBuffer.wrap(pointBuffer, 48, 8);
                    ByteBuffer d8Buf = ByteBuffer.wrap(pointBuffer, 56, 8);
                    ByteBuffer valBuf = ByteBuffer.wrap(pointBuffer, 64, 8);
                    // Use energy as the key and element id as the value/pointer
                    db.put(d1Buf.array(), valBuf.array());
                    db.put(d2Buf.array(), valBuf.array());
                    db.put(d3Buf.array(), valBuf.array());
                    db.put(d4Buf.array(), valBuf.array());
                    db.put(d5Buf.array(), valBuf.array());
                    db.put(d6Buf.array(), valBuf.array());
                    db.put(d7Buf.array(), valBuf.array());
                    db.put(d8Buf.array(), valBuf.array());
                    pointBuffer = lmr.getNextLaghosPointAsBytes();
                }
            } finally {
                db.close();
                times[1] = System.nanoTime();
            }

            System.out.println("Size of db: " + dbFile.length());
            //dbFile.delete();
        } catch (IOException e) {
            System.err.println("IOException during leveldb uni");
        } 

        return times;
    }

    static LaghosMeshReader loadMesh() {
        String meshDir = "/home/bsettlemyer/workspace/mesh";
        String meshName = "1m";
        meshName = "30m";
        //String meshName = "250m";
        String meshFile = meshDir + "/" + meshName + "/" + meshName + "_60_mesh";
        String eFile = meshDir + "/" + meshName + "/" + meshName + "_60_e";
        String rhoFile = meshDir + "/" + meshName + "/" + meshName + "_60_rho";
        String vFile = meshDir + "/" + meshName + "/" + meshName + "_60_v";
        long times[] = new long[2];
        times[0] = System.nanoTime();
        LaghosMeshReader lmr = new LaghosMeshReader(meshFile, eFile, rhoFile, vFile);
        times[1] = System.nanoTime();
        reportTime(meshName + " mesh loaded: ", times);
        return lmr;
    }
    public static void main(String[] args) {

        // Set the load path for the native code
        NativeLibrary.addSearchPath("mfem-utils", "/home/bsettlemyer/workspace/taproot/mfem-utils/build/lib/main/debug");


        System.out.println("Loading mesh ...");
        LaghosMeshReader lmr = loadMesh();
        System.out.println("Complete");

        //long[] timeSingle = runLuceneUniTrial(lmr);
        //reportTime("Lucene BKWriter Dims=9 IdxDims=1", timeSingle);
        //lmr.resetIterator();

        //long[] timeMulti = runLuceneMultiTrial(lmr);
        //reportTime("Lucene BKWriter Dims=9 IdxDims=8", timeMulti);
        //lmr.resetIterator();

        //long[] timeLevelUni = runLevelDBUniTrial(lmr);
        //reportTime("LevelDB Dims=1 IdxDims=1", timeLevelMulti);
        //lmr.resetIterator();

        long[] timeLevelMulti = runLevelDBMultiTrial(lmr);
        reportTime("LevelDB Dims=1 IdxDims=8", timeLevelMulti);
        lmr.resetIterator();
    }
}
