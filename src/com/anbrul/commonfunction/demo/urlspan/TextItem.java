package com.anbrul.commonfunction.demo.urlspan;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class TextItem{
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_AT = 2;
    public static final int TYPE_SHARP = 3;
    public static final int TYPE_LINK = 4;
    public static final int TYPE_EMOTION = 5;
    public int mType = TYPE_TEXT;
    public int mColor = DEFAULT_COLOR;
    private static int DEFAULT_COLOR = Color.DKGRAY;
    private static int HIGHLIGHT_COLOR = Color.rgb(33, 66, 99);
    public String mText = "";
    public Bitmap mBitmap;
    
    /**
     * Construct a normal item with text
     * @param text
     */
    public TextItem(String text) {
        super();
        mText = text;
        mType = TYPE_TEXT;
    }
    
    public TextItem(String text, int type) {
        mText = text;
        mType = type;
        
        switch (type) {
        case TYPE_TEXT:
            mColor = DEFAULT_COLOR;
            break;
        case TYPE_AT:
            mColor = HIGHLIGHT_COLOR;
            break;
        case TYPE_LINK:
            mColor = HIGHLIGHT_COLOR;
            break;
        case TYPE_SHARP:
            mColor = HIGHLIGHT_COLOR;
            break;
            
        case TYPE_EMOTION:
            mColor = HIGHLIGHT_COLOR;
            break;

        default:
            mColor = DEFAULT_COLOR;
            break;
        }
    }
    
    public TextItem(String text, int type, int color) {
        mText = text;
        mType = type;
        mColor = color;
    }
    
    public TextItem(Bitmap bitmap) {
        if(bitmap != null){
            mBitmap = bitmap;
            mType = TYPE_EMOTION;
        }else{
            Log.w("TextItem", "Create bitmap item failed, bitmap is null!");
            mType = TYPE_TEXT;
        }
    }
    
    public TextItem getHeadItem(int size){
        if(mType == TYPE_EMOTION && mBitmap != null){
            return new TextItem("");
        }
        
        if(size >= mText.length()){
            return this;
        }
        
        String text = mText.substring(0, size);
//        Log.e("Test", "get head:" + text + "�� size = " + size);
        
        return new TextItem(text, TYPE_TEXT, mColor);
    }
    
    public TextItem getTailItem(int size){
        if(mType == TYPE_EMOTION && mBitmap != null){
            return this;
        }
        
        if(size >= mText.length()){
            return null;
        }
        
        String text = mText.substring(size);
//        Log.e("Test", "get tail:" + text + "�� size = " + size);
        
        return new TextItem(text, TYPE_TEXT, mColor);
    }
}
