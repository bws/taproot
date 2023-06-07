package sci.mfem;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.Platform;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

public class LaghosMeshReader extends MFEMMeshReader {

    /** The path to the mfem shared object */
    static final String SHARED_OBJ_DIR = "/home/bsettlemyer/workspace/taproot/mfem-utils/build/lib/main/debug";

    /** The size of a Laghos struct in bytes */
    final int LAGHOS_POINT_SIZE = (1 * Native.getNativeSize(Long.TYPE)) + (8 * Native.getNativeSize(Double.TYPE));

    /** The default number of points to fetch */
    static final int DEFAULT_FETCH_COUNT = 64*1024;

    @FieldOrder({"x", "y", "z", "e", "rho", "v_x", "v_y", "v_z", "elementId"})
    public class LaghosPoint extends Structure {
        public double x, y, z;
        public double e, rho, v_x, v_y, v_z;
        public long elementId;

        public LaghosPoint(Pointer p) {
            super(p);
            read();
        }

        public LaghosPoint(byte[] bytes) {
            System.out.println("Creating LaghosPoint with bytes len: " + bytes.length);
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            this.x = buf.getFloat(0*8);
            this.y = buf.getFloat(1*8);
            this.z = buf.getFloat(2*8);
            this.e = buf.getFloat(3*8);
            this.rho = buf.getFloat(4*8);
            this.v_x = buf.getFloat(5*8);
            this.v_y = buf.getFloat(6*8);
            this.v_z = buf.getFloat(7*8);
            this.elementId = buf.getLong(8*8);
        }

        public String toString() {
            String s = "Id: " + this.elementId;
            s += "Pos: <" + this.x + "," + this.y + "," + this.z + ">";
            return s; 
        }

    }

    /**
     * Create a LaghosMeshReader
     */
    public LaghosMeshReader(String meshFile, String eFile, String rhoFile, String vFile) {
        // Set the load path for the native code
        NativeLibrary.addSearchPath("mfem-utils", SHARED_OBJ_DIR);
        MFEMUtils lib = MFEMUtils.INSTANCE;
        this.meshHandle = lib.mfem_laghos_mesh_open(meshFile, eFile, rhoFile, vFile);
        this.meshIter = 0;

        // Create a buffer to hold 64k Laghos points (about 4MB worth)
        this.pointsBufferAllocSize = DEFAULT_FETCH_COUNT * LAGHOS_POINT_SIZE;
        this.pointsBuffer = new Memory(this.pointsBufferAllocSize);

        // Describe the current point
        this.pointBegin = 0;
        this.pointCurrent = 0;
        this.pointsBuffered = 0;
    };

    /** Reset the mesh iterator to the first element */
    public void resetIterator() {
        this.meshIter = 0;
        this.pointBegin = 0;
        this.pointCurrent = 0;
        this.pointsBuffered = 0;
    }

    /** @return the number of mesh cells */
    public long getNumElements() {
        MFEMUtils lib = MFEMUtils.INSTANCE;
        return lib.mfem_laghos_mesh_get_num_elements(meshHandle);
    }
    
    /** @return the number of points */
    public int getNumPoints() {
        MFEMUtils lib = MFEMUtils.INSTANCE;
        return lib.mfem_laghos_mesh_get_num_points(meshHandle);
    }
    
    /**
     * Beginning at point 0, return the LaghosPoints in order
     */
    public LaghosPoint getNextLaghosPoint() {
        byte[] bytes = getNextLaghosPointAsBytes();
        LaghosPoint lp = null;
        if (null != bytes) {
            lp = new LaghosPoint(bytes);
        } else {
            System.out.println("No bytes buffered");
        }
        return lp;
    }

    public byte[] getNextLaghosPointAsBytes() {
        // If the next point isn't buffered, fetch a new buffer of points
        //System.err.println("Get Next Point Cur: " +  pointCurrent + " Begin: " + pointBegin + " Buffered: " + pointsBuffered);
        if (this.pointCurrent >= (pointBegin + pointsBuffered)) {
            this.pointsBuffered = fetchLaghosPoints(this.pointsBuffer, DEFAULT_FETCH_COUNT);
            this.pointBegin = this.pointCurrent;
            //System.err.println("Points fetched Current: " + this.pointCurrent + " Begin: " + this.pointBegin + " Count: " + this.pointsBuffered);
        }

        byte[] packedBytes = null;
        if (0 != this.pointsBuffered) {
            long offset = (this.pointCurrent - this.pointBegin) * LAGHOS_POINT_SIZE;
            //System.err.println("Point construct Current: " + this.pointCurrent + " Begin: " + this.pointBegin + " Count: " + this.pointsBuffered + " off:" + offset);
            packedBytes = pointsBuffer.getByteArray(offset, LAGHOS_POINT_SIZE);
            this.pointCurrent++;
        }
        return packedBytes;

    }

    /**
     * This function reads a buffer full of LaghosPoints.
     * 
     * @return the number of points loaded into the pointsBuffer
     */
    protected long fetchLaghosPoints(Pointer pointsBuffer, long bufferSize) {
        MFEMUtils lib = MFEMUtils.INSTANCE;
        long count = 0;
        LongByReference iter = new LongByReference(this.meshIter);
        if (0 == lib.mfem_laghos_mesh_at_end(this.meshHandle, iter)) {
            count = lib.mfem_laghos_mesh_read(this.meshHandle, iter, pointsBuffer, bufferSize);
            pointsBuffered = count;
            meshIter = iter.getValue();
        }
        else {
            //System.err.println("Reached the end of the mesh iter:" + this.meshIter);
        }

        // Count is 0, so set the points buffer to null
        if (0 == count) {
            //System.err.println("Count was 0, setting buf to null");
            pointsBuffer = null;
        }
        return count; 
    }

    // Mesh handle
    public int meshHandle;

    // MeshIterator
    long meshIter;

    // Memory containing a native array of points
    Pointer pointsBuffer;

    // Total size of the buffer
    long pointsBufferAllocSize;

    // Number of points in the points buffer
    long pointsBuffered;

    // Index of the first buffered LaghosPoint
    long pointBegin;

    // Bytes that contain data in the pointsBuffer
    long pointCurrent;
}
