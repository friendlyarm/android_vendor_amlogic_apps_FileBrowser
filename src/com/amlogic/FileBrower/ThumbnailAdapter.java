package com.amlogic.FileBrower;


import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/* android.widget.BaseAdapter */
public class ThumbnailAdapter extends BaseAdapter{
  
  private LayoutInflater mInflater;
  //private Bitmap mIcon_folder;
  //private Bitmap mIcon_file;
  //private Bitmap mIcon_audio;
  //private Bitmap mIcon_video;
  //private Bitmap mIcon_apk;
 // private Bitmap mIcon_image;
  private List<String> items;
  private String file_path;
  Context c;
  /* MyAdapter  */  
  public ThumbnailAdapter(Context context,List<String> it){
   
    mInflater = LayoutInflater.from(context);
    items = it;  
    c = context;
    //mIcon_folder = BitmapFactory.decodeResource(context.getResources(),R.drawable.item_preview_dir);      //
    //mIcon_file = BitmapFactory.decodeResource(context.getResources(),R.drawable.txt_default);          //
  //  mIcon_image = BitmapFactory.decodeResource(context.getResources(),R.drawable.item_preview_photo);        //
    //mIcon_audio = BitmapFactory.decodeResource(context.getResources(),R.drawable.item_preview_music);        //
    //mIcon_video = BitmapFactory.decodeResource(context.getResources(),R.drawable.item_preview_video);        //
   // mIcon_apk = BitmapFactory.decodeResource(context.getResources(),R.drawable.txt_default);            //apk
  }  
  public int getCount(){
    return items.size();
  }
  public Object getItem(int position){
    return items.get(position);
  }
  public long getItemId(int position){
    return position;
  }
  public View getView(int position,View convertView,ViewGroup par){
    Bitmap bitMap = null;
    ViewHolder holder = null;
      if(convertView == null){
        /* list_itemsLayout */
        convertView = mInflater.inflate(R.layout.gridview_item, null);
        /* holdertexticon */
        holder = new ViewHolder();
        holder.f_title = ((TextView) convertView.findViewById(R.id.itemText));       
        holder.f_icon = ((ImageView) convertView.findViewById(R.id.itemImage));
        holder.f_mark = ((ImageView) convertView.findViewById(R.id.itemMark)) ;
        convertView.setTag(holder);
      }else{
        holder = (ViewHolder) convertView.getTag();
      }      
      /* icon */      
      file_path = items.get(position).toString();
      File f = new File(file_path);
      String file_name = f.getName();
      holder.f_title.setText(file_name);
      if(FileOp.isFileSelected(file_path,"thumbnail")){
    	  
    	  holder.f_mark.setImageResource(R.drawable.item_img_sel);
      }
      else{
    	  holder.f_mark.setImageResource(R.drawable.item_img_nosel);
      }
      
      if(f.isDirectory()){
    	  holder.f_icon.setImageResource(R.drawable.item_preview_dir);
        //holder.f_icon.setImageBitmap(mIcon_folder);      
      }else{    
    	 if(FileOp.isVideo(file_name)){
    		 //holder.f_icon.setImageBitmap(mIcon_video);
    		 holder.f_icon.setImageResource(R.drawable.item_preview_video);
    	 }
    	 else if(FileOp.isMusic(file_name)){
    		 holder.f_icon.setImageResource(R.drawable.item_preview_music);
    		 //holder.f_icon.setImageBitmap(mIcon_audio);   		 
    	 }
    	 else if(FileOp.isPhoto(file_name)){
    		 bitMap = FileOp.fitSizePic(f);
    		 if(bitMap ==null){
    			holder.f_icon.setImageResource(R.drawable.item_preview_photo);  			 
    			holder.f_icon.setImageBitmap(bitMap);
    		 }
    		 else{
    			 holder.f_icon.setImageBitmap(bitMap);
    		 }
    		 //holder.f_icon.setImageResource(R.drawable.item_preview_photo);
    		 
    	 }
    	 else{
			 int icon = FileOp.getThumbDeviceIcon(c,file_name);   		 
    		  holder.f_icon.setImageResource(icon);
    	 }       
      }
    return convertView;
  }
  /**
   * class ViewHolder 
   * */
  private class ViewHolder{
    TextView f_title;   
    ImageView f_icon;
    ImageView f_mark;
  }
}