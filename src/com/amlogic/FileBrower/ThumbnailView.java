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

import com.amlogic.FileBrower.FileBrowerDatabase.FileMarkCursor;
import com.amlogic.FileBrower.FileOp.FileOpReturn;
import com.amlogic.FileBrower.FileOp.FileOpTodo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

    /** Called when the activity is first created. */
public class ThumbnailView extends Activity{
	public static final String TAG = "ThumbnailView";	
	protected static final int SORT_DIALOG_ID = 0;
	protected static final int EDIT_DIALOG_ID = 1;
	private AlertDialog sort_dialog;	
	private AlertDialog edit_dialog;
	private ListView sort_lv;
	private ListView edit_lv;
	
	
	public static FileBrowerDatabase db;
	public static FileMarkCursor myCursor;
	private List<String> filelist = new ArrayList<String>();
	
	GridView ThumbnailView;	
	String ROOT_PATH = "/mnt";
	String cur_path = ROOT_PATH;
	int request_code = 1550;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);         
        setContentView(R.layout.thumbnail);       
        ThumbnailView = (GridView)findViewById(R.id.mygridview);
        
        /*get cur path form listview*/
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();        
        cur_path = bundle.getString("cur_path");
          
        
        if(cur_path.equals(ROOT_PATH)){
        	DeviceScan();
        	
        }
        else{
        	ThumbnailView.setAdapter(getThumbnailAdapter(cur_path,null)); 
        }
        
        
       
        
        /* setup database */
        db = new FileBrowerDatabase(this); 

        /* btn_mode default checked */
        ToggleButton btn_mode = (ToggleButton) findViewById(R.id.btn_thumbmode); 
        btn_mode.setChecked(true);
        
        ThumbnailView.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stutus
				Map<String, Object> item = (Map<String, Object>)ThumbnailView.getItemAtPosition((int)arg3);
				String file_path = (String)item.get("file_path");;
				File file = new File(file_path);
				if(!file.exists()){
					//finish();
					return;
				}

				if (file.isDirectory()) {						
					cur_path = file_path;
					ThumbnailView.setAdapter(getThumbnailAdapter(cur_path,null)); 
				}
				else{
					ToggleButton btn_mode = (ToggleButton) findViewById(R.id.btn_thumbmode); 
					if (!btn_mode.isChecked()){
						openFile(cur_path);
						//showDialog(CLICK_DIALOG_ID);
						
					}
					else {
						if (item.get("item_mark").equals(R.drawable.item_img_unsel)) {
							FileOp.updateFileStatus(file_path, 1);
							item.put("item_mark", R.drawable.item_img_sel);
						}
						else if (item.get("item_mark").equals(R.drawable.item_img_sel)) {
							FileOp.updateFileStatus(file_path, 0);
							item.put("item_mark", R.drawable.item_img_unsel);
						}
						
						((BaseAdapter) ThumbnailView.getAdapter()).notifyDataSetChanged();	
					}
				}
				
			}			
            	
        });
        
        /* lv OnItemLongClickListener */
        ThumbnailView.setOnItemLongClickListener(new OnItemLongClickListener() {
			
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
        
        //button click listener
        /*home button*/
        Button btn_thumbhome = (Button) findViewById(R.id.btn_thumbhome); 
        btn_thumbhome.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {  
    			DeviceScan();
    		}
    		   			       		
        });  
        
        
        /*updir button*/
        Button btn_thumbparent = (Button) findViewById(R.id.btn_thumbparent); 
        btn_thumbparent.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {  
    			if (!cur_path.equals(ROOT_PATH)) {
					File file = new File(cur_path);
					String parent_path = file.getParent();
					
					cur_path = parent_path;
					if(parent_path.equals(ROOT_PATH)){
						cur_path = parent_path;
						DeviceScan();
					}
					else{
						 cur_path = parent_path;
						 ThumbnailView.setAdapter(getThumbnailAdapter(parent_path,null)); 
					
					}
				}
    		}
    		   			       		
        });         
        /*edit button*/
        Button btn_thumbsort = (Button) findViewById(R.id.btn_thumbsort); 
        btn_thumbsort.setOnClickListener(new OnClickListener() {
   		public void onClick(View v) {   
   			showDialog(SORT_DIALOG_ID);
   		}
   		   			       		
       }); 
        /*edit button*/
         Button btn_thumbedit = (Button) findViewById(R.id.btn_thumbedit); 
         btn_thumbedit.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {   
    			showDialog(EDIT_DIALOG_ID);
    		}
    		   			       		
        }); 
        /*switch_button*/
        Button btn_thumbswitch = (Button) findViewById(R.id.btn_thumbswitch); 
        btn_thumbswitch.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			Intent intent = new Intent();
    			intent.setClass(ThumbnailView.this, FileBrower.class);
    			/* Activity */
    			Bundle mybundle = new Bundle();   			
    			mybundle.putString("cur_path", cur_path);
    			intent.putExtras(mybundle);
    			startActivityForResult(intent,request_code);
    			/* Activity */
    			ThumbnailView.this.finish();   	
    		}
    		   			       		
        }); 
        /*close button*/
        Button btn_thumbclose = (Button) findViewById(R.id.btn_thumbclose); 
        btn_thumbclose.setOnClickListener(new OnClickListener() {
   		public void onClick(View v) {  
   			finish();
   		}
   		   			       		
       }); 
        
    }
    
    protected void DeviceScan() {
    	// TODO Auto-generated method stub
    	filelist.clear();
    	String DeviceArray[]={"Internal Memory","SD Card","USB"};   	
    	for(int i=0;i<DeviceArray.length;i++){
    		if(FileOp.deviceExist(DeviceArray[i])){
    			filelist.add(DeviceArray[i]);
    		}
    	} 
    	cur_path = ROOT_PATH;
    	ThumbnailView.setAdapter(getThumbnailAdapter(cur_path,null));   	
	}	

	protected void onActivityResult(int requestCode, int resultCode,Intent data) {
    	// TODO Auto-generated method stub
    		 super.onActivityResult(requestCode, resultCode, data);
    		 switch (resultCode) {
    		 case RESULT_OK:
    			 /* */
    			 Bundle bundle = data.getExtras();
    			 cur_path = bundle.getString("cur_path");
    			 break;
    		 default:
    			 break;
    		 }
}
    private SimpleAdapter getThumbnailAdapter(String path,String sort_type) {
		// TODO Auto-generated method stub
		return new SimpleAdapter(ThumbnailView.this,
        		getThumbData(path,sort_type),
        		R.layout.gridview_item,        		
                new String[]{
        	"item_image",
        	"item_name", 
        	"item_mark"
        	},        		
                new int[]{
        	R.id.itemImage,
        	R.id.itemText, 
        	R.id.itemMark,
		});  		
	}
	private List<? extends Map<String, ?>> getThumbData(String path,String sort_type) {
		// TODO Auto-generated method stub
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		File  file_path = new File(path);		
		if(path.equals(ROOT_PATH)){
			for(int i=0;i<filelist.size();i++){
				String filename = filelist.get(i);
				Map<String, Object> map = new HashMap<String, Object>();
				cur_path = FileOp.convertDeviceName(filelist.get(i));
				map.put("item_image", FileOp.getThumbImage(filename));
				map.put("item_name", filename.toString());
				map.put("item_mark",R.drawable.item_img_nosel);
				map.put("file_path",cur_path);
				list.add(map); 					
			}			
		}
		else{
			
			if(file_path != null && file_path.exists()){
				if(file_path.listFiles() != null){				
					for(File files: file_path.listFiles()){
						Map<String, Object> map = new HashMap<String, Object>();
						String file_abs_path = files.getAbsolutePath();
						if(files.isDirectory()){
							map.put("item_mark",R.drawable.item_img_nosel);
							long file_date = files.lastModified();       	        		  	        		
        	        		map.put("file_date", file_date);	//use for sorting
        	        		
        	        		long file_size = files.length();
        	        		map.put("file_size", file_size);	//use for sorting      	        		          	        
						}
						else{
							if (FileOp.isFileSelected(file_abs_path))
        	        			map.put("item_mark", R.drawable.item_img_sel); 
        	        		else
        	        			map.put("item_mark", R.drawable.item_img_unsel); 
							
							long file_date = files.lastModified();        	        		      	        		
        	        		map.put("file_date", file_date);	//use for sorting      	        		
        	        		long file_size = files.length();
        	        		map.put("file_size", file_size);	//use for sorting
						}						
						
						map.put("item_image", FileOp.getThumbImage(files.getName()));
						map.put("item_name", files.getName());
						map.put("file_path",files.getAbsolutePath());
						list.add(map); 					
					}
				}
			}
		}
		if(!list.isEmpty()){
			if(sort_type != null){
				if(sort_type.equals("by_name")){
					Collections.sort(list, new Comparator<Map<String, Object>>() {
						
						public int compare(Map<String, Object> object1,
								Map<String, Object> object2) {	
							return ((String) object1.get("item_name")).compareTo((String) object2.get("item_name"));					
						}    			
    				});      
					
				}
				else if(sort_type.equals("by_date")){
					Collections.sort(list, new Comparator<Map<String, Object>>() {
						
						public int compare(Map<String, Object> object1,
								Map<String, Object> object2) {	
							return ((Long) object1.get("file_date")).compareTo((Long) object2.get("file_date"));					
						}    			
    				});     
				}
				else if(sort_type.equals("by_size")){
					Collections.sort(list, new Comparator<Map<String, Object>>() {
						
						public int compare(Map<String, Object> object1,
								Map<String, Object> object2) {	
							return ((Long) object1.get("file_size")).compareTo((Long) object2.get("file_size"));					
						}    			
    				}); 
					
				}
			}
		}
		updatePathShow(cur_path);
		return list;
	}
	private void updatePathShow(String device) {
		// TODO Auto-generated method stub		 	
		TextView tv = (TextView) findViewById(R.id.thumb_path); 
		tv.setText(device); 		
	}

	private void openFile(String file_path) {
		// TODO Auto-generated method stub
		File file = new File(file_path);
		Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        String type = "*/*";        
        type = FileOp.CheckMediaType(file);
        intent.setDataAndType(Uri.fromFile(file),type);
        startActivity(intent); 
		
	}
	
	protected Dialog onCreateDialog(int id){
		LayoutInflater inflater = (LayoutInflater) ThumbnailView.this
		.getSystemService(LAYOUT_INFLATER_SERVICE);
		
		switch (id) {
        case SORT_DIALOG_ID:
        	View layout_sort = inflater.inflate(R.layout.sort_dialog_layout,
        		(ViewGroup) findViewById(R.id.layout_root_sort));
        	
            sort_dialog =  new AlertDialog.Builder(ThumbnailView.this)   
        	.setView(layout_sort)
            .create(); 
            return sort_dialog;

        case EDIT_DIALOG_ID:
	    	View layout_edit = inflater.inflate(R.layout.edit_dialog_layout,
	    		(ViewGroup) findViewById(R.id.layout_root_edit));
	    	
	    	edit_dialog = new AlertDialog.Builder(ThumbnailView.this)   
	    	.setView(layout_edit)
	        .create();             
	    	return edit_dialog;
        	
       
        }
        
		return null;    	
    }
	
	
	
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
            				ThumbnailView.setAdapter(getThumbnailAdapter(cur_path, "by_name"));
            			else if (pos == 1)
            				ThumbnailView.setAdapter(getThumbnailAdapter(cur_path, "by_date"));
            			else if (pos == 2)
            				ThumbnailView.setAdapter(getThumbnailAdapter(cur_path, "by_size"));
            		}
            		sort_dialog.dismiss();
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

            edit_lv = (ListView) edit_dialog.getWindow().findViewById(R.id.edit_listview);  
            edit_lv.setAdapter(getDialogListAdapter(EDIT_DIALOG_ID));	
            
            edit_lv.setOnItemClickListener(new OnItemClickListener() {
            	public void onItemClick(AdapterView<?> parent, View view, int pos,
    					long id) {
            		if (!cur_path.equals(ROOT_PATH)) {
            			if (pos == 0) {
            				Log.i(TAG, "DO cut...");
            				FileOp.file_op_todo = FileOpTodo.TODO_CUT;
        					Toast.makeText(ThumbnailView.this,
        							getText(R.string.Toast_msg_cut_todo),
        							Toast.LENGTH_SHORT).show();             				
            			}
            			else if (pos == 1) {
            				Log.i(TAG, "DO copy...");
            				FileOp.file_op_todo = FileOpTodo.TODO_CPY;
        					Toast.makeText(ThumbnailView.this,
        							getText(R.string.Toast_msg_cpy_todo),
        							Toast.LENGTH_SHORT).show();              				
            			}
            			else if (pos == 2) {
            				Log.i(TAG, "DO paste...");            				
        					if (FileOpReturn.SUCCESS == FileOp.pasteSelectedFile()) {
        						db.deleteAllFileMark();
        						ThumbnailView.setAdapter(getThumbnailAdapter(cur_path,null)); 
                				Toast.makeText(ThumbnailView.this,
                						getText(R.string.Toast_msg_paste_ok),
                						Toast.LENGTH_SHORT).show();            						
        					} else {
            					Toast.makeText(ThumbnailView.this,
            							getText(R.string.Toast_msg_paste_nofile),
            							Toast.LENGTH_SHORT).show();            						
        					}
            				FileOp.file_op_todo = FileOpTodo.TODO_NOTHING;
            				            				
            			}
            			else if (pos == 3) {
            				FileOp.file_op_todo = FileOpTodo.TODO_NOTHING;
            				//Log.i(TAG, "DO delete...");   
            				if (FileOpReturn.SUCCESS == FileOp.deleteSelectedFile()) {
            					db.deleteAllFileMark();
            					ThumbnailView.setAdapter(getThumbnailAdapter(cur_path,null));  
                				Toast.makeText(ThumbnailView.this,
                						getText(R.string.Toast_msg_del_ok),
                						Toast.LENGTH_SHORT).show();
            				} else {
            					Toast.makeText(ThumbnailView.this,
            							getText(R.string.Toast_msg_del_nofile),
            							Toast.LENGTH_SHORT).show();
            				}         				          				
            			}
            		}            		
            		edit_dialog.dismiss();
				
    			}            	
            });            
	    	Button edit_btn_close = (Button) edit_dialog.getWindow().findViewById(R.id.edit_btn_close);  
	    	edit_btn_close.setOnClickListener(new OnClickListener() {
	    		public void onClick(View v) {
	    			edit_dialog.dismiss();
	    		}        	
	        }); 
    		break;
    	}
    }

	 /** getDialogListAdapter */
    private SimpleAdapter getDialogListAdapter(int id) {
        return new SimpleAdapter(ThumbnailView.this,
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
    	}
    	return list;
    }    
}


