package com.anbrul.commonfunction.downloader;

public class DownloadTask {
	public static final int STATE_WAIT = 1;
	public static final int STATE_DOWNLOADING = 2;
	public static final int STATE_FINISHED = 3;
	public static final int STATE_FAILED = 4;
	public static final int STATE_CANCELED = 5;
	public static final int STATE_FAILED_NETWORK = 6;
	public static final int STATE_DESTINATION_ERROR = 7;
	public static final int STATE_PAUSED = 8;
	
	int fileID;
	int state;
	int progress;
	String url;
	String message;
	String locpath;
	String filename;
	
	public DownloadTask(int fileID, String url, String locpath, String filename) {
		this.fileID = fileID;
		this.url = url;
		this.state = STATE_WAIT;
		this.progress = 0;
		this.locpath = locpath;
		this.filename=filename;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
		    return true;
		}
		
		if(o instanceof DownloadTask){
			if(this.url.equalsIgnoreCase(((DownloadTask)o).url)){
				return true;
			}
		}
		
		return false;
	}

	public void resetState() {
		state = STATE_WAIT;
	}
}
