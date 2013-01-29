package com.anbrul.commonfunction.demo.share;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

public class ShareImage {
	
	private static final String TAG = "ShareImage";

	public static void shareImage(Activity activity, Uri imageUri){
        final Intent intent = new Intent();
        
        // We need to share this as a text string.
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        boolean start = true;
        if(start){// Start directly
        	activity.startActivity(intent);
        }else{// Query activities for use
        	// Query the system for matching activities.
        	PackageManager packageManager = activity.getPackageManager();
        	List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        	int numActivities = activities.size();
        	Log.d(TAG, "numActivities = " + numActivities);
        	
        	for (int i = 0; i < numActivities; ++i) {
        		final ResolveInfo info = activities.get(i);
        		
        		String label = info.loadLabel(packageManager).toString();
        		Drawable icon = info.loadIcon(packageManager);
        	}
        }

	}

}
