package com.amlogic.FileBrower;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;

import android.os.Environment;
import android.os.Message;
import android.util.Log;

public class FileOp {
	public static boolean switch_mode = false;	
	public static void SetMode(boolean value){
		switch_mode = value;
	}
	public static boolean GetMode(){
		return switch_mode;
	}
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
    		return null;
    	} else if (isVideo(filename)) {
    		return R.drawable.item_preview_video;
    	} else
    		return R.drawable.item_preview_dir;

    }
    
    /** get file type op*/
    public static boolean isVideo(String filename) {     
    	String name = filename.toLowerCase();
        for (String ext : video_extensions) {
            if (name.endsWith(ext))
                return true;
        }
        return false;
    }
    public static boolean isMusic(String filename) {  
    	String name = filename.toLowerCase();
        for (String ext : music_extensions) {
            if (name.endsWith(ext))
                return true;
        }
        return false;
    }
    public  static boolean isPhoto(String filename) {   
    	String name = filename.toLowerCase();
        for (String ext : photo_extensions) {
            if (name.endsWith(ext))
                return true;
        }
        return false;
    } 
    /* file type extensions */
    //video from com.amlogic.amplayer
    public static final String[] video_extensions = { ".3gp",
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
    public static int getThumbDeviceIcon(Context c,String device_name){
    	String internal = c.getString(R.string.memory_device_str);
    	String sdcard = c.getString(R.string.sdcard_device_str);
    	String usb = c.getString(R.string.usb_device_str);
    	if(device_name.equals(internal)){
			return R.drawable.memory_default;
		}
		else if(device_name.equals(sdcard)){
			return R.drawable.sdcard_default;
		}
		else if(device_name.equals(usb)){
			return R.drawable.usb_default;		
		}
		return R.drawable.txt_default;
    }    
	public static String convertDeviceName(Context c,String name) {
		// TODO Auto-generated method stub   	
    		String temp_name=null;
    		String internal = c.getString(R.string.memory_device_str);
        	String sdcard = c.getString(R.string.sdcard_device_str);
        	String usb = c.getString(R.string.usb_device_str);
    		if(name.equals(internal))
    			temp_name="/mnt/flash";
    		else if(name.equals(sdcard))
    			temp_name="/mnt/sdcard";
    		else if(name.equals(usb))
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
    
    public static Bitmap fitSizePic(File f){ 
        Bitmap resizeBmp = null;
        BitmapFactory.Options opts = new BitmapFactory.Options(); 
        if(f.length()<20480){         //0-20k
          opts.inSampleSize = 1;
        }else if(f.length()<51200){   //20-50k
          opts.inSampleSize = 2;
        }else if(f.length()<307200){  //50-300k
          opts.inSampleSize = 4;
        }else if(f.length()<819200){  //300-800k
          opts.inSampleSize = 6;
        }else if(f.length()<1048576){ //800-1024k
          opts.inSampleSize = 8;
        }else{
          opts.inSampleSize = 10;
        }
        resizeBmp = BitmapFactory.decodeFile(f.getPath(),opts);
        return resizeBmp; 
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
    private static void nioTransferCopy(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;

        FileInputStream inStream = null;
        FileOutputStream outStream = null;

        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);

            in = inStream.getChannel();
            out = outStream.getChannel();

            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(inStream);
            close(in);
            close(outStream);
            close(out);
        }
    }
    private static void nioBufferCopy(File source, File target, String cur_page, int buf_size) {
        FileChannel in = null;
        FileChannel out = null;

        FileInputStream inStream = null;
        FileOutputStream outStream = null;

        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);

            in = inStream.getChannel();
            out = outStream.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(1024 * buf_size);
            long bytecount = 0;	
            int byteread = 0;
            while ((byteread = in.read(buffer)) != -1) {
                buffer.flip();
                out.write(buffer);
                buffer.clear();
                bytecount += byteread;
	        	if(cur_page.equals("list")){
	        		FileBrower.mProgressHandler.sendMessage(Message.obtain(
	            			FileBrower.mProgressHandler, 1, (int)(bytecount * 100 / source.length()), 0));
	        	}
	        	else{
	        		ThumbnailView.mProgressHandler.sendMessage(Message.obtain(
	        				ThumbnailView.mProgressHandler, 1, (int)(bytecount * 100 / source.length()), 0));
	        	}                
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(inStream);
            close(in);
            close(outStream);
            close(out);
        }
    }
    private static void close(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static FileOpReturn pasteSelectedFile(String cur_page) {
    	List<String> fileList = new ArrayList<String>();
    	//long copy_time_start=0, copy_time_end = 0;
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
        						//copy_time_start = Calendar.getInstance().getTimeInMillis();
        						if (file.length() < 1024*1024*10)
        							nioBufferCopy(file, file_new, cur_page, 4);
        						else if (file.length() < 1024*1024*100)
        							nioBufferCopy(file, file_new, cur_page, 1024);
        						else 
        							nioBufferCopy(file, file_new, cur_page, 1024*10);
        						//nioTransferCopy(file, file_new);
        						//copy_time_end = Calendar.getInstance().getTimeInMillis();
        						
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
         
