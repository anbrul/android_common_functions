package com.anbrul.commonfunction;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Text file writer and reader
 * @author mikewu
 */
public class CommonFuncFileIO {

    public CommonFuncFileIO() {
    }

    /**
     * Read a single line
     * 
     * @param filePath
     * @return
     * @throws FileNotFoundException
     */
    public String readFileOneLine(String filePath) throws FileNotFoundException {
        String currentRecord = null;
        BufferedReader file = new BufferedReader(new FileReader(filePath));
        String returnStr = null;
        try {
            currentRecord = file.readLine();
        } catch (IOException e) {
            System.out.println("Read file error!");
            e.printStackTrace();
        }
        if (currentRecord == null)
            returnStr = "";
        else {
            returnStr = currentRecord;
        }
        return returnStr;
    }

    /**
     * Read the file
     * 
     * @param filePath
     * @return
     * @throws FileNotFoundException
     */
    public static String getFileString(File file){
        BufferedReader reader = null;
        String content = "";
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
                // Print the the line
//                System.out.println("line " + line + ": " + tempString);
            	tempString = new String(tempString.getBytes("UTF-8"));
                content = content + tempString;
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return content;
    }
    
	public static String getFileString(String fileName, String charset) {
		if (charset == null) {
			charset = "UTF-8";
		}

		String content = "";
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charset));
			String line = null;
			while ((line = br.readLine()) != null) {
				content = content + line;
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return content;
	}

    /**
     * Write string to file
     * 
     * @param file
     * @param content
     * @throws FileNotFoundException
     */
    public void WriteFile(File file, String content){
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(file));
            pw.println(content);
            pw.close();
        } catch (IOException e) {
            System.out.println("Write file error!");
            e.printStackTrace();
        }
    }

    /**
     * Write a string array to file
     * 
     * @param filePath file path
     * @param fileName file name
     * @param args
     * @throws IOException
     */
    public void writeFile(String filePath, String[] args) throws IOException {
        FileWriter fw = new FileWriter(filePath);
        PrintWriter out = new PrintWriter(fw);
        for (int i = 0; i < args.length; i++) {
            out.write(args[i]);
            out.println();
        }
        fw.close();
        out.close();
    }

    /**
     * Check if the file exist
     * @return You know that!
     * @author mikewu
     */
    public static boolean isFileExists(String filePath) {
        File f = new File(filePath);
        if (f.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create a new file, if the file exist, will delete it
     * 
     * @param filePath
     * @return if File exist
     */
    public static boolean createFile(String filePath) {
        boolean ifFileExist = false;
        File file = new File(filePath);

        if (file.exists()) {
            ifFileExist = true;
            file.delete();
        }

        try {
        	File parent = file.getParentFile();
        	if(parent != null){
        		parent.mkdirs();
        	}
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("Create file failed:" + file.getPath());
        }
        return ifFileExist;
    }

    /**
     * Write string to file
     * 
     * @param str content
     * @param destFilePath file path
     * @throws Exception
     */
    public static void writeStringToFile(String str, String destFilePath){
        File file = new File(destFilePath);
        try {
            DataOutputStream outs = new DataOutputStream(new FileOutputStream(file));
            outs.write(str.getBytes());
            outs.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Copy file from one dir to another
     * @param source
     * @param destination
     */
    public static boolean copyFile(String source, String destination){
        try {
            InputStream myInput = new FileInputStream(source);

            String outFileName = destination;
            createFile(outFileName);
            OutputStream myOutput = new FileOutputStream(outFileName);

            // transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            // Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
}
