package com.anbrul.commonfunction;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public abstract class WelcomeActivity extends Activity {
	
	private static final int MAX_SHOWTIME = 10000;

	private static final int MIN_SHOWTIME = 1000;

	private int mShowTime = 2000;
	
	private Class<? extends Activity> mDestinationActivity = null;
	
	Handler mHandler = new Handler(){
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}else{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setFullScreen(this);
		
		// Set show time
		int time = getShowTime();
		if(time > MIN_SHOWTIME && time < MAX_SHOWTIME){
			mShowTime = time;
		}
		
		//
		mDestinationActivity = getDestinationActivity();
		
		setContentView(getContentView());
				
		mHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				onActivityDismiss();
			}
		}, mShowTime);
	}

	/**
	 * Override this method to set the view for the window
	 * @return
	 */
	abstract protected View getContentView();
	
	/**
	 * Set show time in ms, defaut is 2000ms
	 * @return show time, 1000 < time < 10000
	 */
	abstract protected int getShowTime();	
	
	/**
	 * Call this function to set a destination activity
	 * @param a
	 */
	abstract protected Class<? extends Activity> getDestinationActivity();
	
	private void onActivityDismiss(){
		if(mDestinationActivity != null){
			Intent intent = new Intent(this, mDestinationActivity);
			startActivity(intent);
		}
		
		finish();
	}
	
	public static void setFullScreen(Activity activity) {
		activity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

}
