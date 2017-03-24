package com.cobo.kknews;

import java.io.File;
import java.io.FileNotFoundException;
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

import com.cobo.kknews.MyEditText.BackListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
@SuppressLint({ "SimpleDateFormat", "InflateParams" })
public class NewsDetailsActivity extends Activity implements BackListener{
	private ImageView news_details_iv_back;
	private TextView news_details_tv_commentAmount;
	private TextView news_details_tv_title;
	private TextView news_details_tv_source;
	private TextView news_details_tv_time;
	private ImageView news_details_iv_image;
	//private TextView news_details_tv_body;
	private TextView news_details_tv_writeComment;
	private ImageView news_details_iv_collect;
	private MyEditText pop_comment_et_content;
	private TextView pop_comment_tv_wordAmount;
	private Button pop_comment_btn_send;
	private View pop;
	private PopupWindow popw;
	private SharedPreferences sp;
	private SharedPreferences sp1;
	private News tagNews;
	private int commentAmout;
	
	@SuppressLint({ "HandlerLeak", "ShowToast" })
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 1:
				Toast.makeText(NewsDetailsActivity.this, "发表成功!", 0).show();
				break;
			case 2:
				Toast.makeText(NewsDetailsActivity.this, "发表失败!", 0).show();
				break;
			case 3:
				Toast.makeText(NewsDetailsActivity.this, "收藏成功!", 0).show();
				break;
			case 5:
				Toast.makeText(NewsDetailsActivity.this, "取消收藏!", 0).show();
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_details);
		
		//获取从上一页面得到的新闻对象
		Intent i =getIntent();
		tagNews = (News) i.getSerializableExtra("tagNews");
		commentAmout = tagNews.getCommentAmount();
		
		news_details_iv_back = (ImageView) findViewById(R.id.news_details_iv_back);
		news_details_tv_commentAmount = (TextView) findViewById(R.id.news_details_tv_commentAmount);
		news_details_tv_title = (TextView) findViewById(R.id. news_details_tv_title);
		news_details_tv_source = (TextView) findViewById(R.id. news_details_tv_source);
		news_details_tv_time = (TextView) findViewById(R.id. news_details_tv_time);
		news_details_iv_image = (ImageView) findViewById(R.id. news_details_iv_image);
		//news_details_tv_body = (TextView) findViewById(R.id. news_details_tv_body);
		news_details_tv_writeComment = (TextView) findViewById(R.id.news_details_tv_writeComment);
		news_details_iv_collect = (ImageView) findViewById(R.id.news_details_iv_collect);
		pop = getLayoutInflater().inflate(R.layout.pop_comment, null);
		popw = new PopupWindow(pop,LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT,true);
		popw.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		pop_comment_et_content = (MyEditText) pop.findViewById(R.id.pop_comment_et_content);
		pop_comment_tv_wordAmount = (TextView) pop.findViewById(R.id.pop_comment_tv_wordAmount);
		pop_comment_btn_send = (Button) pop.findViewById(R.id.pop_comment_btn_send);
		
		//给控件设置数据
		news_details_tv_commentAmount.setText(tagNews.getCommentAmount()+"跟帖");
		news_details_tv_title.setText(tagNews.getTitle());
		news_details_tv_source.setText(tagNews.getSource());
		news_details_tv_time.setText(tagNews.getPublishTime());
		//从本地给ImageView设置图片
		Uri incoUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/KKNews/news_image/"+tagNews.getId()+".jpg"));
		Bitmap bitmap =null;
		try {
			bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(incoUri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		news_details_iv_image.setImageBitmap(bitmap);
		//给收藏图标设置背景
		sp1 = getSharedPreferences("current_user", MODE_PRIVATE);
		sp1 = getSharedPreferences("collect_"+sp1.getString("account", null), MODE_PRIVATE);
		if(sp1.getBoolean(tagNews.getId()+"", false)==false){
			//未收藏，则显示白色背景
			news_details_iv_collect.setImageDrawable(getResources().getDrawable(R.drawable.collect));
		}else{
			//已收藏，则显示红色背景
			news_details_iv_collect.setImageDrawable(getResources().getDrawable(R.drawable.collect_red));
		}
		
		//绑定字长变化监听器
		pop_comment_et_content.addTextChangedListener(mTextWatcher);
		
		//退出时键盘和输入框都退出
		pop_comment_et_content.setBackListener(this);
		
		
		/**
		 * 点击携带新闻对象进入评论页面
		 */
		news_details_tv_commentAmount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(NewsDetailsActivity.this,CommentActivity.class);
				i.putExtra("tagNews",tagNews);
				startActivity(i);
			}
		});
		
		
		/**
		 * 点击发表评论
		 */
		pop_comment_btn_send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!pop_comment_et_content.getText().toString().equals("")){
					sendComment();
					pop_comment_et_content.setText("");
					back(pop_comment_et_content);
					commentAmout++;
					news_details_tv_commentAmount.setText((commentAmout)+"跟帖");
				}
			}
		});
		
		
		/**
		 * 点击，先判断是否有用户登陆，有则弹出评论输入框，无则跳到登陆页面
		 */
		news_details_tv_writeComment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sp = getSharedPreferences("current_user", MODE_PRIVATE);
				if(sp.getString("account", null)!=null){
					//设置输入框弹出位置
					popw.showAtLocation(v, 0, 0,NewsDetailsActivity.this.getWindowManager().getDefaultDisplay().getHeight());
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
					Intent i = new Intent(NewsDetailsActivity.this,LoginActivity.class);
					startActivity(i);
				}
			}
		});
		
		
		/**
		 * 点击收藏或取消收藏并改变ImageView的颜色
		 */
		news_details_iv_collect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sp = getSharedPreferences("current_user", MODE_PRIVATE);
				if(sp.getString("account", null)!=null){
					Editor edit = sp1.edit();
					if(sp1.getBoolean(tagNews.getId()+"", false)==false){
						uploadCollect();//上传收藏信息到服务器
						edit.putBoolean(tagNews.getId()+"",true);
						edit.apply();
						news_details_iv_collect.setImageDrawable(getResources().getDrawable(R.drawable.collect_red));
					}else{
						removeCollect();//从服务器上删除某条收藏
						edit.remove(tagNews.getId()+"");
						edit.apply();
						news_details_iv_collect.setImageDrawable(getResources().getDrawable(R.drawable.collect));
					}
				}else{
					Intent i = new Intent(NewsDetailsActivity.this,LoginActivity.class);
					startActivity(i);
				}
				
			}
		});
		
		
		news_details_iv_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
	}
	
	
	/**
	 * 将评论数据上传到服务器
	 */
	private void sendComment(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/UploadComment");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("nid", tagNews.getId()+""));
					params.add(new BasicNameValuePair("account", sp.getString("account", null)));
					params.add(new BasicNameValuePair("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
					params.add(new BasicNameValuePair("content",pop_comment_et_content.getText().toString()));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						Log.i("kk", response);
						if(response.equals("success")){
							handler.sendEmptyMessage(1);
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
	 * 上传收藏信息到服务器
	 */
	private void uploadCollect(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/Collect");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation", "upload"));
					params.add(new BasicNameValuePair("nid", tagNews.getId()+""));
					params.add(new BasicNameValuePair("account", sp.getString("account", null)));
					params.add(new BasicNameValuePair("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						Log.i("kk", response);
						if(response.equals("success")){
							handler.sendEmptyMessage(3);
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
	 * 从服务器上删除某条收藏
	 */
	private void removeCollect(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/Collect");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation", "remove"));
					params.add(new BasicNameValuePair("nid", tagNews.getId()+""));
					params.add(new BasicNameValuePair("account", sp.getString("account", null)));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						Log.i("kk", response);
						if(response.equals("success")){
							handler.sendEmptyMessage(5);
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
	        	pop_comment_btn_send.setBackground(NewsDetailsActivity.this.getResources().getDrawable(R.drawable.corner_red));
	        	pop_comment_btn_send.setTextColor(Color.parseColor("#fff8f8ff"));
	         	pop_comment_tv_wordAmount.setText("还可输入"+(50-pop_comment_et_content.getText().toString().length())+"个字");
	        } else{
	        	pop_comment_btn_send.setBackground(NewsDetailsActivity.this.getResources().getDrawable(R.drawable.corner));
	        	pop_comment_btn_send.setTextColor(Color.parseColor("#aaaaaa"));
	        	pop_comment_tv_wordAmount.setText("还可输入50个字");
	        }
	          
	    }
	};
	
	
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
