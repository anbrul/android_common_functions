package com.anbrul.commonfunction.downloader;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class DownloadStatusNotification {
	
	public static final int  MSG_DOWNLOAD = 1;

	private ArrayList<DownloadStatus> mDownloadList;
	private NotificationManager mNotificationManager;
	private Activity mContext;
	public DownloadStatusNotification(Activity context){
		
		mDownloadList = new ArrayList<DownloadStatus>();
		this.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mContext = context;
	}
	
	public void setNotificationInfo(String filename, int id, int percent) {	
		DownloadStatus temp = new DownloadStatus(filename, id);
		DownloadStatus ds = null;
		int index = mDownloadList.indexOf(temp);
		if(-1 == index){
			addNotification(temp);
			ds = temp;
		}else
			ds = mDownloadList.get(index);
		
		if(percent >= 100){
			ds.notif.flags=0;
//			ds.notif.contentView.setImageViewResource(R.id.image, R.drawable.down_over);
//			ds.notif.contentView.setTextViewText(R.id.file_name, ds.fileName);
//			ds.notif.contentView.setProgressBar(R.id.down_pb, 100, 100, false);
//			ds.notif.contentView.setTextViewText(R.id.progress_values, mContext.getString(R.string.statusbar_download_finished));
			ds.notif.flags = Notification.FLAG_AUTO_CANCEL;
			ds.notif.contentIntent = getPendingIntent(ds.id);
		}else{
//			ds.notif.contentView.setImageViewResource(R.id.image, R.drawable.down_in);
//			ds.notif.contentView.setTextViewText(R.id.file_name, ds.fileName);
//			ds.notif.contentView.setProgressBar(R.id.down_pb, 100, percent, false);			
//			ds.notif.contentView.setTextViewText(R.id.progress_values, percent + "%");
		}
		
		mNotificationManager.notify(ds.id, ds.notif);
	}

	public void setNotifictionError(int id, String err) {
		DownloadStatus temp = new DownloadStatus("", id);
		int index = mDownloadList.indexOf(temp);
		if(index == -1) return;

		DownloadStatus ds = mDownloadList.get(index);
		ds.notif.flags=0;
//		ds.notif.contentView.setImageViewResource(R.id.image, R.drawable.download_error);
//		ds.notif.contentView.setViewVisibility(R.id.down_pb, View.INVISIBLE);
//		if(err == null) {
//			ds.notif.contentView.setTextViewText(R.id.progress_values, mContext.getString(R.string.statusbar_download_error));
//		}
//		else {
//			ds.notif.contentView.setTextViewText(R.id.progress_values, mContext.getString(R.string.statusbar_download_error)+" " + err);		
//		}
		
		ds.notif.flags = Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(ds.id, ds.notif);
	}
	
	public void addNotification(DownloadStatus ds){
		Notification notif = new Notification();
		
//		RemoteViews remoteView = new RemoteViews(mContext.getPackageName(), R.layout.statusbar);
//        remoteView.setImageViewResource(R.id.image, R.drawable.down_in);
//    	remoteView.setProgressBar(R.id.down_pb, 100, 0, false);
//    	remoteView.setTextViewText(R.id.progress_values, "0%");
//    	remoteView.setTextViewText(R.id.file_name, ds.fileName);
//    	//remoteView.setTextViewText(R.id.time, "10");
//    	
//    	notif.flags = Notification.FLAG_NO_CLEAR;
//    	notif.icon = R.drawable.kuai_mes_ind;
//    	notif.contentView = remoteView;
    	notif.contentIntent = getPendingIntent(-1);
//        mNotificationManager.notify(ds.id, notif);
        
        ds.notif = notif;
        mDownloadList.add(ds);
	}
	
	private PendingIntent getPendingIntent(long dbid) {
		Intent it = new Intent();
//		it.setClass(mContext, ListCatActivity.class);
		
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 2, it,
				//		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
		PendingIntent.FLAG_UPDATE_CURRENT);
		return contentIntent;
	}

	public void cancelNotifiction(int fileID) {
		mNotificationManager.cancel(fileID);
	}
}
