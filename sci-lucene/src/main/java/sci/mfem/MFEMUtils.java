package sci.mfem;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;


public interface MFEMUtils extends Library {

    /** The singleton instance used to call the MFEM utility functions */
    MFEMUtils INSTANCE = Native.load("mfem-utils", MFEMUtils.class);

    int mfem_open_mesh(String mesh_filename);
    
    int mfem_close_mesh(int mesh_handle);
    
    int mfem_open_laghos_mesh(String mesh_filename, String e_gf_filename, String rho_gf_filename, String v_gf_filename);
    
    int mfem_close_lagos_mesh(int mesh_handle);
    
    int mfem_read_laghos_mesh(int mesh_handle, Pointer cur, Pointer pointArray, long npoints);


}
