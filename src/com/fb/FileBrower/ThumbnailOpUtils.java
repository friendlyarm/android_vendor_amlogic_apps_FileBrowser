package com.fb.FileBrower;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ThumbnailOpUtils {

    private static final String ROOT_PATH = "/storage";
	private static final String SHEILD_EXT_STOR = "/storage/sdcard0/external_storage";
	private static final String NAND_PATH = "/storage/sdcard0";
	private static final String SD_PATH = "/storage/external_storage/sdcard1";
	private static final String USB_PATH ="/storage/external_storage";
	private static final String SATA_PATH ="/storage/external_storage/sata";
	
	public static void stopThumbnailSanner(Context context) {
		context.stopService(new Intent(context, ThumbnailScannerService.class));
		//Log.w("stopThumbnailSanner", "..................");
	}
	
	public static void cleanThumbnails(Context context) {
		Bundle args = new Bundle();		
		args.putString("scan_type", "clean");
		context.startService(
    		new Intent(context, ThumbnailScannerService.class).putExtras(args));
		
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
			if (!dev_path.equals(NAND_PATH) &&
				!dev_path.equals(SD_PATH) &&
				!dev_path.equals(USB_PATH) &&
				!dev_path.equals(SATA_PATH) &&
				!dev_path.startsWith(ROOT_PATH)) 				
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
			if (!dir_path.equals(NAND_PATH) &&
				!dir_path.equals(SD_PATH) &&
				!dir_path.equals(USB_PATH) &&
				!dir_path.equals(SATA_PATH) &&
				!dir_path.startsWith(ROOT_PATH)) 						
				return;	
			
			Bundle args = new Bundle();
			args.putString("dir_path", dir_path);
			args.putString("scan_type", "dir");
			context.startService(
        		new Intent(context, ThumbnailScannerService.class).putExtras(args));
			
		}
	}		
}