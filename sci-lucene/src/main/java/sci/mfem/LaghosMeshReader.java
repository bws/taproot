package sci.mfem;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Platform;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

public class LaghosMeshReader extends MFEMMeshReader {

    /** The size of a Laghos struct in bytes */
    final int LAGHOS_POINT_SIZE = (1 * Native.getNativeSize(Long.TYPE)) + (8 * Native.getNativeSize(Double.TYPE));

    @FieldOrder({"elementId", "x", "y", "z", "e", "rho", "v_x", "v_y", "v_z"})
    public class LaghosPoint extends Structure {
        public int elementId;
        public double x, y, z;
        public double e, rho, v_x, v_y, v_z;

        public LaghosPoint(Pointer p) {
            super(p);
            read();
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
        MFEMUtils lib = MFEMUtils.INSTANCE;
        this.meshHandle = lib.mfem_open_laghos_mesh(meshFile, eFile, rhoFile, vFile);
        this.meshCurrent = new IntByReference(0);

        // Create a buffer to hold 1M Laghos points
        this.pointsBufferAllocSize = 1024 * 1024 * LAGHOS_POINT_SIZE;
        this.pointsBuffer = new Memory(this.pointsBufferAllocSize);

        // Describe the current point
        this.pointBegin = 0;
        this.pointCurrent = 0;
        this.pointsBuffered = 0;
    };

    /**
     * Beginning at point 0, return the LaghosPoints in order
     */
    public LaghosPoint getNextLaghosPoint() {
        // Fetch the next batch of LaghosPoints
        if (this.pointCurrent >= (pointBegin + pointsBuffered)) {
            this.pointBegin += this.pointsBuffered;
            this.pointsBuffered = getLaghosPoints(this.pointsBuffer, this.pointsBufferAllocSize);
        }

        LaghosPoint lp = null;
        if (0 != this.pointsBuffered) {
            long offset = this.pointCurrent * LAGHOS_POINT_SIZE;
            lp = new LaghosPoint(pointsBuffer.share(offset));
            this.pointCurrent++;
        }
        return lp;
    }

    public byte[] getNextLaghosPointAsBytes() {
        // Fetch the next batch of LaghosPoints
        if (this.pointCurrent >= (pointBegin + pointsBuffered)) {
            this.pointBegin += this.pointsBuffered;
            this.pointsBuffered = getLaghosPoints(this.pointsBuffer, this.pointsBufferAllocSize);
        }

        byte[] packedBytes = null;
        if (0 != this.pointsBuffered) {
            long offset = this.pointCurrent * LAGHOS_POINT_SIZE;
            packedBytes = pointsBuffer.getByteArray(offset, LAGHOS_POINT_SIZE);
            this.pointCurrent++;
        }
        return packedBytes;

    }

    /**
     * This function reads a buffer full of LaghosPoints.
     * 
     * @return the number of points written into the pointsBuffer
     */
    protected long getLaghosPoints(Pointer pointsBuffer, long bufferSize) {
        MFEMUtils lib = MFEMUtils.INSTANCE;
        long count = lib.mfem_read_laghos_mesh(this.meshHandle, this.meshCurrent.getPointer(), pointsBuffer, bufferSize);

        // Count is 0, so set the points buffer to null
        if (0 == count) {
            pointsBuffer = null;
        }

        return count; 
    }

    // Mesh handle
    public int meshHandle;

    // MeshIterator
    IntByReference meshCurrent;

    // Memory containing a native array of points
    Pointer pointsBuffer;

    // Total size of the buffer
    long pointsBufferAllocSize;

    // Bytes that contain data in the pointsBuffer
    long pointsBuffered;

    // Index of the first buffered LaghosPoint
    int pointBegin;

    // Bytes that contain data in the pointsBuffer
    long pointCurrent;
}
