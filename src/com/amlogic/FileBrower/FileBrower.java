package com.amlogic.FileBrower;

import android.os.storage.*;
import java.io.ByteArrayOutputStream;
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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amlogic.FileBrower.FileBrowerDatabase.FileMarkCursor;
import com.amlogic.FileBrower.FileBrowerDatabase.ThumbnailCursor;
import com.amlogic.FileBrower.FileOp.FileOpReturn;
import com.amlogic.FileBrower.FileOp.FileOpTodo;

public class FileBrower extends Activity {
	public static final String TAG = "FileBrower";
	
	private static final String ROOT_PATH = "/mnt";
	public static String cur_path = ROOT_PATH;
	private static String prev_path = ROOT_PATH;
	private static final int SORT_DIALOG_ID = 0;
	private static final int EDIT_DIALOG_ID = 1;
	private static final int CLICK_DIALOG_ID = 2;
	private static final int HELP_DIALOG_ID = 3;
	private static String exit_path = ROOT_PATH;
	private AlertDialog sort_dialog;	
	private AlertDialog edit_dialog;
	private AlertDialog click_dialog;
	private AlertDialog help_dialog;
	private ListView sort_lv;
	private ListView edit_lv;
	private ListView click_lv;
	private ListView help_lv;
	private boolean local_mode;
	public static FileBrowerDatabase db;
	public static FileMarkCursor myCursor;
	public static ThumbnailCursor myThumbCursor;
	public static  Handler mProgressHandler;
	private ListView lv;
	private TextView tv;
	private List<String> devList = new ArrayList<String>();
	private int request_code = 1550;

	String open_mode[] = {"movie","music","photo","packageInstall"};
	
	
    private final StorageEventListener mListener = new StorageEventListener() {
        public void onUsbMassStorageConnectionChanged(boolean connected)
        {
        	//this is the action when connect to pc
        	return ;
        }
        public void onStorageStateChanged(String path, String oldState, String newState)
        {
        	if (newState == null || path == null) 
        		return;
        	
        	if(newState.compareTo("mounted") == 0)
        	{
        		//Log.w(path, "mounted.........");
        		//ThumbnailOpUtils.updateThumbnailsForDev(getBaseContext(), path);
        		if (cur_path.equals(ROOT_PATH)) {
        			DeviceScan();
        		}
        		
        	}
        	else if(newState.compareTo("unmounted") == 0)
        	{
        		//Log.w(path, "unmounted.........");
        		if (cur_path.startsWith(path)) {
        			cur_path = ROOT_PATH;
        			DeviceScan();
        		}
        		if (cur_path.equals(ROOT_PATH)) {
        			DeviceScan();
        		}
        		FileOp.cleanFileMarks("list");
        	}
        	else if(newState.compareTo("removed") == 0)
        	{
        		//Log.w(path, "removed.........");
        		if (cur_path.startsWith(path)) {
        			cur_path = ROOT_PATH;
        			DeviceScan();
        		}
        		if (cur_path.equals(ROOT_PATH)) {
        			DeviceScan();
        		}        		
        	}
        }
        
    };

    /** Called when the activity is first created or resumed. */
    @Override
    public void onResume() {
        super.onResume();
        StorageManager m_storagemgr = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		m_storagemgr.registerListener(mListener);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        StorageManager m_storagemgr = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        m_storagemgr.unregisterListener(mListener);
    }
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.main);
        
        //Log.i(TAG, "category =" + getIntent().getCategories());
        
        /* setup database */
        db = new FileBrowerDatabase(this); 

        /* btn_mode default checked */
        ToggleButton btn_mode = (ToggleButton) findViewById(R.id.btn_mode); 
        btn_mode.setChecked(true);
        
        /* setup file list */
        lv = (ListView) findViewById(R.id.listview);  
        local_mode = false;
        //lv.setAdapter(getFileListAdapter(ROOT_PATH)); 
        if(!(FileOp.GetMode())){
        	cur_path = ROOT_PATH;
        	prev_path = ROOT_PATH;
        	
        }  
        else{
        	Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();  
            cur_path = bundle.getString("cur_path");
        	FileOp.SetMode(false);
        }
        if(cur_path.equals(ROOT_PATH)){       	
        	 DeviceScan();
        }
        else{
        	lv.setAdapter(getFileListAdapter(cur_path));
        }
        
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
					if (!btn_mode.isChecked()){
						openFile(file);
						//showDialog(CLICK_DIALOG_ID);
						
					}
					else {
						if (item.get("item_sel").equals(R.drawable.item_img_unsel)) {
							FileOp.updateFileStatus(file_path, 1,"list");
							item.put("item_sel", R.drawable.item_img_sel);
						}
						else if (item.get("item_sel").equals(R.drawable.item_img_sel)) {
							FileOp.updateFileStatus(file_path, 0,"list");
							item.put("item_sel", R.drawable.item_img_unsel);
						}
						
						((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();	
					}
				}
								
			}        	
        });      
        
        /* lv OnItemLongClickListener */
        /* TODO
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
        */
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
				prev_path = cur_path;
				cur_path = ROOT_PATH;
				DeviceScan();
			}        	
        });       
        
        /* btn_close_listener */
        Button btn_close = (Button) findViewById(R.id.btn_close);  
        btn_close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				FileOp.SetMode(false);
				finish();
			}        	
        });    
        
        /* btn_edit_listener */
        Button btn_edit = (Button) findViewById(R.id.btn_edit);  
        btn_edit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!cur_path.equals(ROOT_PATH))
					showDialog(EDIT_DIALOG_ID);
				else {
        			Toast.makeText(FileBrower.this,
        					getText(R.string.Toast_msg_edit_noopen),
        					Toast.LENGTH_SHORT).show();  	
        		}	
			}        	
        });         
        
        /* btn_sort_listener */
        Button btn_sort = (Button) findViewById(R.id.btn_sort);  
        btn_sort.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!cur_path.equals(ROOT_PATH))
					showDialog(SORT_DIALOG_ID);
				else {
        			Toast.makeText(FileBrower.this,
        					getText(R.string.Toast_msg_sort_noopen),
        					Toast.LENGTH_SHORT).show();  					
				}
			}        	
        });   
        
        /* btn_help_listener */
        Button btn_help = (Button) findViewById(R.id.btn_help);  
        btn_help.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(HELP_DIALOG_ID);
			}
        });  
		
        /* btn_istswitch_listener */
        Button btn_listswitch = (Button) findViewById(R.id.btn_listswitch);  
        btn_listswitch.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			FileOp.SetMode(true);
    			Intent intent = new Intent();
    			intent.setClass(FileBrower.this, ThumbnailView1.class);
    			/*  */
    			Bundle mybundle = new Bundle();
    			
    			mybundle.putString("cur_path", cur_path);
    			intent.putExtras(mybundle);			
    			startActivityForResult(intent,request_code);
    			/*  */
    			local_mode = true;
    			FileBrower.this.finish();   	
    		}
    		   			       		
        }); 
        
        /** edit process bar handler
         *  mProgressHandler.sendMessage(Message.obtain(mProgressHandler, msg.what, msg.arg1, msg.arg2));            
         */
        mProgressHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                
                ProgressBar pb = null;
                if (edit_dialog != null)
                	pb = (ProgressBar) edit_dialog.findViewById(R.id.edit_progress_bar);
              
                switch(msg.what) {
                case 0: 	//set invisible
                    if ((edit_dialog != null) && (pb != null)) {                    	
                	pb.setVisibility(View.INVISIBLE);
                    }
                	break;                
                case 1:		//set progress_bar1 
                	if ((edit_dialog != null) && (pb != null)) {  
                		pb.setProgress(msg.arg1);
                 	}
                	break;
                case 2:		//set progress_bar2
                	if ((edit_dialog != null) && (pb != null)) {  
                		pb.setSecondaryProgress(msg.arg1);  
                	}
                	break;
                case 3:		//set visible
                	if ((edit_dialog != null) && (pb != null)) {  
	                	pb.setProgress(0);
	                	pb.setSecondaryProgress(0);    
	                	pb.setVisibility(View.VISIBLE);
                	}
                	break;
                case 4:		//file paste ok
        			db.deleteAllFileMark();
        			lv.setAdapter(getFileListAdapter(cur_path)); 
        			ThumbnailOpUtils.updateThumbnailsForDir(getBaseContext(), cur_path);
        			Toast.makeText(FileBrower.this,
        					getText(R.string.Toast_msg_paste_ok),
        					Toast.LENGTH_SHORT).show();       
        			FileOp.file_op_todo = FileOpTodo.TODO_NOTHING;
                    if (edit_dialog != null)
                    	edit_dialog.dismiss();                    	
                	
                	break;
                case 5:		//file paste err
        			Toast.makeText(FileBrower.this,
        					getText(R.string.Toast_msg_paste_nofile),
        					Toast.LENGTH_SHORT).show();   
        			FileOp.file_op_todo = FileOpTodo.TODO_NOTHING;
                    if (edit_dialog != null)
                    	edit_dialog.dismiss();   
                	break;
                }
                
            }
        };

    }
    /** onDestory() */
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	if(!local_mode){
    		db.deleteAllFileMark();  
    		cur_path = ROOT_PATH;
    		
    	}  	  	
    	db.close();    	    	   	
    }
    
protected void openFile(File f) {
		// TODO Auto-generated method stub	
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        String type = "*/*";        
        type = FileOp.CheckMediaType(f);
        intent.setDataAndType(Uri.fromFile(f),type);
        startActivity(intent);      		
	}
protected void onActivityResult(int requestCode, int resultCode,Intent data) {
// TODO Auto-generated method stub
	 super.onActivityResult(requestCode, resultCode, data);
	 switch (resultCode) {
	 case RESULT_OK:
		 /* */
		 //Intent intent = this.getIntent();
		 Bundle bundle = data.getExtras();
		 cur_path = bundle.getString("cur_path");		
		 break;
	 default:
		 break;
	 }
}   
    
    private void DeviceScan() {
		// TODO Auto-generated method stub
    	devList.clear();
    	String internal = getString(R.string.memory_device_str);
    	String sdcard = getString(R.string.sdcard_device_str);
    	String usb = getString(R.string.usb_device_str);
    	String DeviceArray[]={internal,sdcard,usb};   	
    	for(int i=0;i<DeviceArray.length;i++){
    		if(FileOp.deviceExist(DeviceArray[i])){
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
        	"item_size"
        	},        		
                new int[]{
        	R.id.device_type,
        	R.id.device_name,        	
        	R.id.device_rw,       	
        	R.id.device_size
        	});  		
	}
/*
	private List<? extends Map<String, ?>> getDeviceListData() {
		// TODO Auto-generated method stub
		String file_path = null;
		String device = null;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();              
    	for(int i = 0; i < this.devList.size(); i++) {   
    		Map<String, Object> map = new HashMap<String, Object>();    		
    		map.put("item_name", this.devList.get(i)); 
    		file_path = FileOp.convertDeviceName(this,this.devList.get(i));
    		map.put("file_path", file_path);         
    		map.put("item_size", null);
    		map.put("item_rw", null);
    		map.put("item_type", FileOp.getDeviceIcon(file_path));    		
    		list.add(map); 
    	}
    	device = getString(R.string.rootDevice);
    	updatePathShow(device);
    	return list; 
	}
	*/
	private List<Map<String, Object>> getDeviceListData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
		Map<String, Object> map;
		/*
		map = new HashMap<String, Object>();
		map.put("item_name", getText(R.string.memory_device_str));
		map.put("file_path", "/mnt/flash");
		map.put("item_type", R.drawable.memory_default);
		map.put("file_date", 0);
		map.put("file_size", 0);
		map.put("item_sel", R.drawable.item_img_unsel);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("item_name", getText(R.string.sdcard_device_str));
		map.put("file_path", "/mnt/sdcard");
		map.put("item_type", R.drawable.sdcard_default);
		map.put("file_date", 0);
		map.put("file_size", 0);
		map.put("item_sel", R.drawable.item_img_unsel);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("item_name", getText(R.string.usb_device_str));
		map.put("file_path", "/mnt/usb");
		map.put("item_type", R.drawable.usb_default);
		map.put("file_date", 0);
		map.put("file_size", 0);
		map.put("item_sel", R.drawable.item_img_unsel);
		list.add(map);		
		*/
        File dir = new File("/mnt");
		if (dir.exists() && dir.isDirectory()) {
			if (dir.listFiles() != null) {
				if (dir.listFiles().length > 0) {
					for (File file : dir.listFiles()) {
						if (file.isDirectory()) {
							String path = file.getAbsolutePath();            								
							if (path.equals("/mnt/flash")) {
								map = new HashMap<String, Object>();
								map.put("item_name", getText(R.string.memory_device_str));
								map.put("file_path", "/mnt/flash");
								map.put("item_type", R.drawable.memory_icon);
								map.put("file_date", 0);
								map.put("file_size", 0);	//for sort
					    		map.put("item_size", null);
					    		map.put("item_rw", null);
								list.add(map);								
							} else if (path.equals("/mnt/sdcard")) {
								map = new HashMap<String, Object>();
								map.put("item_name", getText(R.string.sdcard_device_str));
								map.put("file_path", "/mnt/sdcard");
								map.put("item_type", R.drawable.sd_card_icon);
								map.put("file_date", 0);
								map.put("file_size", 1);	//for sort
					    		map.put("item_size", null);
					    		map.put("item_rw", null);
								list.add(map);								
							} else if (path.equals("/mnt/usb")) {
								map = new HashMap<String, Object>();
								map.put("item_name", getText(R.string.usb_device_str) + 
										" " + file.getName());
								map.put("file_path", "/mnt/usb");
								map.put("item_type", R.drawable.usb_card_icon);
								map.put("file_date", 0);
								map.put("file_size", 2);	//for sort
					    		map.put("item_size", null);
					    		map.put("item_rw", null);
								list.add(map);									
							} else if (path.startsWith("/mnt/sd")) {
								map = new HashMap<String, Object>();
								map.put("item_name", getText(R.string.usb_device_str) + 
										" " + file.getName());
								map.put("file_path", path);
								map.put("item_type", R.drawable.usb_card_icon);
								map.put("file_date", 0);
								map.put("file_size", 3);	//for sort
					    		map.put("item_size", null);
					    		map.put("item_rw", null);
								list.add(map);	
							}
						}
					}
				}
			}
		}
		updatePathShow(ROOT_PATH);
    	if (!list.isEmpty()) { 
    		Collections.sort(list, new Comparator<Map<String, Object>>() {
				
				public int compare(Map<String, Object> object1,
						Map<String, Object> object2) {	
					return ((Integer) object1.get("file_size")).compareTo((Integer) object2.get("file_size"));					
				}    			
    		}); 
    }
		return list;
	}	
    /** Dialog */
    @Override
    protected Dialog onCreateDialog(int id) {
    	LayoutInflater inflater = (LayoutInflater) FileBrower.this
		.getSystemService(LAYOUT_INFLATER_SERVICE);
    	
        switch (id) {
        case SORT_DIALOG_ID:
        	View layout_sort = inflater.inflate(R.layout.sort_dialog_layout,
        		(ViewGroup) findViewById(R.id.layout_root_sort));
        	
            sort_dialog =  new AlertDialog.Builder(FileBrower.this)   
        	.setView(layout_sort)
            .create(); 
            return sort_dialog;

        case EDIT_DIALOG_ID:
	    	View layout_edit = inflater.inflate(R.layout.edit_dialog_layout,
	    		(ViewGroup) findViewById(R.id.layout_root_edit));
	    	
	    	edit_dialog = new AlertDialog.Builder(FileBrower.this)   
	    	.setView(layout_edit)
	        .create();             
	    	return edit_dialog;
        	
        case CLICK_DIALOG_ID:
	    	View layout_click = inflater.inflate(R.layout.click_dialog_layout,
	    		(ViewGroup) findViewById(R.id.layout_root_click));
	    	
	    	click_dialog = new AlertDialog.Builder(FileBrower.this)   
	    	.setView(layout_click)
	        .create();
	    	return click_dialog;	    	
        case HELP_DIALOG_ID:
	    	View layout_help = inflater.inflate(R.layout.help_dialog_layout,
		    		(ViewGroup) findViewById(R.id.layout_root_help));
		    	
		    	help_dialog = new AlertDialog.Builder(FileBrower.this)   
		    	.setView(layout_help)
		        .create();
		    return help_dialog;	
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
            
            sort_lv = (ListView) sort_dialog.getWindow().findViewById(R.id.sort_listview);  
            sort_lv.setAdapter(getDialogListAdapter(SORT_DIALOG_ID));	
            
            sort_lv.setOnItemClickListener(new OnItemClickListener() {
            	public void onItemClick(AdapterView<?> parent, View view, int pos,
    					long id) {    				
    				
            		if (!cur_path.equals(ROOT_PATH)) {
            			if (pos == 0)
            				lv.setAdapter(getFileListAdapterSorted(cur_path, "by_name"));
            			else if (pos == 1)
            				lv.setAdapter(getFileListAdapterSorted(cur_path, "by_date"));
            			else if (pos == 2)
            				lv.setAdapter(getFileListAdapterSorted(cur_path, "by_size"));
            		}
            		sort_dialog.dismiss();
            		
    				/*//todo
    				Map<String, Object> item = (Map<String, Object>)parent.getItemAtPosition(pos);
					if (item.get("item_sel").equals(R.drawable.dialog_item_img_unsel))
						item.put("item_sel", R.drawable.dialog_item_img_sel);
					else if (item.get("item_sel").equals(R.drawable.dialog_item_img_sel))
						item.put("item_sel", R.drawable.dialog_item_img_unsel);
					
					((BaseAdapter) sort_lv.getAdapter()).notifyDataSetChanged();	  
					*/  				
    			}
            	
            });
	    	Button sort_btn_close = (Button) sort_dialog.getWindow().findViewById(R.id.sort_btn_close);  
	    	sort_btn_close.setOnClickListener(new OnClickListener() {
	    		public void onClick(View v) {
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

            mProgressHandler.sendMessage(Message.obtain(mProgressHandler, 0));
            edit_lv = (ListView) edit_dialog.getWindow().findViewById(R.id.edit_listview);  
            edit_lv.setAdapter(getDialogListAdapter(EDIT_DIALOG_ID));	
            
            edit_lv.setOnItemClickListener(new OnItemClickListener() {
            	public void onItemClick(AdapterView<?> parent, View view, int pos,
    					long id) {
            		if (!cur_path.equals(ROOT_PATH)) {
            			if (pos == 0) {
            				//Log.i(TAG, "DO cut...");            				
            		        try {        	
            		        	myCursor = db.getFileMark();   
            			        if (myCursor.getCount() > 0) {
                					Toast.makeText(FileBrower.this,
                							getText(R.string.Toast_msg_cut_todo),
                							Toast.LENGTH_SHORT).show();  
                					FileOp.file_op_todo = FileOpTodo.TODO_CUT;
            			        } else {
                					Toast.makeText(FileBrower.this,
                							getText(R.string.Toast_msg_cut_nofile),
                							Toast.LENGTH_SHORT).show();    
                					FileOp.file_op_todo = FileOpTodo.TODO_NOTHING;
            			        }
            		        } finally {        	
            		        	myCursor.close();        	
            		        }  
      
        					edit_dialog.dismiss();
            			}
            			else if (pos == 1) {
            				//Log.i(TAG, "DO copy...");            				
            		        try {        	
            		        	myCursor = db.getFileMark();   
            			        if (myCursor.getCount() > 0) {
                					Toast.makeText(FileBrower.this,
                							getText(R.string.Toast_msg_cpy_todo),
                							Toast.LENGTH_SHORT).show();  
                					FileOp.file_op_todo = FileOpTodo.TODO_CPY;
            			        } else {
                					Toast.makeText(FileBrower.this,
                							getText(R.string.Toast_msg_cpy_nofile),
                							Toast.LENGTH_SHORT).show();     
                					FileOp.file_op_todo = FileOpTodo.TODO_NOTHING;
            			        }
            		        } finally {        	
            		        	myCursor.close();        	
            		        }       
        					edit_dialog.dismiss();
            			}
            			else if (pos == 2) {
            				//Log.i(TAG, "DO paste...");     
            				
            				new Thread () {
            					public void run () {
            						try {
            							FileOp.pasteSelectedFile("list");
            						} catch(Exception e) {
            							Log.e("Exception when paste file", e.toString());
            						}
            					}
            				}.start();
            				            				
            			}
            			else if (pos == 3) {
            				FileOp.file_op_todo = FileOpTodo.TODO_NOTHING;
            				//Log.i(TAG, "DO delete...");   
            				if (FileOpReturn.SUCCESS == FileOp.deleteSelectedFile("list")) {
            					db.deleteAllFileMark();
                				lv.setAdapter(getFileListAdapter(cur_path));  
                				Toast.makeText(FileBrower.this,
                						getText(R.string.Toast_msg_del_ok),
                						Toast.LENGTH_SHORT).show();
            				} else {
            					Toast.makeText(FileBrower.this,
            							getText(R.string.Toast_msg_del_nofile),
            							Toast.LENGTH_SHORT).show();
            				}         				          				
            				edit_dialog.dismiss();
            			}
            		} else {
    					Toast.makeText(FileBrower.this,
    							getText(R.string.Toast_msg_paste_wrongpath),
    							Toast.LENGTH_SHORT).show();
    					edit_dialog.dismiss();
            		}            		
				
    			}            	
            });            
	    	Button edit_btn_close = (Button) edit_dialog.getWindow().findViewById(R.id.edit_btn_close);  
	    	edit_btn_close.setOnClickListener(new OnClickListener() {
	    		public void onClick(View v) {
	    			if((FileOp.copying_file!=null)&&(FileOp.copying_file.exists()))
	    				FileOp.copying_file.delete();
	    			Toast.makeText(FileBrower.this,
							getText(R.string.Toast_copy_fail),
							Toast.LENGTH_SHORT).show();
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
 
            click_lv = (ListView) click_dialog.getWindow().findViewById(R.id.click_listview);  
            click_lv.setAdapter(getDialogListAdapter(CLICK_DIALOG_ID));	
            
	    	Button click_btn_close = (Button) click_dialog.getWindow().findViewById(R.id.click_btn_close);  
	    	click_btn_close.setOnClickListener(new OnClickListener() {
	    		public void onClick(View v) {
	    			click_dialog.dismiss();
	    		}        	
	        }); 			
    		break;
    	case HELP_DIALOG_ID:
            if (display.getHeight() > display.getWidth()) {            	
            	lp.width = (int) (display.getWidth() * 1.0);       	
        	} else {        		
        		lp.width = (int) (display.getWidth() * 0.5);            	
        	}
            dialog.getWindow().setAttributes(lp);   
            
            help_lv = (ListView) help_dialog.getWindow().findViewById(R.id.help_listview);  
            help_lv.setAdapter(getDialogListAdapter(HELP_DIALOG_ID));	
            
            help_lv.setOnItemClickListener(new OnItemClickListener() {
            	public void onItemClick(AdapterView<?> parent, View view, int pos,
    					long id) { 
            		help_dialog.dismiss();				
    			}
            	
            });
	    	Button help_btn_close = (Button) help_dialog.getWindow().findViewById(R.id.help_btn_close);  
	    	help_btn_close.setOnClickListener(new OnClickListener() {
	    		public void onClick(View v) {
	    			help_dialog.dismiss();
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
            	        	String file_abs_path = file.getAbsolutePath();
            	        	map.put("file_path", file_abs_path);
            	        	
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
            	        		if (FileOp.isFileSelected(file_abs_path,"list"))
            	        			map.put("item_sel", R.drawable.item_img_sel); 
            	        		else
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
		if (path.equals(ROOT_PATH))
			tv.setText(getText(R.string.rootDevice));
		else
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
            	        	String file_abs_path = file.getAbsolutePath();
            	        	map.put("file_path", file_abs_path);
            	        	
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
            	        		if (FileOp.isFileSelected(file_abs_path,"list"))
            	        			map.put("item_sel", R.drawable.item_img_sel); 
            	        		else
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
    
    /** getDialogListAdapter */
    private SimpleAdapter getDialogListAdapter(int id) {
        return new SimpleAdapter(FileBrower.this,
        		getDialogListData(id),
        		R.layout.dialog_item,        		
                new String[]{
        	"item_type",
        	"item_name",
        	"item_sel",
        	},        		
                new int[]{
        	R.id.dialog_item_type,
        	R.id.dialog_item_name,
        	R.id.dialog_item_sel,
        	});  
    }
    /** getFileListData */
    private List<Map<String, Object>> getDialogListData(int id) { 
    	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();  
    	Map<String, Object> map; 
    	
    	switch (id) {
    	case SORT_DIALOG_ID:  	
    		map = new HashMap<String, Object>();     		
        	map.put("item_type", R.drawable.dialog_item_type_name);  
        	map.put("item_name", getText(R.string.sort_dialog_name_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);  
        	list.add(map);
        	map = new HashMap<String, Object>();         	
        	map.put("item_type", R.drawable.dialog_item_type_date);  
        	map.put("item_name", getText(R.string.sort_dialog_date_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);  
        	list.add(map);    	
        	map = new HashMap<String, Object>();         	
        	map.put("item_type", R.drawable.dialog_item_type_size);  
        	map.put("item_name", getText(R.string.sort_dialog_size_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);  
        	list.add(map);       	
        	break; 
        	
    	case EDIT_DIALOG_ID: 
    		map = new HashMap<String, Object>();    		
        	map.put("item_type", R.drawable.dialog_item_type_cut);  
        	map.put("item_name", getText(R.string.edit_dialog_cut_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);   
        	list.add(map);
    		map = new HashMap<String, Object>();    		
        	map.put("item_type", R.drawable.dialog_item_type_copy);  
        	map.put("item_name", getText(R.string.edit_dialog_copy_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);  
        	list.add(map);
        	map = new HashMap<String, Object>();         	
        	map.put("item_type", R.drawable.dialog_item_type_paste);  
        	map.put("item_name", getText(R.string.edit_dialog_paste_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);  
        	list.add(map);    	
        	map = new HashMap<String, Object>();         	
        	map.put("item_type", R.drawable.dialog_item_type_delete);  
        	map.put("item_name", getText(R.string.edit_dialog_delete_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);  
        	list.add(map);     		
    		break; 
    		
    	case CLICK_DIALOG_ID:
    		for(int i=0;i<open_mode.length;i++){
    			map = new HashMap<String, Object>();  
    			map.put("item_type", R.drawable.dialog_item_img_unsel);        	
    			map.put("item_name", open_mode[i]);       	      	            	        	
    			map.put("item_sel", R.drawable.dialog_item_img_unsel);   
    			list.add(map); 
    		}
    		break;
    	case HELP_DIALOG_ID:
    		map = new HashMap<String, Object>();    		
        	map.put("item_type", R.drawable.dialog_help_item_home);  
        	map.put("item_name", getText(R.string.dialog_help_item_home_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);   
        	list.add(map);    	
    		map = new HashMap<String, Object>();    		
        	map.put("item_type", R.drawable.dialog_help_item_mode);  
        	map.put("item_name", getText(R.string.dialog_help_item_mode_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);   
        	list.add(map);  
    		map = new HashMap<String, Object>();    		
        	map.put("item_type", R.drawable.dialog_help_item_edit);  
        	map.put("item_name", getText(R.string.dialog_help_item_edit_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);   
        	list.add(map);   
    		map = new HashMap<String, Object>();    		
        	map.put("item_type", R.drawable.dialog_help_item_sort);  
        	map.put("item_name", getText(R.string.dialog_help_item_sort_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);   
        	list.add(map);    
    		map = new HashMap<String, Object>();    		
        	map.put("item_type", R.drawable.dialog_help_item_parent);  
        	map.put("item_name", getText(R.string.dialog_help_item_parent_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);   
        	list.add(map);      
    		map = new HashMap<String, Object>();    		
        	map.put("item_type", R.drawable.dialog_help_item_thumb);  
        	map.put("item_name", getText(R.string.dialog_help_item_thumb_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);   
        	list.add(map); 
    		map = new HashMap<String, Object>();    		
        	map.put("item_type", R.drawable.dialog_help_item_list);  
        	map.put("item_name", getText(R.string.dialog_help_item_list_str));            	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);   
        	list.add(map);     
    		map = new HashMap<String, Object>();    		
        	map.put("item_type", R.drawable.dialog_help_item_close);  
        	String ver_str = " ";
          	try {
          		ver_str += getPackageManager().getPackageInfo("com.amlogic.FileBrower", 0).versionName;			
     		} catch (NameNotFoundException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}      
        	map.put("item_name", getText(R.string.dialog_help_item_close_str) + ver_str);           	        	
        	map.put("item_sel", R.drawable.dialog_item_img_unsel);   
        	list.add(map);          	
    		break;
    	}
    	return list;
    }    
    
     
}