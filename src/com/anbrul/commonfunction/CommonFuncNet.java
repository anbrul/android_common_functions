package com.anbrul.commonfunction;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CommonFuncNet {
	
	public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null) {
            if (activeNetInfo.isConnected()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
	
	/**
	 * Down load bitmap from network, see {@link #loadImageDataFromUrl(URL)}
	 * @param urlString
	 * 		The address of bitmap
	 * @param file
	 * 		Bitmap will been saved path
	 * @return
	 * 		null indicator down load bitmap is fail
	 */
	public static Bitmap loadImageFromUrl(URL url) {
		Bitmap bitmap = null;
		HttpURLConnection conn = null;
		InputStream is = null;

		try {
			conn = (HttpURLConnection) url.openConnection();
			is = conn.getInputStream();
			// Get the length
			int length = (int) conn.getContentLength();
			if (length != -1) {
				byte[] imgData = new byte[length];
				byte[] temp = new byte[512];
				int readLen = 0;
				int destPos = 0;
				while ((readLen = is.read(temp)) > 0) {
					System.arraycopy(temp, 0, imgData, destPos, readLen);
					destPos += readLen;
				}
				
				bitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
			}

			if (is != null) {
				is.close();
			}

			if (conn != null) {
				conn.disconnect();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}

		return bitmap;
	}
	
	/**
	 * Load image data from network, see {@link #loadImageFromUrl(URL)}
	 * @param path
	 * @return
	 */
	public static byte[] loadImageDataFromUrl(URL path){
        HttpURLConnection conn = null;
        InputStream is = null;
        byte[] imgData = null;
        
        try {
            URL url = path;
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
            }
            
            if(is != null){
                is.close();
            }
            
            if(conn != null){
                conn.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (OutOfMemoryError e){
        	// OOM
        }
        
        return imgData;
    }

}
