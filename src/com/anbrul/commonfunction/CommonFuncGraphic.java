package com.anbrul.commonfunction;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class CommonFuncGraphic {

	
	/**
	 * Get the resource drawable, If the canvas is large than the image, the image will be repeat
	 * @param context
	 * @param resID
	 * @return
	 */
	public static Drawable getRepeatDrawableFromResource(Context context, int resID){
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resID);
		BitmapDrawable bg = new BitmapDrawable(bitmap);
		
		bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		
		return bg;
	}
}
