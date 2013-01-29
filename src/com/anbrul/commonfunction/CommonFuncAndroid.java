package com.anbrul.commonfunction;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class CommonFuncAndroid {
	
	private static final String TAG = "CommonFuncAndroid";

	/**
     * Check SD card
     * @return true if SD card is mounted
     * @author mikewu
     */
    public static boolean isSDCardAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    } 
    
    /**
     * Dip to pixel
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * Pixel to dip
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    
    /**
     * Get status bar height
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context){
    	DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return (int) Math.ceil( 25 * metrics.density);
    }
    
    /**
     * Show soft keyboard
     * @param editText
     * @author mikewu
     */
    public static void showKeyBoard(final EditText editText){
        Timer timer = new Timer();   
        timer.schedule(new TimerTask(){
  
            @Override  
            public void run() {
                InputMethodManager m = (InputMethodManager)   
                editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);   
                m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);   
            }   
               
        }, 1000);
    }
    
    /**
     * Draw the view into a bitmap.
     */
    public static Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }
        
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }
    
    /**
     * Copy text to clip board
     * @param context
     * @param text
     */
    public static void setClipboardText(Context context, String text) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setText(text);
	}
    
    /**
     * Send a broadcast to update media store, then we can see the media immediately in gallery
     * @param context
     * @param uri
     */
    public static void scanFileToMediaStore(Context context, Uri uri){
    	context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
    }
    
    /**
     * 
     * @param activity
     */
    public static void setFullScreen(Activity activity){
    	activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
    
    /**
     * 
     * @param activity
     */
    public static void quitFullScreen(Activity activity){
        final WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.getWindow().setAttributes(attrs);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
}
