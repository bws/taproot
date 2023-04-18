package sci.mfem;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.Pointer;

/** JNA Class that provides access to the MFEM library */
public interface MFEMUtils extends Library {

    /** The singleton instance used to call the MFEM utility functions */
    MFEMUtils INSTANCE = Native.load("mfem-utils", MFEMUtils.class);

    int mfem_open_mesh(String mesh_filename);
    
    int mfem_close_mesh(int mesh_handle);
    
    int mfem_laghos_mesh_open(String mesh_filename, String e_gf_filename, String rho_gf_filename, String v_gf_filename);
    
    int mfem_lagos_mesh_close(int mesh_handle);
    
    int mfem_laghos_mesh_read(int mesh_handle, LongByReference cur, Pointer pointArray, long npoints);

    int mfem_laghos_mesh_at_end(int mesh_handle, LongByReference cur);

    int mfem_laghos_mesh_get_num_elements(int mesh_handle);

    int mfem_laghos_mesh_get_num_points(int mesh_handle);

}
