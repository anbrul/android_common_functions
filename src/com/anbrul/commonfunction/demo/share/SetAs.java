package com.anbrul.commonfunction.demo.share;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

public class SetAs {
	
	private static final String TAG = "ShareImage";

	public static void shareImage(Activity activity, Uri imageUri){
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setDataAndType(imageUri, "image/jpeg");
        intent.putExtra("mimeType", "image/jpeg");
        
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
