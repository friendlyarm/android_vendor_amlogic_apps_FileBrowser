package com.amlogic.FileBrower;

import java.io.File;

public class FileOp {
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
    private static final String[] video_extensions = { "3gp",
        "divx",
        "h264",
        "avi",
        "m2ts",
        "mkv",
        "mov",
        "mp2",
        "mp4",
        "mpg",
        "mpeg",
        "rm",
        "rmvb",
        "wmv",
        "ts",
        "tp",
        "dat",
        "vob",
        "flv",
        "vc1",
        "m4v",
        "f4v",
        "asf",
        "lst",
       /* "" */
    };
    //music
    private static final String[] music_extensions = {"mp3",
    	"wma","m4a","aac","ape","ogg","flac","alac","wav","mid","xmf"  	
    };
    //photo
    private static final String[] photo_extensions = { "jpg","jpeg",
    	"bmp","tif","tiff","png","gif"  	
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
        	if(end.equals(ext.toString())){
        		type = "audio";
        		type +="/*";
        		return type;
        	}
        }
        for(String ext:photo_extensions){
        	if(end.equals(ext.toString())){
        		type = "image";
        		type +="/*";
        		return type;
        	}
        }                		
       type ="*/*";
       return type;
       
    }
}
         
