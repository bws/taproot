package sci.mfem;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;


public interface MFEMUtils extends Library {

    MFEMUtils INSTANCE = Native.load("mfem-utils", MFEMUtils.class);

    int mfem_open_mesh(String mesh_filename);
    
    int mfem_close_mesh(int mesh_handle);
    
    int mfem_read_mesh(int mesh_handle);

    @FieldOrder({"elementId", "x", "y", "z", "e", "rho", "v"})
    public class LaghosPoint extends Structure {
        public int elementId;
        public double x, y, z;
        public double e, rho, v;
    }

}
