package com.anbrul.commonfunction.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;

/**
 * A customized popup window
 * @author MikeWu
 *
 */
public class TipWindow {
	static final public int STYLE_MSG = 0;
	static final public int STYLE_NUM = 1;
	static final public int STYLE_OPER = 2;
    private static final int MSG_CLOSE_SELF = 1000;
	
	private Context mContext = null;
	private View mParentView = null;
	private PopupWindow mPopupWindow = null;
	private View vPopupWindow = null;
	private View mLinearLayout = null;
	private int mGravity = Gravity.TOP|Gravity.LEFT;
	private int mWidth = LayoutParams.WRAP_CONTENT;
	private int mHeight = LayoutParams.WRAP_CONTENT;
	private int mAction = -1;
	private int mStyle = 0;
	private int mDelaytime = 2000; 
	private int mResBackgroud = -1;
	private AutoCloseHandler mAutoCloseHandler = new AutoCloseHandler();
	
	public TipWindow(View view, View contentView){
		mContext = view.getContext();
		mParentView = view;
		vPopupWindow = contentView;
	}
	
	public void setGravity(int gravity){
		mGravity = gravity;
	}
	
	public void setSize(int width, int height){
		if (STYLE_MSG == mStyle){
			mWidth = LayoutParams.FILL_PARENT;
		}else{
			mWidth = width;
		}
		
		mHeight = height;
	}
	
	public void setBackgroud(int resID){
		mResBackgroud = resID;
		
		if (mResBackgroud > 0){
			mLinearLayout.setBackgroundResource(mResBackgroud);
		}
	}
	
	public void setStyle(int style){
		mStyle = style;
		
		switch (mStyle){
			default:
				break;
				
			case STYLE_MSG:
				mWidth = LayoutParams.FILL_PARENT;
				break;
				
			case STYLE_NUM:
				mGravity = Gravity.BOTTOM|Gravity.LEFT;
				break;
				
			case STYLE_OPER:
				break;
		}
	}
	
	public void setDelayTime(int delayTime){
		if (delayTime >= 0){
			mDelaytime = delayTime;
		}
	}
	
	public void show(int x, int y){
		mPopupWindow = new PopupWindow(vPopupWindow, mWidth, mHeight, false);
		mPopupWindow.setOutsideTouchable(true);
		
		switch (mStyle){
			default:
				break;
				
			case STYLE_MSG:
				break;
				
			case STYLE_NUM:
				break;
				
			case STYLE_OPER:
				mPopupWindow.setFocusable(true); // This make the widget in the popup window can be clicked
				mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Set the background of the pop up window, make the window can be touched and so that it can be dismissed while touch outside
				break;
		}

		mPopupWindow.showAtLocation(mParentView, mGravity, x, y);
		
		if (STYLE_OPER != mStyle && mDelaytime > 0){
			mAutoCloseHandler.sendEmptyMessageDelayed(0, mDelaytime);
		}
	}
	
	public int getActionID(){
		return mAction;
	}
	
	public void close(){
		if (mPopupWindow.isShowing()){
			mPopupWindow.dismiss();
			
			if (null != mAutoCloseHandler){
				mAutoCloseHandler.removeMessages(0);
			}
		}
	}
	
	class AutoCloseHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_CLOSE_SELF:
                if (mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                }
                break;
            default:
                super.handleMessage(msg);
                break;
            }
        }
	}
}
