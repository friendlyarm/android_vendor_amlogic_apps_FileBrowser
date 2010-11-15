package com.amlogic.FileBrower;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
	
	private ListView lv;
	private TextView tv;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.main);        
        
        /* setup file list */
        lv = (ListView) findViewById(R.id.listview);  
        //lv.setAdapter(getDevListAdapter());
        lv.setAdapter(getFileListAdapter(ROOT_PATH));
        
        /* lv OnItemClickListener */
        lv.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				Map<String, Object> item = (Map<String, Object>)parent.getItemAtPosition(pos);
			
				String file_path = (String) item.get("file_path");
				File file = new File(file_path);

				if (file.isDirectory()) {	
					prev_path = cur_path;
					cur_path = file_path;
					lv.setAdapter(getFileListAdapter(file_path));	
				}
				else {				
					if (item.get("item_sel").equals(R.drawable.item_img_unsel))
						item.put("item_sel", R.drawable.item_img_sel);
					else if (item.get("item_sel").equals(R.drawable.item_img_sel))
						item.put("item_sel", R.drawable.item_img_unsel);
					
					((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();	
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
					lv.setAdapter(getFileListAdapter(parent_path));
				}
			}        	
        });
        
        /* btn_home listener */
        Button btn_home = (Button) findViewById(R.id.btn_home);  
        btn_home.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!cur_path.equals(ROOT_PATH)) {
					String home_path = ROOT_PATH;
					prev_path = cur_path;
					cur_path = home_path;
					lv.setAdapter(getFileListAdapter(home_path));
				}
			}        	
        });       
        
        /* btn_close_listener */
        Button btn_close = (Button) findViewById(R.id.btn_close);  
        btn_close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}        	
        });     
        
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
            .show();  
            
            Button sort_btn_close = (Button) sort_dialog.getWindow().findViewById(R.id.sort_btn_close);  
            sort_btn_close.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    				sort_dialog.dismiss();
    			}        	
            });      
            Button sort_btn_name = (Button) sort_dialog.getWindow().findViewById(R.id.sort_btn_name);  
            sort_btn_name.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    				Log.i(TAG, "do: sort_btn_name");
    				sort_dialog.dismiss();
    			}        	
            });  
            Button sort_btn_date = (Button) sort_dialog.getWindow().findViewById(R.id.sort_btn_date);  
            sort_btn_date.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    				Log.i(TAG, "do: sort_btn_date");
    				sort_dialog.dismiss();
    			}        	
            });   
            Button sort_btn_size = (Button) sort_dialog.getWindow().findViewById(R.id.sort_btn_size);  
            sort_btn_size.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    				Log.i(TAG, "do: sort_btn_size");
    				sort_dialog.dismiss();
    			}        	
            });             
            return sort_dialog;

        case EDIT_DIALOG_ID:
	        LayoutInflater inflater1 = (LayoutInflater) FileBrower.this
	    	.getSystemService(LAYOUT_INFLATER_SERVICE);
	    	View layout_edit = inflater1.inflate(R.layout.edit_dialog_layout,
	    		(ViewGroup) findViewById(R.id.layout_root_edit));
	    	
	    	edit_dialog = new AlertDialog.Builder(FileBrower.this)   
	    	.setView(layout_edit)
	        .show();   
	    	
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
            
	    	return edit_dialog;
        	
        case CLICK_DIALOG_ID:
        	return new AlertDialog.Builder(FileBrower.this)                
            .setTitle("click dialog")     
            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
          	
                	/* User clicked Cancel so do some stuff */                    	
                }
            })             
            .create();  
            
        }
        
		return null;    	
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	switch (id) {
    	case SORT_DIALOG_ID: 
    		{
            WindowManager wm = getWindowManager();
            Display display = wm.getDefaultDisplay();
            LayoutParams lp = dialog.getWindow().getAttributes();
            if (display.getHeight() > display.getWidth()) {            	
            	lp.width = (int) (display.getWidth() * 1.0);       	
        	} else {        		
        		lp.width = (int) (display.getWidth() * 0.5);            	
        	}
            dialog.getWindow().setAttributes(lp);   
    		}
            break;
    	case EDIT_DIALOG_ID:
    		{
            WindowManager wm = getWindowManager();
            Display display = wm.getDefaultDisplay();
            LayoutParams lp = dialog.getWindow().getAttributes();
            if (display.getHeight() > display.getWidth()) {            	
            	lp.width = (int) (display.getWidth() * 1.0);       	
        	} else {        		
        		lp.width = (int) (display.getWidth() * 0.5);            	
        	}
            dialog.getWindow().setAttributes(lp);  
    		}
    		break;
    	case CLICK_DIALOG_ID:
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
            	        		
            	        		String rw = "";
            	        		if (file.canRead()) rw += "r"; else rw += "-";
            	        		if (file.canWrite()) rw += "w"; else rw += "-";  
            	        		map.put("item_rw", rw);       
            	        		
            	        		long file_date = file.lastModified();
            	        		String date = new SimpleDateFormat("yyyy/MM/dd")
            	        			.format(new Date(file_date));
            	        		map.put("item_date", date + " | ");
            	        		map.put("file_date", file_date);	//use for sorting
            	        		
            	        		long file_size = file.length();
            	        		map.put("file_size", file_size);	//use for sorting
            	        		map.put("item_size", " | ");            	        		
            	        	} else {
            	        		map.put("item_sel", R.drawable.item_img_unsel);            	        		
            	        		map.put("item_type", FileOp.getFileTypeImg(file.getName()));
            	        		
            	        		String rw = "";
            	        		if (file.canRead()) rw += "r"; else rw += "-";
            	        		if (file.canWrite()) rw += "w"; else rw += "-";  
            	        		map.put("item_rw", rw);       
            	        		
            	        		long file_date = file.lastModified();
            	        		String date = new SimpleDateFormat("yyyy/MM/dd")
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
   

    
}