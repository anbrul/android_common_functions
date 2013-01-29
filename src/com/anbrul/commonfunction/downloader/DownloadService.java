package com.anbrul.commonfunction.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class provide download functions. <br>
 * See {@link #getInstance(Activity)} <br>
 * See {@link #addDownloadTask(int, String, String, String)} <br>
 * See {@link #cancelDownloadingTask(DownloadTask)}<br>
 * See {@link #setDestinationFolder(String)}<br>
 * See {@link #addDownLoadListener(DownloadListener)}
 * @author mikewu
 */
public class DownloadService {
	private static final int BUFFER_SIZE = 1024;
	private static final String TEMP_FILE = ".info";
	private static final String TAG = "DownloadService";
	
	protected static final int MSG_DOWNLOAD_START = 1000;
	protected static final int MSG_DOWNLOAD_ERROR = 1001;
	protected static final int MSG_DOWNLOAD_DOWNLOADING = 1002;
	
    public static String PATH_LOCAL_DOWNLOAD;
    public static final String APP_PTAH_NAME = "anbrul";
	
	static {
		PATH_LOCAL_DOWNLOAD = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_PTAH_NAME + "/";
	}
	
	/**Max download task count can not set to larger than this number*/
	private static final int MAX_COUNT = 20;
	
	/**User set max download task count*/
	private static int mMaxDownloadThreadCount = 3;
	
	/**Downloading thread count*/
	private static Integer mDownloadingThreadCount = 0;
	
	/**The single download service instance*/
	private static DownloadService mDownloadService;
	
	/**Download tasks*/
	private static List<DownloadTask> mTaskList = new ArrayList<DownloadTask>();
	
	/**The tasks in downloading state*/
	private static List<DownloadTask> mDownloadingTask = new ArrayList<DownloadTask>();
	
	private static DownloadStatusNotification mNotify;

	/**Download state listener*/
	private List<DownloadListener> downloadListener = new ArrayList<DownloadListener>(); 
	
	private String mFolderPath = PATH_LOCAL_DOWNLOAD;

	private DownloadService() {
	}

	/**
	 * Get Download Service instance 
	 * @param notifyActivity Download notification activity, if do not need, set to null
	 * @return DownloadService instance
	 */
	public static DownloadService getInstance(Activity notifyActivity){
		if (mDownloadService == null){
			mDownloadService = new DownloadService();
		}
		
		if (notifyActivity != null){
			mNotify = new DownloadStatusNotification(notifyActivity);
		}
		
		return mDownloadService;
	}
	
	/**
	 * Set download destination folder for all task
	 * @param folerPath
	 */
	public void setDestinationFolder(String folerPath){
		this.mFolderPath = folerPath;
	}
	
	/**
	 * Get max download task count
	 * @return max download task count (1-20)
	 */
	public int getMaxDownloadTaskCount(){
		return mMaxDownloadThreadCount;
	}
	
	/**
	 * Set max download task count
	 * @param count 1-20, default is 3
	 * @return new max download task count
	 */
	public int setMaxDownloadTaskCount(int count){
		if(count < 1){
			throw new IllegalArgumentException("Max down load task count can not be less than 1; try to set to " + count + "failed!");
		}
		
		if(count > MAX_COUNT){
			count = MAX_COUNT;
		}
		
		// Count not change
		if(count == mMaxDownloadThreadCount){
			return count;
		}
		
		mMaxDownloadThreadCount = count;
		Log.d(TAG, "Set max down load thread to " + count);
		
		if(mMaxDownloadThreadCount < mDownloadingThreadCount){
			Log.d(TAG, "Need to pause a thread, max count = " + mMaxDownloadThreadCount + ", downloadCount = " + mDownloadingThreadCount + ", Downloading task size = " + mDownloadingTask.size());
			for(int i = mMaxDownloadThreadCount; i < mDownloadingTask.size(); i++){
				pauseDownloadTask(mDownloadingTask.get(i));
			}
		}else{
			// Create new thread to download waiting task
			while((mMaxDownloadThreadCount > mDownloadingThreadCount) && getWaitingTask() != null){
				startNewDownload();
			}
		}
		
		return count;
	}
	
	/**
	 * Handle download state messages
	 */
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_DOWNLOAD_START:
				if(msg.obj instanceof DownloadTask){
					DownloadTask task = (DownloadTask) msg.obj;
					if(task!= null) {
						for (int i = 0; i < downloadListener.size(); i++) {
							downloadListener.get(i).onTaskStart(task.fileID, task.url);
						}
					}
				}
				break;
			case MSG_DOWNLOAD_ERROR:
				if(msg.obj instanceof DownloadTask){
					DownloadTask task = (DownloadTask) msg.obj;
	
					for (int i = 0; i < downloadListener.size(); i++) {
						downloadListener.get(i).onError(task.fileID, task.url, task.state, task.message); 
					}
					
					if(mNotify != null){
						if(task.state == DownloadTask.STATE_CANCELED) {
							mNotify.cancelNotifiction(task.fileID);
						}
						else {
							mNotify.setNotifictionError(task.fileID, task.message);
						}
					}
				}

				break;
				
			default:
				// Message for each file by fileID
				if(msg.arg2 == MSG_DOWNLOAD_DOWNLOADING){
					if(msg.obj instanceof DownloadTask){
						
						DownloadTask task = (DownloadTask) msg.obj;
						int progress = msg.arg1;
						
						if (mNotify != null) {
							mNotify.setNotificationInfo(task.filename, task.fileID, progress);
						}
						
						for (int i = 0; i < downloadListener.size(); i++) {
							downloadListener.get(i).onDownloading(task.fileID, task.url, progress); 
						}
					}
				}
				break;
			}
			
		}
		
	};
	
	/**
	 * Set a listener to listen the download state
	 * @param listener
	 */
	public void addDownLoadListener(DownloadListener listener){
		this.downloadListener.add(listener);
	}
	
	public void removeDownloadListener(DownloadListener listener) {
		this.downloadListener.remove(listener);
	}
	
	/**
	 * Add a download task, if the task already finished in this instance, it will be ignored;
	 * If the task is in the download list and not finished, it will be the 
	 * @param fileID
	 * @param url
	 * @return true if add success
	 * @throws IllegalAccessException 
	 */
	public synchronized boolean addDownloadTask(int fileID, String url, String locapath, String filename) throws IllegalAccessException{
		if(url == null || url.length() == 0){
			throw new IllegalArgumentException("Url illegal: value=" + url);
		}
		
		if(filename == null || filename.length() == 0){
			throw new IllegalArgumentException("File name empty, name = " + filename);
		}
		
		DownloadTask task = new DownloadTask(fileID, url, locapath, filename);
		
		if(mTaskList.contains(task)){
			// Found the same task in list
			DownloadTask savedTask = mTaskList.get(mTaskList.indexOf(task));
			
			if(savedTask.fileID != fileID){
				throw new IllegalAccessException("Url exist, but file id not the same");
			}
					
			// Task finished
			if(savedTask.state == DownloadTask.STATE_FINISHED){
				Log.d(TAG, "Task already finished! task = " + task.filename);
				return false;
			}else{
				savedTask.resetState();
			}
		}else{
			synchronized(mTaskList){
				mTaskList.add(task);
			}
		}
		
		startNewDownload();
		
		return true;
	}

	private void startNewDownload() {
		// Download thread not start, start now
		if(mDownloadingThreadCount < mMaxDownloadThreadCount){
			synchronized(mDownloadingThreadCount){
				mDownloadingThreadCount++;
				Thread thread = new DownloadThread();
				Log.d(TAG, "Start new download thread id = " + thread.getId());
				thread.start();
			}
		}else{
			Log.d(TAG, "Download thread got to max value, waiting!");
		}
	}
	
	public synchronized boolean cancelDownloadTask(int fileID, String url, String locapath, String filename){
		if(url == null || url.length() == 0){
			throw new IllegalArgumentException("Url illegal: value=" + url);
		}
		
		DownloadTask task = new DownloadTask(fileID, url, locapath, filename);
		
		if(!mTaskList.contains(task)){
			// Task not exist
			Log.d(TAG, "Task not exist, cancel failed! url = " + url);
			return false;
		}
		
		int index = mTaskList.indexOf(task);
		if(index == -1){
			return false;
		}
		
		task = mTaskList.get(index);
		if(task.state == DownloadTask.STATE_DOWNLOADING){
			return cancelDownloadingTask(task);
		}else{
			// Not in downloading state, remove directly
			mTaskList.remove(index);
			return true;
		}
	}
	
	/**
	 * Cancel the download task, It's asynchronous
	 * @param task
	 * @return
	 */
	private boolean cancelDownloadingTask(DownloadTask task) {
		task.state = DownloadTask.STATE_CANCELED;
		return true;
	}
	
	/**
	 * Pause the downloading task
	 * @param task
	 * @return true if the task is downloading, false otherwise
	 */
	private boolean pauseDownloadTask(DownloadTask task){
		if(task.state == DownloadTask.STATE_DOWNLOADING){
			Log.d(TAG, "Pause task " + task.fileID + "--" + task.filename);
			task.state = DownloadTask.STATE_PAUSED;
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Download listener
	 * @author mikewu
	 */
	public static interface DownloadListener{
		public abstract void onTaskStart(int fileID, String url);
		public abstract void onDownloading(int fileID, String url, int progress);
		public abstract void onError(int fileID, String url, int errorCode, String reason);
	}

	/**
	 * Download thread
	 * @author mikewu
	 */
	private class DownloadThread extends Thread{
		@Override
		public void run() {
			while(mMaxDownloadThreadCount >= mDownloadingThreadCount){
				DownloadTask task = getWaitingTask();
				if(task != null){
					synchronized(mDownloadingTask){
						// Task not null and not in downloading list
						if(!mDownloadingTask.contains(task)){
							Log.d(TAG, "Start download task:" + task.fileID + "--" + task.filename + "; Thread id = " + Thread.currentThread().getId());
							mDownloadingTask.add(task);
						}else{
							continue;
						}
					}
					
					downloadTask(task);
					
					synchronized(mDownloadingTask){
						mDownloadingTask.remove(task);
					}
				}else{
					// No waiting task
					break;
				}
			}
			
			synchronized(mDownloadingThreadCount){
				mDownloadingThreadCount--;
			}
			
			Log.d(TAG, "Thread end, Thread id = " + Thread.currentThread().getId());
		}
	}

	/**
	 * Get a task in the list which is waiting for download
	 * @return
	 */
	private DownloadTask getWaitingTask() {
		synchronized(mTaskList){
			for(int i = 0; i < mTaskList.size(); i++){
				DownloadTask task = mTaskList.get(i);
				if(mTaskList.get(i).state == DownloadTask.STATE_WAIT ||
						mTaskList.get(i).state == DownloadTask.STATE_PAUSED){
					return task;
				}
			}
		}
		
		Log.d(TAG, "No waiting task, all download task were finished! thread = " + Thread.currentThread().getId());
		return null;
	}

	/**
	 * Download the specified task
	 * @param task
	 */
	private int downloadTask(DownloadTask task) {
		if (task.state == DownloadTask.STATE_FINISHED) {
			return 0;
		}

		HttpURLConnection conn = null;
		InputStream is = null;
		RandomAccessFile outputFile = null;
		RandomAccessFile tempInfoFile = null;
		String errorMsg = "";
		
		String outputFileName;
		String tempFileName;
		
		// Prepare for download
		try {
			outputFileName = generateDestinationFilePath(task);
			if(!createFile(outputFileName)){
				// Update task state, otherwise the task will still in download waiting list
				task.state = DownloadTask.STATE_DESTINATION_ERROR;
				errorMsg = "Create file failed:" + outputFileName;
				System.out.println(errorMsg);
				task.message = errorMsg;
				Message msg = mHandler.obtainMessage(MSG_DOWNLOAD_ERROR, task);
				mHandler.sendMessage(msg);
				return -1;
			}
			
			tempFileName = outputFileName + TEMP_FILE;
			if (!createFile(tempFileName)){
				task.state = DownloadTask.STATE_DESTINATION_ERROR;
				errorMsg = "Create file failed:" + tempFileName;
				System.out.println(errorMsg);
				task.message = errorMsg;
				Message msg = mHandler.obtainMessage(MSG_DOWNLOAD_ERROR, task);
				mHandler.sendMessage(msg);
				return -1;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			return -2;
		}
		
		// Start download
		try {
			task.state = DownloadTask.STATE_DOWNLOADING;
			
			// Notify
			Message msg = mHandler.obtainMessage(MSG_DOWNLOAD_START, task);
			mHandler.sendMessage(msg);
			
			URL url = new URL(task.url);
			conn = (HttpURLConnection) url.openConnection();
			outputFile = new RandomAccessFile(outputFileName, "rw");
			tempInfoFile = new RandomAccessFile(tempFileName, "rw");
			
			// Set download offset
			int destPos = 0;
			try {
				destPos = tempInfoFile.readInt();
				Log.d(TAG, "Got download state, already download size:" + destPos);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			conn.setRequestProperty("User-Agent", "NetFox");
			String sProperty = "bytes=" + destPos + "-";
			conn.setRequestProperty("RANGE", sProperty);
			conn.setReadTimeout(30000);
			conn.setConnectTimeout(30000);
			outputFile.seek(destPos);
			
			// Check response code
            int responseCode = conn.getResponseCode();
            
            if (responseCode != 200) {
                throw new Exception("Connect to server error, response code = " + responseCode);
            }
			
			// connect streams
			is = conn.getInputStream();

			// Get the length
			int length = conn.getContentLength();
			Log.d(TAG, "File length:" + length);
			int totalLength = destPos + length;
			
			// Start read
			if (length != -1) {
			 // We can not use the length to compute the percentage info.
                Log.w(TAG, "Server return a -1 length.");
			}
			
			byte[] buffer = new byte[BUFFER_SIZE];
			int readLen = 0;
			int progress = 0;
			
			while (checkTaskState(task) && (readLen = is.read(buffer)) > 0) {
				outputFile.write(buffer, 0, readLen);
				destPos += readLen;
				tempInfoFile.seek(0);
				tempInfoFile.writeInt(destPos);
				// Notify
				progress = (int)(100.0 * destPos / totalLength);
				if(progress > task.progress){
					task.progress = progress;
					// Remove the message in the queue first
					if(progress < 100){
						mHandler.removeMessages(task.fileID);
					}
					// Send a new message
					msg = mHandler.obtainMessage(task.fileID, task);
					msg.arg1 = progress;
					msg.arg2 = MSG_DOWNLOAD_DOWNLOADING;
					mHandler.sendMessage(msg);
				}
			}

			if (is != null) {
				is.close();
			}

			if (outputFile != null) {
				outputFile.close();
			}
			
			if(tempInfoFile != null){
				tempInfoFile.close();
				new File(tempFileName).delete();
			}

			if (conn != null) {
				conn.disconnect();
			}
			
			task.state = DownloadTask.STATE_FINISHED;
			Log.d(TAG, "Download success: task id = " + task.fileID + ", task name = " + task.filename + "; Thread id = " + Thread.currentThread().getId());
			return 0;
		} catch (MalformedURLException e) {
			errorMsg = e.getMessage();
			e.printStackTrace();
			task.state = DownloadTask.STATE_FAILED;
		} catch (IOException e) {
			errorMsg = e.getMessage();
			e.printStackTrace();
			task.state = DownloadTask.STATE_FAILED_NETWORK;
		} catch(CancelException e){
			errorMsg = e.getMessage();
			e.printStackTrace();
			task.state = DownloadTask.STATE_CANCELED;
		} catch (PauseException e){
			errorMsg = e.getMessage();
			e.printStackTrace();
			task.state = DownloadTask.STATE_PAUSED;
		} catch (Exception e) {
			task.state = DownloadTask.STATE_FAILED;
		}
		
		// Notify
		if(task.state != DownloadTask.STATE_PAUSED){
			task.message = errorMsg;
			Message msg = mHandler.obtainMessage(MSG_DOWNLOAD_ERROR, task);
			mHandler.sendMessage(msg);
			Log.d(TAG, "Download failed: task = " + task.filename + "\n" + errorMsg);
		}

		return 1;
	}

	/**
	 * Delete error file, not used now
	 * @param outputFileName
	 */
	private void deleteErrorFile(String outputFileName) {
		if(outputFileName != null){
			File file = new File(outputFileName);
			if(file.exists()){
				file.delete();
			}
		}
	}
	
	/**
	 * Check task state before download a block of data to buffer
	 * @param task
	 * @return
	 * @throws PauseException 
	 * @throws RuntimeException
	 */
	private boolean checkTaskState(DownloadTask task) throws CancelException, PauseException{
		if(task.state == DownloadTask.STATE_CANCELED){
			// Stop downloading
			throw new CancelException("Task canceled: task = " + task.filename);
		}
		
		if(task.state == DownloadTask.STATE_PAUSED){
			// Stop downloading
			throw new PauseException("Task paused: task = " + task.filename);
		}
		
		return true;
	}

	/**
	 * Generate destination file path
	 * @param task
	 * @return
	 */
	private String generateDestinationFilePath(DownloadTask task) {
		if(!mFolderPath.endsWith("/")){
			mFolderPath += "/";
		}
		
		return task.locpath;
	}

	/**
     * Create a new file, if the file exist, will delete it
     * 
     * @param filePath
     * @return true if File exist
     */
	private static boolean createFile(String filePath) {
		// Mike Wu: 2011-10-26
		// Check file name valid or not
		if (!isFileNameValid(filePath)){
			return false;
		}
		
		File file = new File(filePath);

		if (file.exists()) {
			Log.d(TAG, "File exist:" + filePath);
			return true;
		}else{
			try {
				File parent = file.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}
				
				Log.d(TAG, "Create file:" + filePath);
				
				return file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return false;
		}
	}
	
	/**
	 * Check the file name. the file name must contain file extension
	 * @param filePath
	 * @return true if file name is valid
	 */
	private static boolean isFileNameValid(String filePath){
		if(!filePath.endsWith(".") && !filePath.endsWith("/")){
			int pos = filePath.lastIndexOf("/");
			
			if(pos != -1){
				return filePath.substring(pos).contains(".");
			}
		}
		
		return false;
	}

	//cancel all download task.
	public boolean cancelAllDownloadTask() {
		boolean downloading = false;
		synchronized(mTaskList) {
			for (int i = 0; i < mTaskList.size(); i++) {
				DownloadTask task = mTaskList.remove(i);
				if (task.state == DownloadTask.STATE_DOWNLOADING) {
					downloading = true;
					cancelDownloadingTask(task);
				}
			}
		}
		return downloading;
	}
	
	class PauseException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = -828786611887847429L;
		
		public PauseException(String cause) {
			super(cause);
		}
	}
	
	class CancelException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4010620710378058123L;

		public CancelException(String cause) {
			super(cause);
		}

	}
}
