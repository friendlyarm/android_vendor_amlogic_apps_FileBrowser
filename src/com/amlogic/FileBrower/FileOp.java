package com.amlogic.FileBrower;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.os.Message;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class FileOp {	
	public static FileOpTodo file_op_todo = FileOpTodo.TODO_NOTHING;
	public static enum FileOpTodo{
		TODO_NOTHING,
		TODO_CPY,
		TODO_CUT
	}
	public static enum FileOpReturn{
		SUCCESS,
		ERR,
		ERR_NO_FILE,
		ERR_DEL_FAIL,
		ERR_CPY_FAIL,
		ERR_CUT_FAIL,
		ERR_PASTE_FAIL
	}
	
    /** getFileSizeStr */
    public static String getFileSizeStr(long length) {
    	int sub_index = 0;
        String sizeStr = "";
        if (length >= 1073741824) {
            sub_index = (String.valueOf((float)length/1073741824)).indexOf(".");
            sizeStr = ((float)length/1073741824+"000").substring(0,sub_index+3)+" GB";
        } else if (length >= 1048576) {
            sub_index = (String.valueOf((float)length/1048576)).indexOf(".");
            sizeStr =((float)length/1048576+"000").substring(0,sub_index+3)+" MB";
        } else if (length >= 1024) {
            sub_index = (String.valueOf((float)length/1024)).indexOf(".");
            sizeStr = ((float)length/1024+"000").substring(0,sub_index+3)+" KB";
        } else if (length < 1024) {
        	sizeStr = String.valueOf(length)+" B";
        }
        return sizeStr;
    }
    
    /** getFileTypeImg */
    public static Object getFileTypeImg(String filename) { 
    	if (isMusic(filename)) {
    		return R.drawable.item_type_music;
    	} else if (isPhoto(filename)) {
    		return R.drawable.item_type_photo;
    	} else if (isVideo(filename)) {
    		return R.drawable.item_type_video;
    	} else
    		return R.drawable.item_type_file;
    }
    public static Object getThumbImage(String filename) { 
    	if (isMusic(filename)) {
    		return R.drawable.item_preview_music;
    	} else if (isPhoto(filename)) {
    		return R.drawable.item_preview_photo;
    	} else if (isVideo(filename)) {
    		return R.drawable.item_preview_video;
    	} else
    		return R.drawable.item_preview_dir;

    }
    
    /** get file type op*/
    private static boolean isVideo(String filename) {     
    	String name = filename.toLowerCase();
        for (String ext : video_extensions) {
            if (name.endsWith(ext))
                return true;
        }
        return false;
    }
    private static boolean isMusic(String filename) {  
    	String name = filename.toLowerCase();
        for (String ext : music_extensions) {
            if (name.endsWith(ext))
                return true;
        }
        return false;
    }
    private  static boolean isPhoto(String filename) {   
    	String name = filename.toLowerCase();
        for (String ext : photo_extensions) {
            if (name.endsWith(ext))
                return true;
        }
        return false;
    } 
    /* file type extensions */
    //video from com.amlogic.amplayer
    private static final String[] video_extensions = { ".3gp",
        ".divx",
        ".h264",
        ".avi",
        ".m2ts",
        ".mkv",
        ".mov",
        ".mp2",
        ".mp4",
        ".mpg",
        ".mpeg",
        ".rm",
        ".rmvb",
        ".wmv",
        ".ts",
        ".tp",
        ".dat",
        ".vob",
        ".flv",
        ".vc1",
        ".m4v",
        ".f4v",
        ".asf",
        ".lst",
       /* "" */
    };
    //music
    private static final String[] music_extensions = { ".mp3",
    	".wma",
    	".m4a",
    	".aac",
    	".ape",
    	".ogg",
    	".flac",
    	".alac",
    	".wav",
    	".mid",
    	".xmf",
    	".mka",
    	".pcm",
    	".adpcm"
    };
    //photo
    private static final String[] photo_extensions = { ".jpg",
    	".jpeg",
    	".bmp",
    	".tif",
    	".tiff",
    	".png",
    	".gif",
    	".giff",
    	".jfi",
    	".jpe",
    	".jif",
    	".jfif"
    };	
    public static String CheckMediaType(File file){
        String type="";
        String fName=file.getName();
        String end=fName.substring(fName.lastIndexOf(".")+1,fName.length()).toLowerCase();       
        /*for(String ext: video_extensions){
        	if(end.equals(ext.toString())){
        		type = "video";
        		type +="/*";
        		return type;
        	}
        }*/
        if(end.equals("3gp")||end.equals("mp4")){
            type = "video";
            type +="/*";
    		return type;
        }
        for(String ext: music_extensions){
        	if(fName.endsWith(ext)){
        		type = "audio";
        		type +="/*";
        		return type;
        	}
        }
        for(String ext:photo_extensions){
        	if(fName.endsWith(ext)){
        		type = "image";
        		type +="/*";
        		return type;
        	}
        }                		
       type ="*/*";
       return type;
       
    }
    public static int getDeviceIcon(String device_name){
		if(device_name.equals("/mnt/usb")){
			return R.drawable.usb_card_icon;
		}
		else if(device_name.equals("/mnt/flash")){
			return R.drawable.memory_icon;
		}
		else if(device_name.equals("/mnt/sdcard")){
			return R.drawable.sd_card_icon;		
		}
		return 0;
		
	}
    public static String convertDeviceName(String name) {
		// TODO Auto-generated method stub   	
    		String temp_name=null;
    		if(name.equals("Internal Memory"))
    			temp_name="/mnt/flash";
    		else if(name.equals("SD Card"))
    			temp_name="/mnt/sdcard";
    		else if(name.equals("USB"))
    			temp_name="/mnt/usb";
    		return temp_name;  
	}
    public static boolean deviceExist(String string) {
		// TODO Auto-generated method stub
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			 File sdCardDir = Environment.getExternalStorageDirectory();
		}
		return true;
	}
     
    /** check file sel status */
    public static boolean isFileSelected(String file_path,String cur_page) {
    	if(cur_page.equals("list")){
    		if (FileBrower.db == null) return false;
    		try {        	
    			FileBrower.myCursor = FileBrower.db.getFileMarkByPath(file_path);   
    			if (FileBrower.myCursor.getCount() > 0) {
    				return true;  
    			}
	       
    		} finally {        	
    			FileBrower.myCursor.close();        	
    		} 
    	}
    	else{
    		if(cur_page.equals("thumbnail")){
    			if (ThumbnailView.db == null) return false;
        		try {        	
        			ThumbnailView.myCursor = ThumbnailView.db.getFileMarkByPath(file_path);   
        			if (ThumbnailView.myCursor.getCount() > 0) {
        				return true;  
        			}
    	       
        		} finally {        	
        			ThumbnailView.myCursor.close();        	
        		} 
    			
    		}
    	}
		return false;    	
    } 
    
    /** update file sel status 
     * 1: add to mark table 0: remove from mark table
     */
    public static void updateFileStatus(String file_path, int status,String cur_page) {
    	if(cur_page.equals("list")){
    		if (FileBrower.db == null) return;
        	if (status == 1) {
                try {        	
                	FileBrower.myCursor = FileBrower.db.getFileMarkByPath(file_path);   
        	        if (FileBrower.myCursor.getCount() <= 0) {
        	        	//Log.i(FileBrower.TAG, "add file: " + file_path);
        	        	FileBrower.db.addFileMark(file_path, 1);
        	        }
        	       
                } finally {        	
                	FileBrower.myCursor.close();        	
                }     		
        	} else {
        		//Log.i(FileBrower.TAG, "remove file: " + file_path);
        		FileBrower.db.deleteFileMark(file_path);
        	}
    		
    	}
    	else{
    		if(cur_page.equals("thumbnail")){
    			if (ThumbnailView.db == null) return;
            	if (status == 1) {
                    try {        	
                    	ThumbnailView.myCursor = ThumbnailView.db.getFileMarkByPath(file_path);   
            	        if (ThumbnailView.myCursor.getCount() <= 0) {
            	        	//Log.i(FileBrower.TAG, "add file: " + file_path);
            	        	ThumbnailView.db.addFileMark(file_path, 1);
            	        }
            	       
                    } finally {        	
                    	ThumbnailView.myCursor.close();        	
                    }     		
            	} else {
            		//Log.i(FileBrower.TAG, "remove file: " + file_path);
            		ThumbnailView.db.deleteFileMark(file_path);
            	}
    			
    		}
    		
    	}
    	
    		
    }
    
    /** cut/copy/paste/delete selected files*/
    public static FileOpReturn cutSelectedFile() {
		return FileOpReturn.ERR;    	
    }
    public static FileOpReturn copySelectedFile() {
		return FileOpReturn.ERR; 	
    }
    public static FileOpReturn pasteSelectedFile(String cur_page) {
    	List<String> fileList = new ArrayList<String>();
   	
    	if ((file_op_todo != FileOpTodo.TODO_CPY) &&
    		(file_op_todo != FileOpTodo.TODO_CUT)) {
    		if(cur_page.equals("list")){
    			FileBrower.mProgressHandler.sendMessage(Message.obtain(
    					FileBrower.mProgressHandler, 5)); 
    		}
    		else{
    			ThumbnailView.mProgressHandler.sendMessage(Message.obtain(
    					ThumbnailView.mProgressHandler, 5)); 
    		}
        	
        	return FileOpReturn.ERR; 
    	}    			
    		
        try {   
        	if(cur_page.equals("list")){
        		FileBrower.myCursor = FileBrower.db.getFileMark();   
    	        if (FileBrower.myCursor.getCount() > 0) {
    	            for(int i=0; i<FileBrower.myCursor.getCount(); i++){
    	            	FileBrower.myCursor.moveToPosition(i);
    	            	fileList.add(FileBrower.myCursor.getColFilePath());
    	            }      	
    	        }       		
        	}
        	else{
        		ThumbnailView.myCursor = ThumbnailView.db.getFileMark();   
    	        if (ThumbnailView.myCursor.getCount() > 0) {
    	            for(int i=0; i<ThumbnailView.myCursor.getCount(); i++){
    	            	ThumbnailView.myCursor.moveToPosition(i);
    	            	fileList.add(ThumbnailView.myCursor.getColFilePath());
    	            }      	
    	        }       	
        		
        	}
        	
        } finally { 
        	if(cur_page.equals("list")){
        		FileBrower.myCursor.close();        		
        	}
        	else{
        		ThumbnailView.myCursor.close();
        	}
        	       	
        }  
        
        if (!fileList.isEmpty()) {
        	if(cur_page.equals("list")){
        		FileBrower.mProgressHandler.sendMessage(Message.obtain(
            			FileBrower.mProgressHandler, 3)); 
        	}
        	else{
        		ThumbnailView.mProgressHandler.sendMessage(Message.obtain(
        				ThumbnailView.mProgressHandler, 3)); 
        	}
        	 
        	for (int i = 0; i < fileList.size(); i++) {
        		String name = fileList.get(i);
        		File file = new File(name);
        		if (file.exists()) {
        			//Log.i(FileBrower.TAG, "paste file: " + name);
        			try {  
        				File file_new;
    					//Log.i(FileBrower.TAG, "copy and paste file: " + name);
        				if(cur_page.equals("list")){
        					 file_new = new File(FileBrower.cur_path + File.separator + file.getName());  
        	        	}
        	        	else{
        	        		 file_new = new File(ThumbnailView.cur_path + File.separator + file.getName()); 
        	        	}
    					
    					
    					if (file_new.exists()) {
        	        		String date = new SimpleDateFormat("yyyyMMddHHmmss_")
    	        			.format(Calendar.getInstance().getTime()); 
        	        		if(cur_page.equals("list")){
        	        			file_new = new File(FileBrower.cur_path + File.separator + date + file.getName()); 
        	        		}
        	        		else{
        	        			file_new = new File(ThumbnailView.cur_path + File.separator + date + file.getName()); 
        	        		}
    						
    					}
    					
    					if (!file_new.exists()) {	
        					//Log.i(FileBrower.TAG, "copy to file: " + file_new.getPath());	        					
        					file_new.createNewFile();
        					try {
	        			        InputStream f_is = new FileInputStream(file.getAbsolutePath()); 
	        			        FileOutputStream f_os = new FileOutputStream(file_new.getAbsolutePath());
	        			        byte[] buffer = new byte[1024];
	        			        int byteread = 0;
	        			        long bytecount = 0;	        			       
	        			        while ( (byteread = f_is.read(buffer)) != -1) {        			          
	        			        	f_os.write(buffer, 0, byteread);
	        			        	bytecount += byteread;	
	        			        	if(cur_page.equals("list")){
	        			        		FileBrower.mProgressHandler.sendMessage(Message.obtain(
		        		            			FileBrower.mProgressHandler, 1, (int)(bytecount * 100 / file.length()), 0));
	        			        	}
	        			        	else{
	        			        		ThumbnailView.mProgressHandler.sendMessage(Message.obtain(
	        			        				ThumbnailView.mProgressHandler, 1, (int)(bytecount * 100 / file.length()), 0));
	        			        	}
	        			        	
	        			        }		        			        
	        			        f_is.close();
	        			        
	        			        if (file_op_todo == FileOpTodo.TODO_CUT)
	        			        	file.delete();
	        			        
        					} catch (Exception e) {
        						Log.e("Exception when copy file", e.toString());
        					} 
    					}

        			} catch (Exception e) {
        				Log.e("Exception when delete file", e.toString());
        			}
        		}   
        		if(cur_page.equals("list")){
        			FileBrower.mProgressHandler.sendMessage(Message.obtain(
                			FileBrower.mProgressHandler, 2, (i+1) * 100 / fileList.size(), 0));
	        	}
	        	else{
	        		ThumbnailView.mProgressHandler.sendMessage(Message.obtain(
	        				ThumbnailView.mProgressHandler, 2, (i+1) * 100 / fileList.size(), 0));
	        	}
        		
        		
        	}
        	if(cur_page.equals("list")){
        		FileBrower.mProgressHandler.sendMessage(Message.obtain(
            			FileBrower.mProgressHandler, 4));
            	return FileOpReturn.SUCCESS;
        	}
        	else{
        		ThumbnailView.mProgressHandler.sendMessage(Message.obtain(
        				ThumbnailView.mProgressHandler, 4));
            	return FileOpReturn.SUCCESS;
        	}
        	
        } else {
        	if(cur_page.equals("list")){
        		FileBrower.mProgressHandler.sendMessage(Message.obtain(
            			FileBrower.mProgressHandler, 5));  
        		return FileOpReturn.ERR; 
        		
        	}
        	else{
        		ThumbnailView.mProgressHandler.sendMessage(Message.obtain(
        				ThumbnailView.mProgressHandler, 5));  
        		return FileOpReturn.ERR; 
        	}
        	
        		
        }
        
    }   
    public static FileOpReturn deleteSelectedFile(String cur_page) {
    	List<String> fileList = new ArrayList<String>();
        try {
        	if(cur_page.equals("list")){
        		FileBrower.myCursor = FileBrower.db.getFileMark();   
    	        if (FileBrower.myCursor.getCount() > 0) {
    	            for(int i=0; i<FileBrower.myCursor.getCount(); i++){
    	            	FileBrower.myCursor.moveToPosition(i);
    	            	fileList.add(FileBrower.myCursor.getColFilePath());
    	            }      	
    	        }

        	}
        	else{
        		ThumbnailView.myCursor = ThumbnailView.db.getFileMark();   
    	        if (ThumbnailView.myCursor.getCount() > 0) {
    	            for(int i=0; i<ThumbnailView.myCursor.getCount(); i++){
    	            	ThumbnailView.myCursor.moveToPosition(i);
    	            	fileList.add(ThumbnailView.myCursor.getColFilePath());
    	            }      	
    	        }
        		
        	}
        	
        } finally {  
        	if(cur_page.equals("list")){
        		FileBrower.myCursor.close();
        		
        	}
        	else{
        		ThumbnailView.myCursor.close();
        	}
        }     	
    	
        if (!fileList.isEmpty()) {
        	for (String name : fileList) {
        		File file = new File(name);
        		if (file.exists()) {
        			//Log.i(FileBrower.TAG, "delete file: " + name);
        			try {
        			file.delete();
        			} catch (Exception e) {
        				Log.e("Exception when delete file", e.toString());
        			}
        		}
        	}
        	return FileOpReturn.SUCCESS;
        } else
        	return FileOpReturn.ERR;    
			
    }
    
}
         
