package com.anbrul.commonfunction;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public class ImageHelper {
	private static final String TAG = "ImageHelper";
	
	static String ROOT;
	static {
		if(CommonFuncAndroid.isSDCardAvailable()){
			ROOT = Environment.getExternalStorageDirectory().toString();
		}else{
			ROOT = "/data/data/com.laifu.image";
		}
	}
	
	public static String STORE_PATH = ROOT + "/anbrul/images/";
	public static String CACHE_PATH = ROOT + "/anbrul/cache/";
	private static String SHARE_PATH = ROOT + "/anbrul/temp/";

	public static Drawable generateImageDrawable(URL path) throws Exception{
		 byte[] imageData = generateImage(path);
		 if(imageData != null){
			 Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
			 if(bitmap != null){
				 return new BitmapDrawable(bitmap);
			 }
		 }
		 
		 return null;
	 }
	 
	public static File generateImageFile(URL path) throws Exception {
		
		HttpURLConnection conn = null;
		InputStream is = null;
		byte[] imgData = null;
		File file = null;

		try {
			URL url = path;
			String fileName = getFileName(url);
			file = new File(CACHE_PATH, fileName);
			if (file != null && file.exists()) {
				return file;
			} else {
				conn = (HttpURLConnection) url.openConnection();
				is = conn.getInputStream();
				// Get the length
				int length = (int) conn.getContentLength();
				if (length != -1) {
					imgData = new byte[length];
					byte[] temp = new byte[512];
					int readLen = 0;
					int destPos = 0;
					while ((readLen = is.read(temp)) > 0) {
						System.arraycopy(temp, 0, imgData, destPos, readLen);
						destPos += readLen;
					}

					file = writeBitmapToCache(imgData, file);
				}

				if (is != null) {
					is.close();
				}

				if (conn != null) {
					conn.disconnect();
				}
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			throw new Exception("Out of memory!");
		}

		return file;
	}
	
	 public static byte[] generateImage(URL path) throws Exception{
	        HttpURLConnection conn = null;
	        InputStream is = null;
	        byte[] imgData = null;
	        
	        try {
	            URL url = path;
	            String fileName = getFileName(url);
                File file = new File(CACHE_PATH, fileName);
                if (file != null && file.exists()) {
                	Log.d(TAG, "Load image from cache file");
					imgData = getImageFileBytes(file);
                }else{
                	conn = (HttpURLConnection) url.openConnection();
                	is = conn.getInputStream();
                	// Get the length
                	int length = (int) conn.getContentLength();   
                	if (length != -1) {   
                		imgData = new byte[length];   
                		byte[] temp=new byte[512];   
                		int readLen=0;   
                		int destPos=0;   
                		while((readLen=is.read(temp))>0){   
                			System.arraycopy(temp, 0, imgData, destPos, readLen);   
                			destPos+=readLen;   
                		}
                		
                		writeBitmapToCache(imgData, file);
                	}
                	
                	if(is != null){
                		is.close();
                	}
                	
                	if(conn != null){
                		conn.disconnect();
                	}
                }
                
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }catch (OutOfMemoryError e){
	            throw new Exception("Out of memory!");
	        }
	        
	        return imgData;
	    }

	private static String getFileName(URL url) {
		return new MD5().getMD5ofStr(url.toString());
	}
	
	/**
	 * Write the special data to file
	 * 
	 * @param imgData
	 * @param file
	 *            Save data, maybe is SDCard or cache
	 * @author mikewu
	 */
	private static File writeBitmapToCache(byte[] imgData, File file) {
		if(file != null){
			File parent = file.getParentFile();
        	if(parent != null){
        		parent.mkdirs();
        	}
		}

		FileOutputStream fos = null;
		BufferedOutputStream outPutBuffer = null;

		if (file != null) {
			try {
				fos = new FileOutputStream(file);

				outPutBuffer = new BufferedOutputStream(fos);
				outPutBuffer.write(imgData);
				outPutBuffer.flush();
				fos.flush();
				return file;

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}

					if (outPutBuffer != null) {
						outPutBuffer.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	private static byte[] getImageFileBytes(File file) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		if(is == null){
			return null;
		}
		
		ByteArrayOutputStream os = new ByteArrayOutputStream(512);
		byte[] buffer = new byte[512];
		int len;
		try {
			while ((len = is.read(buffer)) >= 0) {
				os.write(buffer, 0, len);
			}
		} catch (java.io.IOException e) {
		}
		
		return os.toByteArray();
	}

}
