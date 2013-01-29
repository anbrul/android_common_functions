package com.anbrul.commonfunction;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class AsyncImageLoader {
    private static final String TAG = "AsyncImageLoader";
    private static String DEFAULT_APP_FOLDER = "MyCommonfunction";

    private static String ROOT;
    public static String STORE_PATH;
    public static String CACHE_PATH;
    private static String SHARE_PATH;

    static {
        setAppname(DEFAULT_APP_FOLDER);
    }

    // If the images are not used frequently, we can remove the "static"
    private static HashMap<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();

    // If we do not use a static cache, we need a constructor
    public AsyncImageLoader() {
    }
    
    public static void setAppname(String appName){
        if (CommonFuncAndroid.isSDCardAvailable()) {
            ROOT = Environment.getExternalStorageDirectory().toString();
        } else {
            ROOT = "/data/data/com.anbrul.commonfunction";
        }

        STORE_PATH = ROOT + "/" + DEFAULT_APP_FOLDER + "/images/";
        CACHE_PATH = ROOT + "/" + DEFAULT_APP_FOLDER + "/cache/";
        SHARE_PATH = ROOT + "/" + DEFAULT_APP_FOLDER + "/share/";
    }

    public static Drawable loadDrawable(final String imageUrl, final ImageView imageView, final ImageCallback imageCallback) {
        Log.d(TAG, "loadDrawable(), url = " + imageUrl);
        if (imageCache.containsKey(imageUrl)) {
            // get from cache
            SoftReference<Drawable> softReference = imageCache.get(imageUrl);
            Drawable drawable = softReference.get();
            if (drawable != null) {
                Log.w(TAG, "loadDrawable(), find url incache: " + imageUrl);
                return drawable;
            }
        }

        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                imageCallback.imageLoaded((Drawable) message.obj, imageView, imageUrl);
            }
        };

        // create a new thread to get image
        new Thread() {
            @Override
            public void run() {
                Drawable drawable = loadImageFromUrl(imageUrl);
                if (drawable != null) {
                    imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
                    Message message = handler.obtainMessage(0, drawable);
                    handler.sendMessage(message);
                }
            }
        }.start();

        return null;
    }

    private static Drawable loadImageFromUrl(String url) {

        if (url == null || url.length() == 0) {
            return null;
        }

        try {
            return ImageHelper.generateImageDrawable(new URL(url), false);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Down load bitmap from net and save it to cache
     * 
     * @param urlString
     *            The address of bitmap
     * @param file
     *            Bitmap will been saved path
     * @return null indicator down load bitmap is fail
     */
    private Bitmap loadImageFromUrl(String urlString, boolean cache) {
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        File file = new File(CACHE_PATH + urlString);

        try {
            URL url = new URL(urlString);
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

                // Save to cache
                if (file != null) {
                    // writeBitmapToCache(imgData, file);
                }
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

    // callback
    public interface ImageCallback {
        public void imageLoaded(Drawable imageDrawable, ImageView imageView, String imageUrl);
    }

    public static class ImageHelper {

        private static final String TAG = "ImageHelper";

        public static Drawable generateImageDrawable(URL path, boolean byteWay) throws Exception {
            Bitmap bitmap = null;
            if(byteWay){
                byte[] imageData = generateImage(path);
                
                if (imageData != null) {
                    bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                }
            }else{
                bitmap = generateImageBitmap(path);
            }

            if (bitmap != null) {
                return new BitmapDrawable(bitmap);
            }
            
            return null;
        }

        public static File checkCacheFile(URL path) {
            String fileName = getFileName(path);
            File file = new File(CACHE_PATH, fileName);
            if (file != null && file.exists()) {
                return file;
            } else {
                return null;
            }
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
                    conn.setReadTimeout(60000);
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
                file = null;
                e.printStackTrace();
            } catch (IOException e) {
                file = null;
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
                file = null;
                throw new Exception("Out of memory!");
            }

            return file;
        }

        public static Bitmap generateImageBitmap(URL path) throws Exception {
            HttpURLConnection conn = null;
            InputStream is = null;
            Bitmap bitmap = null;
            
            try {
                URL url = path;
                String fileName = getFileName(url);
                File file = new File(CACHE_PATH, fileName);
                if (file != null && file.exists()) {
                    Log.d(TAG, "Load image from cache file, url = " + path);
                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                } else {
                    conn = (HttpURLConnection) url.openConnection();
                    is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    if (bitmap != null) {
                        writeBitmapToCache(bitmap, file);
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
                throw new Exception("Out of memory! url = " + path);
            }
            
            return bitmap;
        }
        
        public static byte[] generateImage(URL path) throws Exception {
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

                        writeBitmapToCache(imgData, file);
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

            return imgData;
        }

        private static String getFileName(URL url) {
            return new MD5().getMD5ofStr(url.toString());
        }

        private static File writeBitmapToCache(Bitmap bitmap, File file) {
            if (file != null) {
                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
            }
            
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 85, new FileOutputStream(file));
                return file;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            
            return null;
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
            if (file != null) {
                File parent = file.getParentFile();
                if (parent != null) {
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

            if (is == null) {
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
}
