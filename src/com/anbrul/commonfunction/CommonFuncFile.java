package com.anbrul.commonfunction;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

public class CommonFuncFile {
	
	private static final String TAG = "CommonFuncFile";

	/**
     * Get file size string
     * @param size
     * @return
     * @author mikewu
     */
    public static String calculateFileSize(long size) {
        if (size < 1024l) {
            return size + "B";
        } else if (size < (1024l * 1024l)) {
            return Math.round((size * 100 >> 10)) / 100.00 + "KB";
        } else if (size < (1024l * 1024l * 1024l)) {
            return (Math.round((size * 100 >> 20)) / 100.00) + "MB";
        } else {
            return Math.round((size * 100 >> 30)) / 100.00 + "GB";
        }
    }
    
    /**
     * Check the file parent folder exist or not, if not, create it.
     * @param bitmapFile
     * @return
     */
    public static boolean makesureFileFolderExist(File bitmapFile) {
    	File parent = bitmapFile.getParentFile();
    	
    	 if(parent != null && !parent.exists()){
         	if(!parent.mkdirs()){
         		// folder exist or can not be created
         		Log.d(TAG, "Folder exist or can not be created:" + bitmapFile.getAbsolutePath());
         		return false;
         	}
         }
    	 
    	 return true;
	}
    
    /**
     * Create a folder with full permission
     * @param path
     * @return
     */
    public static boolean createWholePermissionFolder(String path){
        Log.d(TAG, "+ createWholePermissionFolder()");
    
        Process p;
        int status = -1;
        boolean isSuccess = false;
        
        try {
            File destDir = new File(path);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            p = Runtime.getRuntime().exec("chmod 777 " + destDir);
            status = p.waitFor();
            if (status == 0) {
                Log.d(TAG, "Modify folder permission success!");
                isSuccess = true;
            } else {
                Log.e(TAG, "Modify folder permission fail!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Modify folder permission exception!: " + e.toString());
        }
    
        Log.d(TAG, "- createWholePermissionFolder()");
        return isSuccess;
    }
    
	/**
	 * Write the image data to file
	 * 
	 * @param imgData
	 * @param file  Save data, maybe is SDCard or cache
	 */
	public static boolean writeBitmapDataToFile(byte[] imgData, File file) {
		FileOutputStream fos = null;
		BufferedOutputStream outPutBuffer = null;

		boolean retValue = false;
		
		if (file != null) {
			try {
				fos = new FileOutputStream(file);

				outPutBuffer = new BufferedOutputStream(fos);
				outPutBuffer.write(imgData);
				outPutBuffer.flush();
				fos.flush();
				retValue  = true;

			} catch (IOException e) {
				Log.e(TAG, e.toString());
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}

					if (outPutBuffer != null) {
						outPutBuffer.close();
					}
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			}
		}
		
		return retValue;
	}

}
