package com.anbrul.commonfunction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;

public class CommonFuncBitmap {
	
    private static final String TAG = null;

	/**
	 * Rotate a bitmap
     * @author mikewu
     * @param b
     * @param degrees
     * @return
     * @throws Exception 
     */
    public static Bitmap rotateBitmap(Bitmap b, int degrees){
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle(); // Release Bitmap after operation
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                Log.e(TAG, "rotateBitmap failed, out of memory!");
//                return b; // OutOfMemoryError, return b
            }
        }
        return b;
    }
    
    /**
     * Save a bitmap to file
     * @param bitmap
     * @param filePath
     * @return
     * @author mikewu
     */
    public static boolean saveTempBitmap(Bitmap bitmap, String filePath){
        if(bitmap == null || filePath == null || filePath.length() == 0){
            Log.e(TAG, "saveTempBitmap(), illegal param, bitmap = " + bitmap + "filename = " + filePath);
            return false;
        }
        
        File bitmapFile = new File(filePath);
        
        if(bitmapFile.isDirectory()){
        	Log.e(TAG, "Create new file failed, It's a directory:" + filePath);
        	return false;
        }
        
        CommonFuncFile.makesureFileFolderExist(bitmapFile);
        
        try {
        	if(!bitmapFile.createNewFile()){
        		Log.e(TAG, "Create new file failed");
        		return false;
        	}
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
        if(!bitmapFile.exists()){
        }
        
        FileOutputStream bitmapWriter;
        boolean retValue = false;
        try {
            bitmapWriter = new FileOutputStream(bitmapFile);
            if(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bitmapWriter)) {   
                Log.d("TAG","Save picture successfully! file name = " + filePath);   
                bitmapWriter.flush();
                bitmapWriter.close();
                retValue = true;
            }   
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return retValue;
    }
    
	/**
     * Save bitmap data to file
     * @param imgData
     * @param file
     * @return
     */
    public static boolean saveBitmapDataToFile(byte[] imgData, String filePath) {
    	if(filePath == null || filePath.length() == 0){
            Log.e(TAG, "Illegal file path:" + filePath);
            return false;
        }
        
        File bitmapFile = new File(filePath);
        if(!bitmapFile.isFile()){
        	return false;
        }
    		
    	return CommonFuncFile.writeBitmapDataToFile(imgData, bitmapFile);
    }
    
    /**
     * Try to decode a image file with file path
     * @param filePath image file path
     * @param quanlity the compress rate
     * @param autoCompress if need to compress more if OOM occurs
     * @return the decoded bitmap or null if failed
     * @author mikewu
     */
    public static Bitmap tryToDecodeImageFile(String filePath, int quanlity, boolean autoCompress){
        Bitmap bitmap = null;
        try {
            if(quanlity == 1){
                bitmap = BitmapFactory.decodeFile(filePath);
            }else{
                BitmapFactory.Options options = new Options();
                options.inSampleSize = quanlity;
                bitmap = BitmapFactory.decodeFile(filePath, options);
            }
        } catch (OutOfMemoryError oe) {
            if(autoCompress){
                int rate = (quanlity >= 4) ? 2 : 4;
                Log.d(TAG, "Decode the file automatically with quanlity :" + quanlity * rate);
                bitmap = tryToDecodeImageFile(filePath, quanlity * rate, false);
            }else{
                Log.e(TAG, "Decode the file failed!, out of memory!");
                oe.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        
        return bitmap;
    }
    
    /**
     * Create a bitmap of the view
     * @param v
     * @return
     */
    public static Bitmap getViewBitmap(View v) {
    	return CommonFuncAndroid.getViewBitmap(v);
    }
    
    /**
     * Get image bitmap with fixed size directly to avoid OOM
     * @param filePath
     * @param outSize
     * @author: Mike Wu
     * @return
     */
    public static Bitmap getFixedHeightImage(String filePath, int outSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = null;
        try {
            BitmapFactory.decodeFile(filePath, options); 
            options.inJustDecodeBounds = false; 
            int scale = (int) (options.outHeight / outSize);
            options.inSampleSize = scale;                                       
            bitmap = BitmapFactory.decodeFile(filePath, options);
            
            // if we need a real fixed image we need to resize the bitmap
            // Here we just check the height, check the width if you want
            if(bitmap.getHeight() != outSize){
                bitmap = resizeBitmap(bitmap, outSize, outSize, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch(Error e){
            e.printStackTrace();
        }
        return bitmap;
    }
    
    public static Bitmap resizeBitmap(Bitmap source, int reqWidth, int reqHeight, int reqRotate){
        int width = source.getWidth();
        int height = source.getHeight();

        float scaleWidth = ((float) reqWidth) / width;
        float scaleHeight = ((float) reqHeight) / height;
        
        Matrix matrix = new Matrix();
        
        matrix.postScale(scaleWidth, scaleHeight);
        
        if(reqRotate != 0){
            matrix.postRotate(reqRotate);
        }
        
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);
        
        if(bitmap != source)
        source.recycle();
        
        return bitmap;
    }
}
