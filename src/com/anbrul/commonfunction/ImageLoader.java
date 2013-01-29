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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
import android.widget.ImageView;

/**
 * This class is use to load image from network Asynchronously
 * @author MikeWu
 *
 */
public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static int LOCAL_FILE_DECODE_SIZE = 100;
    private static Context appContext = null;
    
    private static String APP_FOLDER = "/hexin";

    private static String ROOT;
    public static String STORE_PATH;
    public static String CACHE_PATH;
    private static String SHARE_PATH;

    static {
        if (CommonFuncAndroid.isSDCardAvailable()) {
            ROOT = Environment.getExternalStorageDirectory().toString();
        } else {
            ROOT = "/data/data/com.kortide.elastospush";
        }

        STORE_PATH = ROOT + APP_FOLDER + "/images/";
        CACHE_PATH = ROOT + APP_FOLDER + "/cache/";
        SHARE_PATH = ROOT + APP_FOLDER + "/share/";
    }

    // If the images are not used frequently, we can remove the "static"
    private static HashMap<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();

    // If we do not use a static cache, we need a constructor
    public ImageLoader() {
    }
    
    /**
     * Set application context for ImageLoader
     * When we load image from network or from file, we need resource to initialize the density information
     * @param context
     */
    public static void setAppContext(Context context){
        appContext = context;
    }
    
    public static void setLocalFileThumbnailHeight(int size){
        LOCAL_FILE_DECODE_SIZE = size;
    }

    public static Drawable loadDrawable(final String imageUrl, final ImageView imageView, final ImageCallback imageCallback) {
        if(imageUrl == null || imageUrl.length() == 0){
            Log.e(TAG, "loadDrawable(), url is null");
            return null;
        }
        
        if (imageCache.containsKey(imageUrl)) {
            // get from cache
            SoftReference<Drawable> softReference = imageCache.get(imageUrl);
            Drawable drawable = softReference.get();
            if (drawable != null) {
//                Log.d(TAG, "loadDrawable(), find url in cache");
                return drawable;
            }else{
                Log.d(TAG, "loadDrawable(), find url, but the cache has been cleared!");
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
            return ImageHelper.generateImageDrawable(url, false);
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
    
    /**
     * Get image bitmap with fixed size directly to avoid OOM
     * 
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
            if (bitmap != null && bitmap.getHeight() != outSize) {
                float scaleHeight = ((float) outSize) / bitmap.getHeight();
                Matrix matrix = new Matrix();
                matrix.postScale(scaleHeight, scaleHeight);

                Bitmap source = bitmap;
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                
                if(bitmap != source && !source.isRecycled()){
                    source.recycle();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap resizeBitmap(Bitmap orignal, int reqWidth, int reqHeight) {
        int width = orignal.getWidth();
        int height = orignal.getHeight();

        float scaleWidth = ((float) reqWidth) / width;
        float scaleHeight = ((float) reqHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(orignal, 0, 0, width, height, matrix, true);
    }

    public static class ImageHelper {
        public static Drawable generateImageDrawable(String path, boolean byteWay) throws Exception {
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
                return new BitmapDrawable(appContext != null ? appContext.getResources() : null, bitmap);
            }
            
            return null;
        }

        public static File checkCacheFile(String path) {
            String fileName = getFileName(path);
            File file = new File(CACHE_PATH, fileName);
            if (file != null && file.exists()) {
                return file;
            } else {
                return null;
            }
        }

        public static File generateImageFile(String path) throws Exception {

            HttpURLConnection conn = null;
            InputStream is = null;
            byte[] imgData = null;
            File file = null;

            try {
                URL url = new URL(path);
                String fileName = getFileName(path);
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
        
        private static boolean islocalStoragePath(String path){
            if(path.startsWith("/mnt/sdcard") || path.startsWith("/sdcard")){
                return true;
            }else{
                return false;
            }
        }

        public static Bitmap generateImageBitmap(String path) throws Exception {
            HttpURLConnection conn = null;
            InputStream is = null;
            Bitmap bitmap = null;
            
            try {
                String fileName = getFileName(path);
                File file = new File(CACHE_PATH, fileName);
                
                if (islocalStoragePath(path)) {
                    if (new File(path).exists()) {
                        if (isImageFile(path)) {
                            bitmap = getFixedHeightImage(path, LOCAL_FILE_DECODE_SIZE);
                        } else if (isVideoFile(path)) {
                            bitmap = getVideoThumbnail(path);
                        } else {
                            Log.e(TAG, "Unknown file type, path=" + path);
                        }
                    } else {
                        Log.e(TAG, "File not exist, path=" + path);
                    }
                } else if (file != null && file.exists()) {
                    Log.d(TAG, "Load image from cache file");
                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                } else {
                    URL url = new URL(path);
                    Log.d(TAG, "Load image from network");
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

        private static Bitmap getVideoThumbnail(String path) {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, Thumbnails.MICRO_KIND);  // 96*96
//            Bitmap bitmap1=ThumbnailUtils.extractThumbnail(bitmap,200,200);
            Log.d(TAG, "Get video thumbnail bitmap:" + bitmap + "size = " + bitmap.getWidth() + ":" + bitmap.getHeight());
            return bitmap;
        }

        private static boolean isImageFile(String path) {
            return FileType.isImage(new File(path));
        }
        
        private static boolean isVideoFile(String path) {
            if(path.toLowerCase().endsWith(".mp4")){
                return true;
            }
            
            return false;
        }

        public static byte[] generateImage(String path) throws Exception {
            HttpURLConnection conn = null;
            InputStream is = null;
            byte[] imgData = null;

            try {
                String fileName = getFileName(path);
                File file = new File(CACHE_PATH, fileName);
                if (file != null && file.exists()) {
                    Log.d(TAG, "Load image from cache file");
                    imgData = getImageFileBytes(file);
                } else {
                    URL url = new URL(path);
                    Log.d(TAG, "Load image from network");
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

        private static String getFileName(String url) {
            return new MD5().getMD5ofStr(url);
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
            } catch (IOException e) {
            }

            return os.toByteArray();
        }

    }
}
