package com.cobo.kknews;

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

import com.cobo.udslide.PullToRefreshLayout;
import com.cobo.udslide.PullToRefreshLayout.OnRefreshListener;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class CommentListener implements OnRefreshListener{
	private int nid;
	private Handler handler;
	private CommentAdapter adapter;
	private List<Comment> commentList;
	
	public CommentListener(int nid,CommentAdapter adapter,List<Comment> commentList){
		this.nid = nid;
		this.adapter = adapter;
		this.commentList = commentList;
		Log.i("kk", "listsize:"+commentList.size());
		Log.i("kk", "minCid:"+commentList.get(commentList.size()-1).getCid());
	}
	
	/**
	 * 刷新评论
	 */
	@Override
	public void onRefresh(final PullToRefreshLayout pullToRefreshLayout) {
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				//千万别忘了告诉控件刷新完毕了哦！
				pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
				if(msg.what==0){
					Toast.makeText(pullToRefreshLayout.getContext(), "没有更多最新评论了!", 0).show();
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
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/ShowComment");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation","showLatest"));
					params.add(new BasicNameValuePair("nid",nid+""));
					params.add(new BasicNameValuePair("maxCid",commentList.get(0).getCid()+""));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						if(!response.equals("no data")){
							parseComment(response,true);
							handler.sendEmptyMessage(1);
						}else{
							handler.sendEmptyMessage(0);
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
	
	
	/**
	 * 加载更多评论
	 */
	@Override
	public void onLoadMore(final PullToRefreshLayout pullToRefreshLayout) {
		handler= new Handler(){
			@Override
			public void handleMessage(Message msg){
				// 千万别忘了告诉控件加载完毕了哦！
				pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
				if(msg.what==0){
					Toast.makeText(pullToRefreshLayout.getContext(), "没有更多以前的评论了!", 0).show();
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
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/ShowComment");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation","showAgo"));
					params.add(new BasicNameValuePair("nid",nid+""));
					params.add(new BasicNameValuePair("minCid",commentList.get(commentList.size()-1).getCid()+""));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						if(!response.equals("no data")){
							parseComment(response,false);
							handler.sendEmptyMessage(1);
							Log.i("kk", response);
						}else{
							handler.sendEmptyMessage(0);
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
	
	private void parseComment(String commentJosn,boolean isLatest){
		Comment comment = null;
		JSONArray ja = null;
		try {
			ja = new JSONArray(commentJosn);
			JSONObject jo = null;
			if(isLatest==true){
				for(int i=0;i<ja.length();i++){
					comment = new Comment();
					jo = ja.getJSONObject(i);
					if(jo.getString("iconUrl")!=null){
						comment.setIconUrl(jo.getString("iconUrl"));
					}
					comment.setCid(jo.getInt("cid"));
					comment.setNickname(jo.getString("nickname"));
					comment.setTime(jo.getString("time"));
					comment.setContent(jo.getString("content"));
					commentList.add(0,comment);
				}
			}else{
				for(int i=0;i<ja.length();i++){
					comment = new Comment();
					jo = ja.getJSONObject(i);
					if(jo.getString("iconUrl")!=null){
						comment.setIconUrl(jo.getString("iconUrl"));
					}
					comment.setCid(jo.getInt("cid"));
					comment.setNickname(jo.getString("nickname"));
					comment.setTime(jo.getString("time"));
					comment.setContent(jo.getString("content"));
					commentList.add(commentList.size(),comment);
					Log.i("kk","lastid:"+commentList.get(commentList.size()-1).getCid());
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}


}
