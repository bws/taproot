package sci.mfem;

public class LaghosMeshReader extends MFEMMeshReader {

    /**
     * Create a LaghosMeshReader
     */
    public LaghosMeshReader() {};

    /**
     * @return the number of LaghosPoints in the ByteBuffer
     */
    public int getLaghosPoints(MFEMUtils.LaghosPoint[] points) { 
        return 0; 
    }
    
}
