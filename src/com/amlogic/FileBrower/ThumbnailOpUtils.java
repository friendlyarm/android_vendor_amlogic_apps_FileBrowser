package com.amlogic.FileBrower;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.amlogic.FileBrower.FileBrowerDatabase.ThumbnailCursor;

public class ThumbnailOpUtils {
	
	public static void stopThumbnailSanner(Context context) {
		context.stopService(new Intent(context, ThumbnailScannerService.class));
		
	}
	
	public static void cleanThumbnails(Context context, FileBrowerDatabase db) {
		if (db != null) {
			 ThumbnailCursor cc = null;
			 try {
				 cc = db.checkThumbnail();
				 if (cc != null && cc.moveToFirst()) {
					 if (cc.getCount() > 0) {
						 for (int i = 0; i < cc.getCount(); i++) {
							 cc.moveToPosition(i);
							 String file_path = cc.getColFilePath();
							 if (file_path != null) {
								 if (!new File(file_path).exists()) {
									 db.deleteThumbnail(file_path);
								 }
							 }
						 }
					 }					 
				 }
			 } finally {
				 if(cc != null) cc.close();
			 }
		}
		
	}
	
	public static void deleteAllThumbnails(Context context, FileBrowerDatabase db) {
		if (db != null)
			db.deleteAllThumbnail();
	}
	
	public static void updateThumbnailsForAllDev(Context context) {
		Bundle args = new Bundle();		
		args.putString("scan_type", "all");
		context.startService(
    		new Intent(context, ThumbnailScannerService.class).putExtras(args));
	}
	
	public static void updateThumbnailsForDev(Context context, String dev_path) {
		if (dev_path != null) {
			if (!dev_path.equals("/mnt/sdcard") &&
				!dev_path.equals("/mnt/flash") &&
				!dev_path.equals("/mnt/usb") &&
				!dev_path.startsWith("/mnt/sd")) 				
					return;			
			
			Bundle args = new Bundle();
			args.putString("dir_path", dev_path);
			args.putString("scan_type", "dev");
			context.startService(
        		new Intent(context, ThumbnailScannerService.class).putExtras(args));
		}
	}	
	
	public static void updateThumbnailsForDir(Context context, String dir_path) {
		if (dir_path != null) {
			if (!dir_path.startsWith("/mnt/sdcard") &&
				!dir_path.startsWith("/mnt/flash") &&
				!dir_path.startsWith("/mnt/usb") &&
				!dir_path.startsWith("/mnt/sd")) 				
				return;	
			
			Bundle args = new Bundle();
			args.putString("dir_path", dir_path);
			args.putString("scan_type", "dir");
			context.startService(
        		new Intent(context, ThumbnailScannerService.class).putExtras(args));
			
		}
	}		
}