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

import com.cobo.udslide.MyListener;
import com.cobo.udslide.PullToRefreshLayout;
import com.cobo.udslide.PullableListView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

@SuppressWarnings("deprecation")
@SuppressLint({ "HandlerLeak", "InflateParams" })
public class FragmentHeadline extends Fragment {
	private View view;
	private PullableListView plv_headline;
	private NewsAdapter adapter ;
	private PullToRefreshLayout ptrl;
	private List<News> newsList = new ArrayList<News>();
	
	private Handler handler = new Handler(){
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			newsList = (List<News>) msg.obj;
			adapter = new NewsAdapter(view.getContext(),R.layout.item_news, newsList);
	    	plv_headline.setAdapter(adapter);
			ptrl.setOnRefreshListener(new MyListener(adapter,newsList,1));
		}
		
	};
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		plv_headline = (PullableListView)getActivity().findViewById(R.id.plv_headline);
		ptrl = (PullToRefreshLayout) getActivity().findViewById(R.id.refresh_view);
		
		plv_headline.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("static-access")
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//标记为已读新闻
				SharedPreferences sp = getActivity().getSharedPreferences("current_user",getActivity().MODE_PRIVATE);
				sp = getActivity().getSharedPreferences("read_"+sp.getString("account", null),getActivity().MODE_PRIVATE);
				Editor edit = sp.edit();
				edit.putBoolean(newsList.get(position).getId()+"", true);
				edit.apply();
				//跳转到新闻详情页面
				Intent i = new Intent(getActivity(),NewsDetailsActivity.class);
				i.putExtra("tagNews", newsList.get(position));
				getActivity().startActivity(i);
			}
			
		});
		getNews();
		super.onStart();
	}

	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view =inflater.inflate(R.layout.fragment_headline, null);
		return view;
	}
	
	private void getNews(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/ShowNews");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation", "showNews"));
					params.add(new BasicNameValuePair("kind", "1"));
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
	
	private void parseNews(String newsJosn){
		News news = null;
		JSONArray ja = null;
		List<News> newsList = new ArrayList<News>();
		try {
			ja = new JSONArray(newsJosn);
			JSONObject jo = null;
			for(int i=0;i<ja.length();i++){
				news = new News();
				jo = ja.getJSONObject(i);
				news.setId(jo.getInt("nid"));
				news.setTitle(jo.getString("title"));
				news.setSource(jo.getString("source"));
				news.setImageUrl(jo.getString("imageUrl"));
				news.setPublishTime(jo.getString("time"));
				news.setCommentAmount(jo.getInt("commentAmount"));
				newsList.add(news);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Message msg = new Message();
		msg.obj = newsList;
		handler.sendMessage(msg);
	}
}
	
