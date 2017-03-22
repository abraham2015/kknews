package com.cobo.udslide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cobo.kknews.News;
import com.cobo.kknews.NewsAdapter;
import com.cobo.udslide.PullToRefreshLayout.OnRefreshListener;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


@SuppressLint({ "HandlerLeak", "ShowToast" })
@SuppressWarnings("deprecation")
public class MyListener implements OnRefreshListener
{	
	private int kind;
	private Handler handler1;
	private Handler handler2;
	private NewsAdapter adapter;
	private List<News> newsList = new ArrayList<News>();
	public MyListener(NewsAdapter adapter,List<News> newsList,int kind) {
		this.kind = kind;
		this.adapter = adapter;
		this.newsList = newsList;
	}
	@Override
	public void onRefresh(final PullToRefreshLayout pullToRefreshLayout)
	{
		handler1 = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				//千万别忘了告诉控件刷新完毕了哦！
				pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
				if(msg.what==0){
					Toast.makeText(pullToRefreshLayout.getContext(), "没有更多最新新闻了!", 0).show();
					Log.i("kk", "0");
				}else{
					adapter.notifyDataSetChanged();
					Log.i("kk", "1");
				}
			}
		};
		new Thread(new Runnable(){
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/ShowNews");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation","showLatestNews"));
					params.add(new BasicNameValuePair("kind",kind+""));
					params.add(new BasicNameValuePair("maxID",newsList.get(0).getId()+""));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						if(!response.equals("no data")){
							parseNews(response,true);
							handler1.sendEmptyMessage(1);
							Log.i("kk", "1data");
						}else{
							handler1.sendEmptyMessage(0);
							Log.i("kk", "0nodata");
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					if(httpClient!=null){
						httpClient.getConnectionManager().shutdown();
					}
				}
				
			}}
		).start();
		
		
	}

	@Override
	public void onLoadMore(final PullToRefreshLayout pullToRefreshLayout)
	{
		// 加载操作
		handler2 = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				// 千万别忘了告诉控件加载完毕了哦！
				pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
				if(msg.what==0){
					Toast.makeText(pullToRefreshLayout.getContext(), "没有更多以前新闻了!", 0).show();
				}else{
					adapter.notifyDataSetChanged();
				}
			}
		};
		new Thread(new Runnable(){
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/ShowNews");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation","showAgoNews"));
					params.add(new BasicNameValuePair("kind",kind+""));
					params.add(new BasicNameValuePair("minID",newsList.get(newsList.size()-1).getId()+""));
					Log.i("kk","minID:"+newsList.get(newsList.size()-1).getId());
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						if(!response.equals("no data")){
							parseNews(response,false);
							handler2.sendEmptyMessage(1);
							Log.i("kk", response);
							
						}else{
							handler2.sendEmptyMessage(0);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					if(httpClient!=null){
						httpClient.getConnectionManager().shutdown();
					}
				}
				
			}}
		).start();
		
		
	}
	
	private void parseNews(String newsJosn,boolean isLatest){
		News news = null;
		JSONArray ja = null;
		try {
			ja = new JSONArray(newsJosn);
			JSONObject jo = null;
			if(isLatest==true){
				for(int i=0;i<ja.length();i++){
					news = new News();
					jo = ja.getJSONObject(i);
					news.setId(jo.getInt("nid"));
					news.setTitle(jo.getString("title"));
					news.setSource(jo.getString("source"));
					news.setImageUrl(jo.getString("imageUrl"));
					news.setPublishTime(jo.getString("time"));
					news.setCommentAmount(jo.getInt("commentAmount"));
					newsList.add(0,news);
				}
			}else{
				for(int i=0;i<ja.length();i++){
					news = new News();
					jo = ja.getJSONObject(i);
					news.setId(jo.getInt("nid"));
					news.setTitle(jo.getString("title"));
					news.setSource(jo.getString("source"));
					news.setImageUrl(jo.getString("imageUrl"));
					news.setPublishTime(jo.getString("time"));
					news.setCommentAmount(jo.getInt("commentAmount"));
					newsList.add(newsList.size(),news);
					Log.i("kk","lastid:"+newsList.get(newsList.size()-1).getId());
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

}
