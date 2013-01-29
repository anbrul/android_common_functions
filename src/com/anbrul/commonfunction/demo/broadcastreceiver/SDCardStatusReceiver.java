package com.anbrul.commonfunction.demo.broadcastreceiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;

public class SDCardStatusReceiver {
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)){
				//
			}else if(intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)){
				//
			}
			
		}
	};
	
	public void registerBroadcastReceiver(Activity activity){
		// install an intent filter to receive SD card related events.
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
		intentFilter.addDataScheme("file");
		activity.registerReceiver(mReceiver, intentFilter);
	}
}
