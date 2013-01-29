package com.anbrul.commonfunction.demo.urlspan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

public class WeiboUrlSpan extends ClickableSpan {

    private String mUrl;
    private Context parentContext;

    public WeiboUrlSpan(String url, Context context) {
        mUrl = url;
        parentContext = context;
    }

    @Override
    public void onClick(View widget) {
        if (mUrl.startsWith("@")){
            Bundle bundleUserInfo = new Bundle();
            bundleUserInfo.putLong("user_info", 0);
            bundleUserInfo.putString("userName", mUrl);

            Log.e("string url", "dfasf = " + parentContext);
//            Intent intentInfo = new Intent(parentContext, UserInfo.class);
//            intentInfo.putExtras(bundleUserInfo);
//            intentInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            parentContext.startActivity(intentInfo);
        }else if (mUrl.startsWith("http://")){
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            parentContext.startActivity(myIntent);
        }
        else if (mUrl.startsWith("#")){
        	String[] trend = mUrl.split("#");
//        	WeiboConfig.getWeiboData().mTrendString = trend[1];
//            Intent trendIntent = new Intent(parentContext, HomeTab.class);
//            trendIntent.putExtra("weiboType", WeiboConfig.WEIBO_TYPE.TRENDS.getValue());
//            parentContext.startActivity(trendIntent);	
        }
    }
}
