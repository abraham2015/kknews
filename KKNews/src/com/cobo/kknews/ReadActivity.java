package com.cobo.kknews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint({ "HandlerLeak", "ShowToast" })
@SuppressWarnings("deprecation")
public class ReadActivity extends Activity {
	private ImageView read_iv_back;
	private ListView read_lv;
	private List<News> newsList;
	private SharedPreferences sp;
	
	private Handler handler = new Handler(){
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			newsList = (List<News>) msg.obj;
			ReadNewsAdapter adapter = new ReadNewsAdapter(ReadActivity.this,R.layout.item_news, newsList);
			read_lv.setAdapter(adapter);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read);
		
		read_iv_back = (ImageView) findViewById(R.id.read_iv_back);
		read_lv = (ListView) findViewById(R.id.read_lv);
		
		sp = getSharedPreferences("current_user",MODE_PRIVATE);
		sp = getSharedPreferences("read_"+sp.getString("account", null),MODE_PRIVATE);
		
		//展示已读新闻
		if(sp.getInt("counter", 0)!=0){
			showReadNews();
		}else{
			Toast.makeText(ReadActivity.this,"你还没阅读呢!", 0).show();
		}
		
		
		//跳转到新闻详情页面
		read_lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent i = new Intent(ReadActivity.this,NewsDetailsActivity.class);
				i.putExtra("tagNews", newsList.get(position));
				startActivity(i);
			}
		});
		
		read_iv_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}
	
	
	/**
	 * 从服务器下载用户最近读的新闻
	 */
	private void showReadNews(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				Map<String, ?> all = sp.getAll();
				Set<String> set = all.keySet();
				set.remove("counter");
				StringBuffer nidSB = new StringBuffer();
				for(String s:set){
					nidSB.append(s+",");
				}
				String nidStr = nidSB.toString();
				nidStr = nidStr.substring(0, nidStr.length()-1);
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/ShowNews");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation", "showReadNews"));
					params.add(new BasicNameValuePair("nidStr", nidStr));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						if(!response.equals("no data")){
							parseNews(response);
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
	 * 解析服务器返回的新闻Json
	 * @param newsJosn
	 */
	private void parseNews(String newsJosn){
		News news = null;
		JSONArray ja = null;
		News[] newsArray = null;
		List<News> newsList = new ArrayList<News>();
		try {
			ja = new JSONArray(newsJosn);
			JSONObject jo = null;
			newsArray = new News[ja.length()];
			for(int i=0;i<ja.length();i++){
				news = new News();
				jo = ja.getJSONObject(i);
				news.setId(jo.getInt("nid"));
				news.setTitle(jo.getString("title"));
				news.setSource(jo.getString("source"));
				news.setImageUrl(jo.getString("imageUrl"));
				news.setPublishTime(jo.getString("time"));
				news.setCommentAmount(jo.getInt("commentAmount"));
				newsArray[sp.getInt(jo.getInt("nid")+"", 0)-1]=news;
			}
			for(News n :newsArray){
				newsList.add(n);
			}
			Collections.reverse(newsList);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Message msg = new Message();
		msg.obj = newsList;
		handler.sendMessage(msg);
	}
}
