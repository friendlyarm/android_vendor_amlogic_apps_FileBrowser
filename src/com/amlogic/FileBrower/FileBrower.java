package com.amlogic.FileBrower;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

public class FileBrower extends Activity {
	private static final String TAG = "FileBrower";
	
	private static final String ROOT_PATH = "/mnt";
	private static String cur_path = ROOT_PATH;
	private static String prev_path = ROOT_PATH;
	
	private static final int SORT_DIALOG_ID = 0;
	private static final int EDIT_DIALOG_ID = 1;
	private static final int CLICK_DIALOG_ID = 2;
	private AlertDialog sort_dialog;	
	private AlertDialog edit_dialog;
	private AlertDialog click_dialog;
	
	private ListView lv;
	private TextView tv;
	private List<String> devList = new ArrayList<String>();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.main);        
        
        /* setup file list */
        lv = (ListView) findViewById(R.id.listview);  
        
        //lv.setAdapter(getFileListAdapter(ROOT_PATH));
        DeviceScan();
        
        /* lv OnItemClickListener */
        lv.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				Map<String, Object> item = (Map<String, Object>)parent.getItemAtPosition(pos);
			
				String file_path = (String) item.get("file_path");
				File file = new File(file_path);
				if(!file.exists()){
					//finish();
					return;
				}

				if (file.isDirectory()) {	
					prev_path = cur_path;
					cur_path = file_path;
					lv.setAdapter(getFileListAdapter(file_path));	
				}
				else {	
					ToggleButton btn_mode = (ToggleButton) findViewById(R.id.btn_mode); 
					if (!btn_mode.isChecked())
						showDialog(CLICK_DIALOG_ID);	
					else {
						if (item.get("item_sel").equals(R.drawable.item_img_unsel))
							item.put("item_sel", R.drawable.item_img_sel);
						else if (item.get("item_sel").equals(R.drawable.item_img_sel))
							item.put("item_sel", R.drawable.item_img_unsel);
						
						((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();	
					}
				}
								
			}        	
        });      
        
        /* lv OnItemLongClickListener */
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			
			public boolean onItemLongClick(AdapterView<?> parent, View view, int pos,
					long id) {
				Map<String, Object> item = (Map<String, Object>)parent.getItemAtPosition(pos);
				
				String file_path = (String) item.get("file_path");
				File file = new File(file_path);
				
				if (file.isFile()) {	
					showDialog(EDIT_DIALOG_ID);					
				}
				return false;
			}
		});
        
        /* btn_parent listener */
        Button btn_parent = (Button) findViewById(R.id.btn_parent);  
        btn_parent.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!cur_path.equals(ROOT_PATH)) {
					File file = new File(cur_path);
					String parent_path = file.getParent();
					prev_path = cur_path;
					cur_path = parent_path;
					if(parent_path.equals(ROOT_PATH)){
						DeviceScan();
					}
					else{
						lv.setAdapter(getFileListAdapter(parent_path));
					
					}
				}
			}        	
        });
        
        /* btn_home listener */
        Button btn_home = (Button) findViewById(R.id.btn_home);  
        btn_home.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {				
				DeviceScan();
			}        	
        });       
        
        /* btn_close_listener */
        /*Button btn_close = (Button) findViewById(R.id.btn_close);  
        btn_close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}        	
        });  */   
        
        /* btn_edit_listener */
        Button btn_edit = (Button) findViewById(R.id.btn_edit);  
        btn_edit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(EDIT_DIALOG_ID);
			}        	
        });         
        
        /* btn_sort_listener */
        Button btn_sort = (Button) findViewById(R.id.btn_sort);  
        btn_sort.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(SORT_DIALOG_ID);
			}        	
        });           
    }
    
    private void DeviceScan() {
		// TODO Auto-generated method stub
    	devList.clear();
    	String DeviceArray[]={"Internal Memory","SD Card","USB"};   	
    	for(int i=0;i<DeviceArray.length;i++){
    		if(deviceExist(DeviceArray[i])){
    			devList.add(DeviceArray[i]);
    		}
    	} 
    	lv.setAdapter(getDeviceListAdapter());
		
	}

	private ListAdapter getDeviceListAdapter() {
		// TODO Auto-generated method stub
		return new SimpleAdapter(FileBrower.this,
        		getDeviceListData(),
        		R.layout.device_item,        		
                new String[]{
        	"item_type",
        	"item_name",        	        	
        	"item_rw",
        	"item_size"},        		
                new int[]{
        	R.id.device_type,
        	R.id.device_name,        	
        	R.id.device_rw,       	
        	R.id.device_size});  		
	}

	private List<? extends Map<String, ?>> getDeviceListData() {
		// TODO Auto-generated method stub
		String file_path = null;
		String device = null;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();              
    	for(int i = 0; i < this.devList.size(); i++) {   
    		Map<String, Object> map = new HashMap<String, Object>();    		
    		map.put("item_name", this.devList.get(i)); 
    		file_path = convertDeviceName(this.devList.get(i));
    		map.put("file_path", file_path);         
    		map.put("item_size", null);
    		map.put("item_rw", null);
    		map.put("item_type", getDeviceIcon(file_path));    		
    		list.add(map); 
    	}
    	device = getString(R.string.rootDevice);
    	updatePathShow(device);
    	return list; 
	}
	private int getDeviceIcon(String device_name){
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
	protected String convertDeviceName(String name) {
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
	private boolean deviceExist(String string) {
		// TODO Auto-generated method stub
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			 File sdCardDir = Environment.getExternalStorageDirectory();
		}
		return true;
	}
    /** Dialog */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case SORT_DIALOG_ID:
        	LayoutInflater inflater = (LayoutInflater) FileBrower.this
        		.getSystemService(LAYOUT_INFLATER_SERVICE);
        	View layout_sort = inflater.inflate(R.layout.sort_dialog_layout,
        		(ViewGroup) findViewById(R.id.layout_root_sort));
        	
            sort_dialog =  new AlertDialog.Builder(FileBrower.this)   
        	.setView(layout_sort)
            .create(); 
            return sort_dialog;

        case EDIT_DIALOG_ID:
	        LayoutInflater inflater1 = (LayoutInflater) FileBrower.this
	    	.getSystemService(LAYOUT_INFLATER_SERVICE);
	    	View layout_edit = inflater1.inflate(R.layout.edit_dialog_layout,
	    		(ViewGroup) findViewById(R.id.layout_root_edit));
	    	
	    	edit_dialog = new AlertDialog.Builder(FileBrower.this)   
	    	.setView(layout_edit)
	        .create();             
	    	return edit_dialog;
        	
        case CLICK_DIALOG_ID:
	        LayoutInflater inflater2 = (LayoutInflater) FileBrower.this
	    	.getSystemService(LAYOUT_INFLATER_SERVICE);
	    	View layout_click = inflater2.inflate(R.layout.click_dialog_layout,
	    		(ViewGroup) findViewById(R.id.layout_root_click));
	    	
	    	click_dialog = new AlertDialog.Builder(FileBrower.this)   
	    	.setView(layout_click)
	        .create();
	    	return click_dialog;
	    	
        }
        
		return null;    	
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        LayoutParams lp = dialog.getWindow().getAttributes();    	
    	switch (id) {
    	case SORT_DIALOG_ID:
            if (display.getHeight() > display.getWidth()) {            	
            	lp.width = (int) (display.getWidth() * 1.0);       	
        	} else {        		
        		lp.width = (int) (display.getWidth() * 0.5);            	
        	}
            dialog.getWindow().setAttributes(lp);   

            Button sort_btn_close = (Button) sort_dialog.getWindow().findViewById(R.id.sort_btn_close);  
            sort_btn_close.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    				sort_dialog.dismiss();
    			}        	
            });      
            Button sort_btn_name = (Button) sort_dialog.getWindow().findViewById(R.id.sort_btn_name);  
            sort_btn_name.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {    				
    				lv.setAdapter(getFileListAdapterSorted(cur_path, "by_name"));
    				sort_dialog.dismiss();
    			}        	
            });  
            Button sort_btn_date = (Button) sort_dialog.getWindow().findViewById(R.id.sort_btn_date);  
            sort_btn_date.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {    				
    				lv.setAdapter(getFileListAdapterSorted(cur_path, "by_date"));
    				sort_dialog.dismiss();
    			}        	
            });   
            Button sort_btn_size = (Button) sort_dialog.getWindow().findViewById(R.id.sort_btn_size);  
            sort_btn_size.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {    				
    				lv.setAdapter(getFileListAdapterSorted(cur_path, "by_size"));
    				sort_dialog.dismiss();
    			}        	
            });     		
            break;
    	case EDIT_DIALOG_ID:    		
            if (display.getHeight() > display.getWidth()) {            	
            	lp.width = (int) (display.getWidth() * 1.0);       	
        	} else {        		
        		lp.width = (int) (display.getWidth() * 0.5);            	
        	}
            dialog.getWindow().setAttributes(lp);  

            Button edit_btn_close = (Button) edit_dialog.getWindow().findViewById(R.id.edit_btn_close);  
            edit_btn_close.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    				edit_dialog.dismiss();
    			}        	
            });  
            Button edit_btn_copy = (Button) edit_dialog.getWindow().findViewById(R.id.edit_btn_copy);  
            edit_btn_copy.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    				Log.i(TAG, "do: edit_btn_copy");
    				edit_dialog.dismiss();
    			}        	
            });   
            Button edit_btn_paste = (Button) edit_dialog.getWindow().findViewById(R.id.edit_btn_paste);  
            edit_btn_paste.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    				Log.i(TAG, "do: edit_btn_paste");
    				edit_dialog.dismiss();
    			}        	
            });  
            Button edit_btn_delete = (Button) edit_dialog.getWindow().findViewById(R.id.edit_btn_delete);  
            edit_btn_delete.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    				Log.i(TAG, "do: edit_btn_delete");
    				edit_dialog.dismiss();
    			}        	
            }); 
    		break;
    	case CLICK_DIALOG_ID:
            if (display.getHeight() > display.getWidth()) {            	
            	lp.width = (int) (display.getWidth() * 1.0);       	
        	} else {        		
        		lp.width = (int) (display.getWidth() * 0.5);            	
        	}
            dialog.getWindow().setAttributes(lp);  
 
	    	Button click_btn_close = (Button) click_dialog.getWindow().findViewById(R.id.click_btn_close);  
	    	click_btn_close.setOnClickListener(new OnClickListener() {
	    		public void onClick(View v) {
	    			click_dialog.dismiss();
	    		}        	
	        }); 			
    		break;
    	}
    }  
	
    
    /** getFileListAdapter */
    private SimpleAdapter getFileListAdapter(String path) {
        return new SimpleAdapter(FileBrower.this,
        		getFileListData(path),
        		R.layout.filelist_item,        		
                new String[]{
        	"item_type",
        	"item_name",
        	"item_sel",
        	"item_size",
        	"item_date",
        	"item_rw"},        		
                new int[]{
        	R.id.item_type,
        	R.id.item_name,
        	R.id.item_sel,
        	R.id.item_size,
        	R.id.item_date,
        	R.id.item_rw});  
    }
    
    /** getFileListData */
    private List<Map<String, Object>> getFileListData(String path) {    	
    	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();   	
    	try {
    		File file_path = new File(path); 
        	if (file_path != null && file_path.exists()) { 
        		if (file_path.listFiles() != null) {
            		if (file_path.listFiles().length > 0) {
            			for (File file : file_path.listFiles()) {    					
            	        	Map<String, Object> map = new HashMap<String, Object>();    		        	
            	        	map.put("item_name", file.getName());            	        	
            	        	map.put("file_path", file.getAbsolutePath());
            	        	
            	        	if (file.isDirectory()) {
            	        		map.put("item_sel", R.drawable.item_img_nosel);
            	        		map.put("item_type", R.drawable.item_type_dir);
            	        		
            	        		String rw = "d";
            	        		if (file.canRead()) rw += "r"; else rw += "-";
            	        		if (file.canWrite()) rw += "w"; else rw += "-";  
            	        		map.put("item_rw", rw);       
            	        		
            	        		long file_date = file.lastModified();
            	        		String date = new SimpleDateFormat("yyyy/MM/dd HH:mm")
            	        			.format(new Date(file_date));
            	        		map.put("item_date", date + " | ");
            	        		map.put("file_date", file_date);	//use for sorting
            	        		
            	        		long file_size = file.length();
            	        		map.put("file_size", file_size);	//use for sorting
            	        		map.put("item_size", " | ");            	        		
            	        	} else {
            	        		map.put("item_sel", R.drawable.item_img_unsel);            	        		
            	        		map.put("item_type", FileOp.getFileTypeImg(file.getName()));
            	        		
            	        		String rw = "-";
            	        		if (file.canRead()) rw += "r"; else rw += "-";
            	        		if (file.canWrite()) rw += "w"; else rw += "-";  
            	        		map.put("item_rw", rw);       
            	        		
            	        		long file_date = file.lastModified();
            	        		String date = new SimpleDateFormat("yyyy/MM/dd HH:mm")
            	        			.format(new Date(file_date));
            	        		map.put("item_date", date + " | ");
            	        		map.put("file_date", file_date);	//use for sorting
            	        		
            	        		long file_size = file.length();
            	        		map.put("file_size", file_size);	//use for sorting
            	        		map.put("item_size", FileOp.getFileSizeStr(file_size) + " | ");
            	        		
            	        		
            	        	}
            	        	
            	        	list.add(map);    		        	
            			}
            		}            		
        		}
        		updatePathShow(path);
        	}
    	} catch (Exception e) {
    		Log.e(TAG, "Exception when getFileListData(): ", e);
    		return list;
		}   
    	
		//Log.i(TAG, "list size = " + list.size());
    	return list;
 	}  
        
    /** updatePathShow */
    private void updatePathShow(String path) {      	
        tv = (TextView) findViewById(R.id.path); 
        tv.setText(path);    	
    }
   
    /** getFileListAdapterSorted */
    private SimpleAdapter getFileListAdapterSorted(String path, String sort_type) {
        return new SimpleAdapter(FileBrower.this,
        		getFileListDataSorted(path, sort_type),
        		R.layout.filelist_item,        		
                new String[]{
        	"item_type",
        	"item_name",
        	"item_sel",
        	"item_size",
        	"item_date",
        	"item_rw"},        		
                new int[]{
        	R.id.item_type,
        	R.id.item_name,
        	R.id.item_sel,
        	R.id.item_size,
        	R.id.item_date,
        	R.id.item_rw});  
    }
    
    /** getFileListDataSorted */
    private List<Map<String, Object>> getFileListDataSorted(String path, String sort_type) {    	
    	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();   	
    	try {
    		File file_path = new File(path); 
        	if (file_path != null && file_path.exists()) { 
        		if (file_path.listFiles() != null) {
            		if (file_path.listFiles().length > 0) {
            			for (File file : file_path.listFiles()) {    					
            	        	Map<String, Object> map = new HashMap<String, Object>();    		        	
            	        	map.put("item_name", file.getName());            	        	
            	        	map.put("file_path", file.getAbsolutePath());
            	        	
            	        	if (file.isDirectory()) {
            	        		map.put("item_sel", R.drawable.item_img_nosel);
            	        		map.put("item_type", R.drawable.item_type_dir);
            	        		
            	        		String rw = "d";
            	        		if (file.canRead()) rw += "r"; else rw += "-";
            	        		if (file.canWrite()) rw += "w"; else rw += "-";  
            	        		map.put("item_rw", rw);       
            	        		
            	        		long file_date = file.lastModified();
            	        		String date = new SimpleDateFormat("yyyy/MM/dd HH:mm")
            	        			.format(new Date(file_date));
            	        		map.put("item_date", " | " + date + " | ");
            	        		map.put("file_date", file_date);	//use for sorting
            	        		
            	        		long file_size = file.length();
            	        		map.put("file_size", file_size);	//use for sorting
            	        		map.put("item_size", "");            	        		
            	        	} else {
            	        		map.put("item_sel", R.drawable.item_img_unsel);            	        		
            	        		map.put("item_type", FileOp.getFileTypeImg(file.getName()));
            	        		
            	        		String rw = "-";
            	        		if (file.canRead()) rw += "r"; else rw += "-";
            	        		if (file.canWrite()) rw += "w"; else rw += "-";  
            	        		map.put("item_rw", rw);       
            	        		
            	        		long file_date = file.lastModified();
            	        		String date = new SimpleDateFormat("yyyy/MM/dd HH:mm")
            	        			.format(new Date(file_date));
            	        		map.put("item_date", " | " + date + " | ");
            	        		map.put("file_date", file_date);	//use for sorting
            	        		
            	        		long file_size = file.length();
            	        		map.put("file_size", file_size);	//use for sorting
            	        		map.put("item_size", FileOp.getFileSizeStr(file_size));
            	        		
            	        		
            	        	}
            	        	
            	        	list.add(map);    		        	
            			}
            		}            		
        		}
        		updatePathShow(path);
        	}
    	} catch (Exception e) {
    		Log.e(TAG, "Exception when getFileListData(): ", e);
    		return list;
		}   
    	
		/* sorting */
    	if (!list.isEmpty()) {    	
        	if (sort_type.equals("by_name")) {
        		Collections.sort(list, new Comparator<Map<String, Object>>() {
    				
    				public int compare(Map<String, Object> object1,
    						Map<String, Object> object2) {	
    					return ((String) object1.get("item_name")).compareTo((String) object2.get("item_name"));					
    				}    			
        		});           		
        		
        	} else if (sort_type.equals("by_date")) {
        		Collections.sort(list, new Comparator<Map<String, Object>>() {
    				
    				public int compare(Map<String, Object> object1,
    						Map<String, Object> object2) {	
    					return ((Long) object1.get("file_date")).compareTo((Long) object2.get("file_date"));					
    				}    			
        		});         		
        	} else if (sort_type.equals("by_size")) {
        		Collections.sort(list, new Comparator<Map<String, Object>>() {
    				
    				public int compare(Map<String, Object> object1,
    						Map<String, Object> object2) {	
    					return ((Long) object1.get("file_size")).compareTo((Long) object2.get("file_size"));					
    				}    			
        		});         		
        	}   		
 		
    	}
    	
    	return list;
 	}  
    
}