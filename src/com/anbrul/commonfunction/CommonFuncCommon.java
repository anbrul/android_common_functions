package com.anbrul.commonfunction;

import java.text.Collator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonFuncCommon {
    private static final String IP_PATTERN = "^((25[0-5]|2[0-4]\\d|(1\\d|[1-9])?\\d)\\.){3}(25[0-5]|2[0-4]\\d|(1\\d|[1-9])?\\d)$";
	
	/**
	 * Compare Chinese string
	 * @param chineseString1
	 * @param chineseString2
	 * @return
	 */
	public static int chineseCompare(String chineseString1, String chineseString2) {
        return Collator.getInstance(Locale.CHINESE).compare(chineseString1,	chineseString2);
    }
	
	/**
     * Judge if the characters in the string are all number 
     * @param str
     * @return
     * @author mikewu
     */
    public static boolean isNumeric(String str){ 
        Pattern pattern = Pattern.compile("[0-9]*"); 
        return pattern.matcher(str).matches(); 
    }
    
    /**
     * Judge if all the characters in the string are all Chinese characters
     * @param str
     * @return
     */
    public static boolean isChinese(String str){
    	Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]*"); 
    	return pattern.matcher(str).matches(); 
    }
    
    /**
     * Search the first matched string
     * @param str
     * @param regex
     * @return
     */
    public static String getMatchedString(String str, String regex){
        String ret = null;
        
        Pattern pattern = Pattern.compile(regex); 
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()){
        	ret = matcher.group();
        }
        
        return ret;
    }
    
    /**
     * Check how much times the string matches the regex
     * @param str
     * @param regex
     * @return
     */
    public static int getMatchedStringCount(String str, String regex){
    	int ret = 0;
    	
    	Pattern pattern = Pattern.compile(regex); 
    	Matcher matcher = pattern.matcher(str);
    	while(matcher.find()){
    		ret++;
    	}
    	
    	return ret;
    }

}
