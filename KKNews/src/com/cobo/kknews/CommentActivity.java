package com.cobo.kknews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

import com.cobo.kknews.MyEditText.BackListener;
import com.cobo.udslide.NewsListener;
import com.cobo.udslide.PullToRefreshLayout;
import com.cobo.udslide.PullableListView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
@SuppressWarnings("deprecation")
public class CommentActivity extends Activity implements BackListener{
	private ImageView comment_iv_back;
	private ImageView comment_iv_writeComment;
	private PullToRefreshLayout comment_refresh_view;
	private PullableListView comment_plv;
	private View pop;
	private PopupWindow popw;
	private MyEditText pop_comment_et_content;
	private TextView pop_comment_tv_wordAmount;
	private Button pop_comment_btn_send;
	private News tagNews;
	private List<Comment> commentList = new ArrayList<Comment>();
	private CommentAdapter adapter;
	private Comment newComment;
	private String publishTime;
	
	@SuppressLint({ "HandlerLeak", "ShowToast" })
	private Handler handler = new Handler(){
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			if(msg.what ==1){
				commentList = (List<Comment>) msg.obj;
				Log.i("kk", "minCid:"+commentList.size());
				adapter = new CommentAdapter(CommentActivity.this, R.layout.item_comment, commentList);
				comment_plv.setAdapter(adapter);
				comment_refresh_view.setOnRefreshListener(new CommentListener(tagNews.getId(),adapter,commentList));
			}else if(msg.what ==2){
				Toast.makeText(CommentActivity.this,"发表成功!", 0).show();
			}else{
				Toast.makeText(CommentActivity.this,"暂无评论，快抢沙发!", 1).show();
			}
		}
	};
	
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment);
		
		
		//获取从上一页面得到的新闻对象
		Intent i =getIntent();
		tagNews = (News) i.getSerializableExtra("tagNews");

		comment_iv_back = (ImageView) findViewById(R.id.comment_iv_back);
		comment_iv_writeComment = (ImageView) findViewById(R.id.comment_iv_writeComment);
		comment_refresh_view = (PullToRefreshLayout)findViewById(R.id.comment_refresh_view);
		comment_plv = (PullableListView)findViewById(R.id.comment_plv);
		pop = getLayoutInflater().inflate(R.layout.pop_comment, null);
		popw = new PopupWindow(pop,LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT,true);
		popw.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		pop_comment_et_content = (MyEditText) pop.findViewById(R.id.pop_comment_et_content);
		pop_comment_tv_wordAmount = (TextView) pop.findViewById(R.id.pop_comment_tv_wordAmount);
		pop_comment_btn_send = (Button) pop.findViewById(R.id.pop_comment_btn_send);
		
		adapter = new CommentAdapter(CommentActivity.this, R.layout.item_comment, commentList);
		comment_plv.setAdapter(adapter);
		
		//获取评论
		getComment();
		
		//绑定字长变化监听器
		pop_comment_et_content.addTextChangedListener(mTextWatcher);
		
		//退出时键盘和输入框都退出
		pop_comment_et_content.setBackListener(this);
		
		
		/**
		 * 点击，先判断是否有用户登陆，有则弹出评论输入框，无则跳到登陆页面
		 */
		comment_iv_writeComment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("current_user", MODE_PRIVATE);
				if(sp.getString("account", null)!=null){
					//设置输入框弹出位置
					popw.showAtLocation(v, 0, 0,CommentActivity.this.getWindowManager().getDefaultDisplay().getHeight());
					Timer timer = new Timer();
					timer.schedule(new TimerTask(){
						@Override
						public void run() {
							//打开软键盘
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
							imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS); 
						}
					}, 200);
				}else{
					Intent i = new Intent(CommentActivity.this,LoginActivity.class);
					startActivity(i);
				}
			}
		});
		
		
		/**
		 * 点击发表评论
		 */
		pop_comment_btn_send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!pop_comment_et_content.getText().toString().equals("")){
					SharedPreferences sp = getSharedPreferences("current_user", MODE_PRIVATE);
					sp = getSharedPreferences("user_"+sp.getString("account", null), MODE_PRIVATE);
					publishTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
					newComment = new Comment();
					newComment.setNid(tagNews.getId());
					newComment.setAccount(sp.getString("account", null));
					newComment.setNickname(sp.getString("nickname", null));
					newComment.setTime(publishTime.substring(5,16));
					newComment.setIconUrl(sp.getString("account", null)+".jpg");
					newComment.setContent(pop_comment_et_content.getText().toString());
					commentList.add(0,newComment);
					sendComment();
					adapter.notifyDataSetChanged();
					pop_comment_et_content.setText("");
					back(pop_comment_et_content);
					
				}
			}
		});
		
		comment_iv_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
	}
	

	/**
	 * 从服务器获取特定新闻id的评论
	 */
	private void getComment(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/ShowComment");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation","show"));
					params.add(new BasicNameValuePair("nid", tagNews.getId()+""));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						Log.i("kk", response);
						if(!response.equals("no data")){
							parseComment(response);
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
	 * 解析服务器返回的评论json数据
	 * @param commentJson 评论json
	 */
	private void parseComment(String commentJson){
		Comment comment = null;
		JSONArray ja = null;
		List<Comment> commentList = new ArrayList<Comment>();
		try {
			ja = new JSONArray(commentJson);
			JSONObject jo = null;
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
				commentList.add(comment);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Message msg = new Message();
		msg.what = 1;
		msg.obj = commentList;
		handler.sendMessage(msg);
	}
	
	
	/**
	 * 字长变化监听器
	 */
	TextWatcher mTextWatcher = new TextWatcher() {  
	    private CharSequence temp;  
	    @Override  
	    public void onTextChanged(CharSequence s, int start, int before, int count) {  
	       temp = s;  
	    }  
	          
	    @Override  
	    public void beforeTextChanged(CharSequence s, int start, int count,  
	          int after) {  
	     
	    }  
	          
	    @Override  
	    public void afterTextChanged(Editable s) {  
	        if (temp.length()>0) {  
	        	pop_comment_btn_send.setBackground(CommentActivity.this.getResources().getDrawable(R.drawable.corner_red));
	        	pop_comment_btn_send.setTextColor(Color.parseColor("#fff8f8ff"));
	         	pop_comment_tv_wordAmount.setText("还可输入"+(50-pop_comment_et_content.getText().toString().length())+"个字");
	        } else{
	        	pop_comment_btn_send.setBackground(CommentActivity.this.getResources().getDrawable(R.drawable.corner));
	        	pop_comment_btn_send.setTextColor(Color.parseColor("#aaaaaa"));
	        	pop_comment_tv_wordAmount.setText("还可输入50个字");
	        }
	          
	    }
	};
	
	
	/**
	 * 将评论数据上传到服务器
	 */
	private void sendComment(){
		new Thread(new Runnable() {
			@SuppressLint("SimpleDateFormat")
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/UploadComment");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("nid", tagNews.getId()+""));
					params.add(new BasicNameValuePair("account", newComment.getAccount()));
					params.add(new BasicNameValuePair("time",publishTime));
					params.add(new BasicNameValuePair("content",newComment.getContent()));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						Log.i("kk", response);
						if(response.equals("success")){
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
	 * 退出输入框
	 */
	@Override
	public void back(TextView textView) {
		if(popw!=null&&popw.isShowing()){
			popw.dismiss();
		}
	}

}
