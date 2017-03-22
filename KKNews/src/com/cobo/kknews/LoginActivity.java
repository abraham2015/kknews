package com.cobo.kknews;

import java.io.File;
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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "HandlerLeak", "ShowToast", "SdCardPath" })
@SuppressWarnings("deprecation")
public class LoginActivity extends Activity {
	private ImageView login_iv_back;
	private TextView login_tv_register;
	private EditText login_et_account;
	private EditText login_et_pw;
	private Button login_btn_login;
	private String account;
	private String pw;
	private boolean[] fill = new boolean[2];//2个EditText是否有内容
	private SharedPreferences sp;
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(LoginActivity.this, "登陆成功!", 0).show();
				
				//将当前登陆账号写入SharedPreferences
				sp = getSharedPreferences("current_user",MODE_PRIVATE);
				Editor edit = sp.edit();
				edit.putString("account", account);
				edit.apply();
				
				//将用户信息写入SharedPreferences
				if(!(new File("/data/data/"+getPackageName().toString()+"/shared_prefs/user_"+account+".xml").exists())){
					sp = getSharedPreferences("user_"+account,MODE_PRIVATE);
					edit = sp.edit();
					edit.putString("account", account);
					edit.putString("pw", pw);
					String response = (String)msg.obj;
					edit.putString("nickname", response.substring(1));
					edit.apply();
				}
				
				//检查是否存在用于记录最近阅读的新闻的SharedPreferences，如果不存在则创建
				if(!(new File("/data/data/"+getPackageName().toString()+"/shared_prefs/read_"+account+".xml").exists())){
					sp = getSharedPreferences("read_"+account,MODE_PRIVATE);
					edit = sp.edit();
					edit.putInt("counter", 0);//记录最近阅读的新闻条数
					edit.apply();
				}
				
				//下载用户收藏信息记录到SharedPreferences,这是为了当用户卸载应用后重新登陆或者换手机登陆时，
				//用户收藏的新闻id能在本地与服务器数据库保持同步，不同步的话，收藏按钮背景出错
				downloadCollect();
				
				onBackPressed();
				break;
			case 2:
				Toast.makeText(LoginActivity.this, "你的账号已被封!", 1).show();
				break;
			case 3:
				Toast.makeText(LoginActivity.this, "账号或密码错误!", 0).show();
				break;	
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		login_iv_back = (ImageView) findViewById(R.id.login_iv_back);
		login_et_account = (EditText) findViewById(R.id.login_et_account);
		login_et_pw = (EditText) findViewById(R.id.login_et_pw);
		login_btn_login = (Button) findViewById(R.id.login_btn_login);
		login_tv_register = (TextView) findViewById(R.id.login_tv_register);
		
		login_et_account.addTextChangedListener(new MyTextWatcher(fill,0));
		login_et_pw.addTextChangedListener(new MyTextWatcher(fill,1));
		
		/*//回显账号密码
		sp = getSharedPreferences("current_user",MODE_PRIVATE);
		String currentUser = sp.getString("account", null);
		if(currentUser!=null){
			sp = getSharedPreferences("user_"+currentUser,MODE_PRIVATE);
			login_et_account.setText(sp.getString("account", null));
			login_et_pw.setText(sp.getString("pw", null));
		}*/
		
		login_btn_login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!login_et_account.getText().toString().equals("")&&!login_et_pw.getText().toString().equals("")){
					account = login_et_account.getText().toString();
					pw = login_et_pw.getText().toString();
					login();
				}
				
			}
		});
		
		
		login_tv_register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
				startActivity(i);
			}
		});
		
		
		login_iv_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}
	
	
	/**
	 * 检查每个EditText是否都有内容，是的话改变注册按钮的背景颜色
	 */
	private void checkfill(){
		if((fill[0]==true)&&(fill[1]==true)){
			login_btn_login.setBackground(LoginActivity.this.getResources().getDrawable(R.drawable.corner_red));
			login_btn_login.setTextColor(Color.parseColor("#fff8f8ff"));
		}else{
			login_btn_login.setBackground(LoginActivity.this.getResources().getDrawable(R.drawable.corner));
			login_btn_login.setTextColor(Color.parseColor("#aaaaaa"));
	    }	
	}
	
	
	/**
	 *监听EditText长短变化
	 */
	class MyTextWatcher implements TextWatcher{  
	    private CharSequence temp;
	    private boolean[] fill;
	    int index;
	    public MyTextWatcher( boolean[] fill, int index ){
	    	this.fill = fill;
	    	this.index = index;
	    }
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
	            fill[index] = true;
	            checkfill();
	        } else{
	        	fill[index] = false;
	        	checkfill();
	        }
	          
	    }
	};
	

	/**
	 * 将账号密码发送到服务器，返回回复信息
	 */
	private void login() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/Login");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("account", account));
					params.add(new BasicNameValuePair("pw", pw));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						Log.i("kk", response);
						if(response.equals("fail")){
							///账号或密码错误
							handler.sendEmptyMessage(3);
						}else if(response.equals("ban")){
							//封号
							handler.sendEmptyMessage(2);
						}else{
							//登陆成功,得到服务器返回的信息
							Message msg = new Message();
							msg.obj = response;
							msg.what = 1;
							handler.sendMessage(msg);
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
	 * 下载用户收藏信息记录到SharedPreferences
	 */
	private void downloadCollect(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/Collect");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation", "showNew"));
					params.add(new BasicNameValuePair("account", sp.getString("account", null)));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						Log.i("kk", response);
						if(!response.equals("")){
							SharedPreferences sp1 = getSharedPreferences(
									"collect_"+sp.getString("account", null), LoginActivity.MODE_PRIVATE);
							Editor edit = sp1.edit();
							edit.clear();
							String[] nidArray = response.split(",");
							for(String s :nidArray){
								Log.i("kk", s);
								edit.putBoolean(s, true);
							}
							edit.apply();
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

}
