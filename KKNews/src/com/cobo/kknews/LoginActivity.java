package com.cobo.kknews;

import java.io.File;
import java.util.ArrayList;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "HandlerLeak", "ShowToast", "SdCardPath" })
@SuppressWarnings("deprecation")
public class LoginActivity extends Activity implements BackListener{
	private ImageView login_iv_back;
	private TextView login_tv_register;
	private TextView login_tv_forgetPW;
	private EditText login_et_account;
	private EditText login_et_pw;
	private Button login_btn_login;
	private String account;
	private String pw;
	private String newPW;
	private View pop_pw;
	private PopupWindow popw;
	private MyEditText pop_pw_et_account;
	private MyEditText pop_pw_et_oldPW;
	private MyEditText pop_pw_et_newPW;
	private Button pop_pw_btn_comfirm;
	private Button pop_pw_btn_cancel;
	private boolean[] fill = new boolean[2];//2个EditText是否有内容
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(LoginActivity.this, "登陆成功!", 0).show();
				
				//将当前登陆账号写入SharedPreferences
				SharedPreferences sp = getSharedPreferences("current_user",MODE_PRIVATE);
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
				//检查是否存在用于记录收藏的新闻的SharedPreferences，如果不存在则下载
				if(!(new File("/data/data/"+getPackageName().toString()+"/shared_prefs/collect_"+account+".xml").exists())){
					downloadCollect();
				}
				onBackPressed();
				break;
			case 2:
				Toast.makeText(LoginActivity.this, "你的账号已被封!", 1).show();
				break;
			case 3:
				Toast.makeText(LoginActivity.this, "账号或密码错误!", 0).show();
				break;	
			case 5:
				popw.dismiss();
				Toast.makeText(LoginActivity.this, "修改成功!", 0).show();
				pop_pw_et_newPW.setText("");
				pop_pw_et_oldPW.setText("");
				pop_pw_et_account.setText("");
				break;	
			case 6:
				Toast.makeText(LoginActivity.this, "账号或密码错误!", 0).show();
				break;	
			case 7:
				SharedPreferences sp1 = getSharedPreferences("current_user",MODE_PRIVATE);
				sp1 = getSharedPreferences(
						"collect_"+sp1.getString("account", null), LoginActivity.MODE_PRIVATE);
				Editor edit1 = sp1.edit();
				edit1.clear();
				String[] nidArray = (String[]) msg.obj;
				for(String s :nidArray){
					Log.i("kk", s);
					edit1.putBoolean(s, true);
				}
				edit1.apply();
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
		login_tv_forgetPW = (TextView) findViewById(R.id.login_tv_forgetPW);
		pop_pw = getLayoutInflater().inflate(R.layout.pop_pw, null);
		pop_pw_et_account = (MyEditText) pop_pw.findViewById(R.id.pop_pw_et_account);
		pop_pw_et_oldPW = (MyEditText) pop_pw.findViewById(R.id.pop_pw_et_oldPW);
		pop_pw_et_newPW = (MyEditText) pop_pw.findViewById(R.id.pop_pw_et_newPW);
		pop_pw_btn_comfirm = (Button) pop_pw.findViewById(R.id.pop_pw_btn_comfirm);
		pop_pw_btn_cancel = (Button) pop_pw.findViewById(R.id.pop_pw_btn_cancel);
		
		pop_pw_et_account.setBackListener(this);
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
		
		
		/*
		 * 点击弹出修改密码View
		 */
		login_tv_forgetPW.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popw = new PopupWindow(pop_pw,LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT,true);
				popw.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
				popw.showAtLocation(v, 0, 0,LoginActivity.this.getWindowManager().getDefaultDisplay().getHeight());
				Timer timer = new Timer();
				timer.schedule(new TimerTask(){
					@Override
					public void run() {
						//打开软键盘
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
						imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS); 
					}
				}, 200);
			}
		});
		
		//确定修改密码
		pop_pw_btn_comfirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!pop_pw_et_account.getText().toString().equals("")&&
						!pop_pw_et_oldPW.getText().toString().equals("")&&
						!pop_pw_et_newPW.getText().toString().equals("")){
					pw = pop_pw_et_oldPW.getText().toString();
					newPW = pop_pw_et_newPW.getText().toString();
					account = pop_pw_et_account.getText().toString();
					modifyPW();
				}
			}
		});
		
		//取消修改密码
		pop_pw_btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pop_pw_et_newPW.setText("");
				pop_pw_et_oldPW.setText("");
				pop_pw_et_account.setText("");
				popw.dismiss();
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
	 * 修改密码
	 */
	private void modifyPW() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/ModifyUserInfo");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("tag","pw"));
					params.add(new BasicNameValuePair("account", account));
					params.add(new BasicNameValuePair("oldPW", pw));
					params.add(new BasicNameValuePair("newPW", newPW));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						Log.i("kk", response);
						if(response.equals("success")){
							///账号或密码错误
							handler.sendEmptyMessage(5);
						}else{
							handler.sendEmptyMessage(6);
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
					SharedPreferences sp = getSharedPreferences("current_user",MODE_PRIVATE);
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
							String[] nidArray = response.split(",");
							Message msg = new Message();
							msg.obj = nidArray;
							msg.what = 7;
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
	 * 退出输入框
	 */
	@Override
	public void back(TextView textView) {
		if(popw!=null&&popw.isShowing()){
			popw.dismiss();
		}
	}

}
