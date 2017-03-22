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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class NewsAdapter extends ArrayAdapter<News> {
	private Context context;
	private List<News> newsList;
	private SharedPreferences sp;
	
	@SuppressWarnings("static-access")
	public NewsAdapter(Context context, int textViewresourceId, List<News> newsList) {
		super(context, textViewresourceId, newsList);
		this.context = context;
		this.newsList = newsList;
		sp = context.getSharedPreferences("current_user",context.MODE_PRIVATE);
		sp = context.getSharedPreferences("read_"+sp.getString("account", null),context.MODE_PRIVATE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		News news = newsList.get(position);
		View view;
		ViewHolder viewHolder;
		if(convertView==null){
			view= LayoutInflater.from(context).inflate(R.layout.item_news, null);
			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) view.findViewById(R.id.item_news_tv_title);
			viewHolder.smartImage = (SmartImageView) view.findViewById(R.id.item_news_siv_image);
			viewHolder.source = (TextView) view.findViewById(R.id.item_news_tv_source);
			viewHolder.commentAmount = (TextView) view.findViewById(R.id.item_news_tv_comment);
			view.setTag(viewHolder);
		}else{
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.title.setText(news.getTitle());
		viewHolder.source.setText(news.getSource());
		viewHolder.commentAmount.setText(news.getCommentAmount()+"跟帖");
		if(sp.getBoolean(news.getId()+"", false)==true){
			viewHolder.title.setTextColor(Color.parseColor("#aaaaaa"));
			viewHolder.source.setTextColor(Color.parseColor("#aaaaaa"));
			viewHolder.commentAmount.setTextColor(Color.parseColor("#aaaaaa"));
		}
		if(new File(Environment.getExternalStorageDirectory()+"/KKNews/news_image/"+news.getId()+".jpg").exists()){
			//图片已在本地，则从本地获取
			Uri incoUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/KKNews/news_image/"+news.getId()+".jpg"));
			Bitmap bitmap =null;
			try {
				bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(incoUri));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			viewHolder.smartImage.setImageBitmap(bitmap);
		}else{
			//图片不在本地，则从服务器获取，并下载到本地
			viewHolder.smartImage.setImageUrl("http://www.skycobo.com:8080/KKNews_data/news_image/"+news.getImageUrl());
			try {
				new DownloadSaveImage(new File(Environment.getExternalStorageDirectory()+"/KKNews/news_image/"+news.getId()+".jpg"),
						new URL("http://www.skycobo.com:8080/KKNews_data/news_image/"+news.getImageUrl())).ds();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		return view;
		
	}
	
	class ViewHolder{
		TextView title;
		SmartImageView smartImage;
		TextView source;
		TextView commentAmount;
	}
}
