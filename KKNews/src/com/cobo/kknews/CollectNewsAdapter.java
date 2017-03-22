package com.cobo.kknews;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.cobo.kknews.utils.DownloadSaveImage;
import com.loopj.android.image.SmartImageView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class CollectNewsAdapter extends ArrayAdapter<CollectNews> {
	private Context context;
	private List<CollectNews> collectNewsList;
	
	public CollectNewsAdapter(Context context, int textViewresourceId, List<CollectNews> collectNewsList) {
		super(context, textViewresourceId, collectNewsList);
		this.context = context;
		this.collectNewsList = collectNewsList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CollectNews collectNews = collectNewsList.get(position);
		View view;
		ViewHolder viewHolder;
		if(convertView==null){
			view= LayoutInflater.from(context).inflate(R.layout.item_collect, null);
			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) view.findViewById(R.id.item_collectNews_tv_title);
			viewHolder.smartImage = (SmartImageView) view.findViewById(R.id.item_collectNews_siv_image);
			viewHolder.time = (TextView) view.findViewById(R.id.item_collectNews_tv_collectTime);
			view.setTag(viewHolder);
		}else{
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.title.setText(collectNews.getTitle());
		viewHolder.time.setText(collectNews.getCollectTime());
		if(new File(Environment.getExternalStorageDirectory()+"/KKNews/news_image/"+collectNews.getId()+".jpg").exists()){
			//图片已在本地，则从本地获取
			Uri incoUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/KKNews/news_image/"+collectNews.getId()+".jpg"));
			Bitmap bitmap =null;
			try {
				bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(incoUri));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			viewHolder.smartImage.setImageBitmap(bitmap);
		}else{
			//图片不在本地，则从服务器获取，并下载到本地
			viewHolder.smartImage.setImageUrl("http://www.skycobo.com:8080/KKNews_data/news_image/"+collectNews.getImageUrl());
			try {
				new DownloadSaveImage(new File(Environment.getExternalStorageDirectory()+"/KKNews/news_image/"+collectNews.getId()+".jpg"),
						new URL("http://www.skycobo.com:8080/KKNews_data/news_image/"+collectNews.getImageUrl())).ds();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		return view;
		
	}
	
	class ViewHolder{
		TextView title;
		SmartImageView smartImage;
		TextView time;
	}
}