package com.anbrul.commonfunction;

import java.lang.reflect.Field;
import java.util.HashMap;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CommonFuncDevice {
	
	public static AppInfo getAppInfo(Context context) {
		AppInfo info = null;
		
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
			
			if (pi != null) {
				info = new AppInfo();
				info.packageName = context.getPackageName();
				info.versionCode = pi.versionCode + "";
				info.versionName = pi.versionName == null ? "None" : pi.versionName;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return info;
	}
	
	/**
	 * Get IMEI
	 * @Hint Requires Permission: {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
	 * @param context
	 * @return
	 */
	public static String getIMEI(Context context){
		return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId(); 
	}
	
	/**
	 * Returns the unique subscriber ID, for example, the IMSI for a GSM phone. 
	 * Return null if it is unavailable.
	 * @Hint Requires Permission: {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
	 * @param context
	 * @return IMSI for GSM phone
	 */
	public static String getIMSI(Context context){
		return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId(); 
	}
	
	public static HashMap<String, String> getDeviceInfo(Context context) {
		HashMap<String, String> mDeviceInfo = new HashMap<String, String>();
		Field[] fields = Build.class.getDeclaredFields();
		
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				mDeviceInfo.put(field.getName(), field.get(null).toString());
				Log.d("Device info", "key:" + field.getName() + "; value:" + field.get(null).toString());
			} catch (Exception e) {
				Log.e("MoreTab", "Error while collect crash info", e);
			}
		}

		String t = android.provider.Settings.System.getString(context.getContentResolver(), "android_id");
		Log.d("Device info", "key:" + "android_id" + "; value:" + t);

		return mDeviceInfo;
	}
	
	public static class AppInfo{
		String packageName;
		String versionName;
		String versionCode;
	}
}
