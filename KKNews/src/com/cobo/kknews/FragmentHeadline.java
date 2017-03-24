package com.cobo.kknews;

import java.util.ArrayList;
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

import com.cobo.udslide.NewsListener;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

@SuppressWarnings("deprecation")
@SuppressLint({ "HandlerLeak", "InflateParams" })
public class FragmentHeadline extends Fragment {
	private View view;
	private PullableListView plv_headline;
	private NewsAdapter adapter ;
	private PullToRefreshLayout ptrl;
	SharedPreferences sp;
	Editor edit;
	private List<News> newsList = new ArrayList<News>();
	
	private Handler handler = new Handler(){
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			newsList = (List<News>) msg.obj;
			adapter = new NewsAdapter(view.getContext(),R.layout.item_news, newsList);
	    	plv_headline.setAdapter(adapter);
			ptrl.setOnRefreshListener(new NewsListener(adapter,newsList,1));
			if(sp.getInt("position", -1)!=-1){
				plv_headline.setSelectionFromTop(sp.getInt("position", 0), sp.getInt("scrolledY", 0));
			}
		}
		
	};
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		plv_headline = (PullableListView)getActivity().findViewById(R.id.plv_headline);
		ptrl = (PullToRefreshLayout) getActivity().findViewById(R.id.refresh_view);
		
		sp = getActivity().getSharedPreferences("position_1", getActivity().MODE_PRIVATE);
		edit = sp.edit();
		
		plv_headline.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("static-access")
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//如果未标记为已读，则标记为已读新闻
				SharedPreferences sp = getActivity().getSharedPreferences("current_user",getActivity().MODE_PRIVATE);
				sp = getActivity().getSharedPreferences("read_"+sp.getString("account", null),getActivity().MODE_PRIVATE);
				int counter = sp.getInt("counter", 0);
				Editor edit = sp.edit();
				if(counter<20){
					if(sp.getInt(newsList.get(position).getId()+"", 0)==0){
						edit.putInt(newsList.get(position).getId()+"", counter+1);
						edit.putInt("counter", counter+1);
						edit.apply();
					}
				}else{
					//当最近阅读记录有20条后，移除最早阅读的那条
					Map<String, ?> all = sp.getAll();
					all.remove("counter");
					Set<String> keySet = all.keySet();
					for(String s:keySet){
						if(sp.getInt(s, 0)==1){
							edit.remove(s);
							keySet.remove(s);
							break;
						}
					}
					int value = 0;
					for(String s:keySet){
						value = sp.getInt(s, 0);
						edit.putInt(s, value-1);
					}
					edit.putInt(newsList.get(position).getId()+"", 20);
					edit.apply();
				}
				//跳转到新闻详情页面
				Intent i = new Intent(getActivity(),NewsDetailsActivity.class);
				i.putExtra("tagNews", newsList.get(position));
				getActivity().startActivity(i);
			}
			
		});
		
		plv_headline.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//保存当前滚动到的位置
			    if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			    	int position = plv_headline.getFirstVisiblePosition();//获取在总的列表条数中的索引编号
			    	View firstVisibleItem = plv_headline.getChildAt(0);//获取在可视的item中的索引编号
			    	int scrolledY = firstVisibleItem.getTop();//获取第一个列表项相对于屏幕顶部的位置
			    	Log.i("kk", "position:"+position+" scrolledY:"+scrolledY);
			    	edit.putInt("position", position);
			    	edit.putInt("scrolledY", scrolledY);
			    	edit.apply();
			    }
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){}
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
	
