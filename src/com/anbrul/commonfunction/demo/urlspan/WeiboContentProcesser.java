package com.anbrul.commonfunction.demo.urlspan;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;

public class WeiboContentProcesser {
    
    private static TextItem getString(String content, char head){
        if(head == 'H'){
            return getHTTPLinkString(content);
        }else if(head == '#'){
            return getSharpString(content);
        }else if(head == '@'){
            return getAtString(content);
        }else if(head == '['){
            return getEmotionString(content);
        }else{
            return null;
        }
    }
    
    private static TextItem getHTTPLinkString(String str){
        String expr = "http://[0-9a-zA-Z/:\\.?&=_]+";
        String ret = getMatchedString(str, expr);
        
        if(ret != null){            
//            System.out.println("Got http link:" + ret);
            return new TextItem(ret, TextItem.TYPE_LINK);
        }else{
            return null;
        }
    }
    
    /**
     * Search the matched string begin with the index 0
     * @param str
     * @param regex
     * @return
     */
    public static String getMatchedString(String str, String regex){
        String ret = null;
        
        Pattern pattern = Pattern.compile(regex); 
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()){
            if(matcher.start() == 0){
                ret = matcher.group();
            }
        }
        
        return ret;
    }
    
    private static TextItem getEmotionString(String str){
        String expr = "\\[[0-9a-zA-Z\u4e00-\u9fa5]+\\]";
        String ret = getMatchedString(str, expr);
        
        if(ret != null){
//            System.out.println("Got Emotion:" + ret);
            return new TextItem(ret, TextItem.TYPE_EMOTION);
        }else{
            return null;
        }
    }
    
    private static TextItem getSharpString(String str){
        
        String expr = "#[0-9a-zA-Z\u4e00-\u9fa5]+#";
        String ret = getMatchedString(str, expr);
        
        if(ret != null){
//            System.out.println("Got #:" + ret);
            return new TextItem(ret, TextItem.TYPE_SHARP);
        }else{
            return null;
        }
        
    }
    
    private static TextItem getAtString(String str){
        String expr = "@[0-9a-zA-Z\u4e00-\u9fa5_\\-]+";
//        String expr = "@[[^@\\s%s]0-9]{1,20}";  // From Sina
        String ret = getMatchedString(str, expr);
        
        if(ret != null){
            // http link do not included after @
            int index = ret.indexOf("http");
            if(index != -1){ // find "http"
                if(index == 1){ // http is follow @
                    ret = null;
                }else{
                    ret = ret.substring(0, index);
                }
            }
        }
        
        if(ret != null){
//            System.out.println("Got @ link:" + ret);
            return new TextItem(ret, TextItem.TYPE_AT);
        }else{
            return null;
        }
    }
    
    public static List<TextItem> splitWeibo(Context context, String content){
        List<TextItem> items = splitWeiboWithoutImage(content);
        
        for(TextItem item : items){
            if(item.mType == TextItem.TYPE_EMOTION){
//                item.mBitmap = getItemBitmap();
            }
        }
        
        return items;
    }
    
    public static List<TextItem> splitWeiboWithoutImage(String content){
        List<TextItem> items = new ArrayList<TextItem>();
        String item = "";
        String tempStr = content;
        
        for(int i = 0; i < content.length(); i++){
            char  current = content.charAt(i);
            if (current == '#' || current == '@' || current == '[' || tempStr.startsWith("http://", i)){
                String text = "";
                if(tempStr.startsWith("http://", i)){
                    current = 'H';
                    text += "http://";
                }else{
                    text += current;
                }
                
                if(item.length() != 0){
                    items.add(new TextItem(item));
                }
                
                TextItem textItem = getString(tempStr.substring(i), current);
                if(textItem != null){
                    items.add(textItem);
                    i += textItem.mText.length() - 1;
                    item = "";
                }else{
                    item = "" + text;
                    i += text.length() - 1;
                }
                
            }else{
                item += current;
            }
        }
        
        if(item.length() != 0){
            items.add(new TextItem(item));
        }
        
        return items;
    }
    
    public static Spanned getSpannedString(Context context, String content){
        List<TextItem> items = splitWeibo(context, content);
        SpannableStringBuilder spannableString = new SpannableStringBuilder("");
        
        for(TextItem item : items){
            SpannableString spannable = new SpannableString(item.mText);
            switch (item.mType) {
            case TextItem.TYPE_TEXT:
                break;
                
            case TextItem.TYPE_LINK:
                URLSpan urlSpan = new URLSpan(item.mText);
                spannable.setSpan(urlSpan, 0, item.mText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
//                ForegroundColorSpan cSpan = new ForegroundColorSpan(item.mColor);
//                spannable.setSpan(cSpan, 0, item.mText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
//                break;
            case TextItem.TYPE_AT:
            case TextItem.TYPE_EMOTION:
                if(item.mBitmap != null){
                    ImageSpan imageSpan = new ImageSpan(context, item.mBitmap, ImageSpan.ALIGN_BOTTOM);   
                    spannable.setSpan(imageSpan, 0, item.mText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    break;
                }else{
                    // If we can not get emotion bitmap here, we will highlight it, we do not break here
                }
            case TextItem.TYPE_SHARP:
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(item.mColor);
                spannable.setSpan(colorSpan, 0, item.mText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
                break;
            default:
                break;
            }
            
            spannableString.append(spannable);
        }
        
        return spannableString;
    }
    
    public static Spanned getCustomizedSpannedString(Context context, String content){
        List<TextItem> items = splitWeibo(context, content);
        SpannableStringBuilder spannableString = new SpannableStringBuilder("");
        
        for(TextItem item : items){
            SpannableString spannable = new SpannableString(item.mText);
            switch (item.mType) {
            case TextItem.TYPE_TEXT:
                break;
                
            case TextItem.TYPE_LINK:
            case TextItem.TYPE_AT:
            case TextItem.TYPE_SHARP:
                WeiboUrlSpan urlSpan = new WeiboUrlSpan(item.mText, context);
                spannable.setSpan(urlSpan, 0, item.mText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
            case TextItem.TYPE_EMOTION:
                if(item.mBitmap != null){
                    ImageSpan imageSpan = new ImageSpan(context, item.mBitmap, ImageSpan.ALIGN_BOTTOM);   
                    spannable.setSpan(imageSpan, 0, item.mText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    break;
                }else{
                    // If we can not get emotion bitmap here, we will highlight it, we do not break here
                }
            default:
                break;
            }
            
            spannableString.append(spannable);
        }
        
        return spannableString;
    }
    
    public static Spanned getSpannedEmotionString(Context context, String content){
        SpannableString spannable = new SpannableString(content);
        String expr = "\\[[0-9a-zA-Z\u4e00-\u9fa5]+\\]";

        Pattern pattern = Pattern.compile(expr); 
        Matcher matcher = pattern.matcher(content);
        Bitmap bitmap = null; // Here need to init the bitmap 
        while(matcher.find()){
            ImageSpan imageSpan = new ImageSpan(context, bitmap, ImageSpan.ALIGN_BOTTOM);   
            spannable.setSpan(imageSpan, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        return spannable;
    }
}
