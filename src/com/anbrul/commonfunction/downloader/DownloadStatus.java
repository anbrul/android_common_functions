package com.anbrul.commonfunction.downloader;

import android.app.Notification;

public class DownloadStatus {
	public boolean isFinish;
	public int percent;
	public int id;
	public String fileName;
	public Notification notif;
	public DownloadStatus(String fileName, int id){
		this.fileName = fileName;
		this.id = id;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(this == o){
			return true;
		}
		
		if(o instanceof DownloadStatus){
			if(((DownloadStatus)o).id == id){
				return true;
			}
		}
		
		return false;
	}
	
	
	
}
