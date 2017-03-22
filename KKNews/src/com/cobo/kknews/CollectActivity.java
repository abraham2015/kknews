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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint({ "HandlerLeak", "ShowToast" })
@SuppressWarnings("deprecation")
public class CollectActivity extends Activity {
	private ImageView collect_iv_back;
	private ListView collect_lv;
	private List<CollectNews> collectNewsList;
	
	private Handler handler = new Handler(){
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 1){
				collectNewsList = (List<CollectNews>) msg.obj;
				CollectNewsAdapter adapter = new CollectNewsAdapter(CollectActivity.this,R.layout.item_collect, collectNewsList);
				collect_lv.setAdapter(adapter);
			}else{
				Toast.makeText(CollectActivity.this, "你还没有任何收藏呢!", 0).show();
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collect);
		
		collect_iv_back = (ImageView) findViewById(R.id.collect_iv_back);
		collect_lv = (ListView) findViewById(R.id.collect_lv);
		
		//获取个人收藏
		getCollect();
		
		/**
		 * 点击进入新闻详情页面
		 */
		collect_lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent i = new Intent(CollectActivity.this,NewsDetailsActivity.class);
				i.putExtra("tagNews", collectNewsList.get(position));
				startActivity(i);
			}
			
		});
		
		collect_iv_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}
	
	
	/**
	 * 从服务器上获取个人收藏新闻
	 */
	private void getCollect(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				SharedPreferences sp = getSharedPreferences("current_user", MODE_PRIVATE);
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/Collect");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation", "show"));
					params.add(new BasicNameValuePair("account", sp.getString("account", null)));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						Log.i("kk", response);
						if(!response.equals("no data")){
							parseCollect(response);
						}else{
							handler.sendEmptyMessage(2);
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
	 * 解析服务器返回的收藏Josn
	 */
	private void parseCollect(String collectNewsJson){
		CollectNews collectNews = null;
		JSONArray ja = null;
		List<CollectNews> newsList = new ArrayList<CollectNews>();
		try {
			ja = new JSONArray(collectNewsJson);
			JSONObject jo = null;
			for(int i=0;i<ja.length();i++){
				collectNews = new CollectNews();
				jo = ja.getJSONObject(i);
				collectNews.setId(jo.getInt("nid"));
				collectNews.setTitle(jo.getString("title"));
				collectNews.setSource(jo.getString("source"));
				collectNews.setImageUrl(jo.getString("imageUrl"));
				collectNews.setPublishTime(jo.getString("newsTime"));
				collectNews.setCommentAmount(jo.getInt("commentAmount"));
				collectNews.setCollectTime(jo.getString("collectTime"));
				newsList.add(collectNews);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Message msg = new Message();
		msg.what = 1;
		msg.obj = newsList;
		handler.sendMessage(msg);
	}
	
	@Override
	protected void onRestart() {
		onCreate(null);
	}
	
}
