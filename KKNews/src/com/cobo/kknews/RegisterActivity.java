package com.cobo.kknews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint({ "HandlerLeak", "ShowToast", "SimpleDateFormat" })
@SuppressWarnings("deprecation")
public class RegisterActivity extends Activity {
	private EditText register_et_account;
	private ImageView register_iv_notice;
	private EditText register_et_pw1;
	private EditText register_et_pw2;
	private EditText register_et_nickname;
	private Button register_btn_register;
	private ImageView register_iv_back;
	private String account;
	private String pw;
	private String nickname;
	private boolean[] fill = new boolean[4];//4个EditText是否有内容
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				register_iv_notice.setImageDrawable(RegisterActivity.this.getResources().getDrawable(R.drawable.correct));
				register_iv_notice.setVisibility(View.VISIBLE);
				Toast.makeText(RegisterActivity.this, "该账号可使用!", 0).show();
				break;
			case 2:
				register_iv_notice.setImageDrawable(RegisterActivity.this.getResources().getDrawable(R.drawable.error));
				register_iv_notice.setVisibility(View.VISIBLE);
				Toast.makeText(RegisterActivity.this, "该账号已被使用!", 0).show();
				break;
			case 3:
				Toast.makeText(RegisterActivity.this, "注册成功!", 0).show();
				onBackPressed();
				break;
			case 4:
				Toast.makeText(RegisterActivity.this, "注册失败!", 0).show();
				break;
			default:
				break;
			}
		}
		
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		register_et_account = (EditText) findViewById(R.id.register_et_account);
		register_iv_notice = (ImageView) findViewById(R.id.register_iv_notice);
		register_et_pw1 = (EditText) findViewById(R.id.register_et_pw1);
		register_et_pw2 = (EditText) findViewById(R.id.register_et_pw2);
		register_et_nickname = (EditText) findViewById(R.id.register_et_nickname);
		register_btn_register = (Button) findViewById(R.id.register_btn_register);
		register_iv_back = (ImageView) findViewById(R.id.register_iv_back);
		
		register_et_account.addTextChangedListener(new MyTextWatcher(register_et_account,fill,0));
		register_et_pw1.addTextChangedListener(new MyTextWatcher(register_et_pw1,fill,1));
		register_et_pw2.addTextChangedListener(new MyTextWatcher(register_et_pw2,fill,2));
		register_et_nickname.addTextChangedListener(new MyTextWatcher(register_et_nickname,fill,3));
		
		
		register_btn_register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!register_et_account.getText().toString().equals("")&&!register_et_pw1.getText().toString().equals("")
						&&!register_et_pw2.getText().toString().equals("")&&!register_et_nickname.getText().toString().equals("")){
					if(register_et_pw1.getText().toString().equals(register_et_pw2.getText().toString())){
						if(!register_et_nickname.getText().toString().contains(" ")){
							account = register_et_account.getText().toString();
							pw = register_et_pw1.getText().toString();
							nickname = register_et_nickname.getText().toString();
							register();
						}else{
							Toast.makeText(RegisterActivity.this,"昵称不有空格!", 0).show();
						}
					}else{
						Toast.makeText(RegisterActivity.this,"两次密码不相同!", 0).show();
					}
				}
			}
		});
		
		/**
		 * 监听账号是否输入完成，完成则发账号到服务器检查是否可用
		 */
		register_et_account.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					if(!register_et_account.getText().toString().equals("")){
						checkAccount();
					}
				}
			}
		});
		
		
		register_iv_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

	}
	
	
	/**
	 * 检查账号是否可使用
	 */
	private void checkAccount(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/Register");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation", "checkAccount"));
					params.add(new BasicNameValuePair("account", register_et_account.getText().toString()));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						if(response.equals("canUse")){
							//账号未被注册
							handler.sendEmptyMessage(1);
						}else{
							//账号已被注册
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
	 * 将注册信息发送到服务器，返回回复信息
	 */
	private void register() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpClient httpClient=null;
				try{
					httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://www.skycobo.com:8080/KKNewsServer/Register");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("operation", "register"));
					params.add(new BasicNameValuePair("account", account));
					params.add(new BasicNameValuePair("pw", pw));
					params.add(new BasicNameValuePair("nickname", nickname));
					params.add(new BasicNameValuePair("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
					UrlEncodedFormEntity entry = new UrlEncodedFormEntity(params,"utf-8");
					httpPost.setEntity(entry);
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						if(response.equals("success")){
							//注册成功
							handler.sendEmptyMessage(3);
						}else{
							//注册失败
							handler.sendEmptyMessage(4);
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
	 * 检查每个EditText是否都有内容，是的话改变注册按钮的背景颜色
	 */
	private void checkfill(){
		if((fill[0]==true)&&(fill[1]==true)&&(fill[2]==true)&&(fill[3]==true)){
			register_btn_register.setBackground(RegisterActivity.this.getResources().getDrawable(R.drawable.corner_red));
			register_btn_register.setTextColor(Color.parseColor("#fff8f8ff"));
		}else{
			register_btn_register.setBackground(RegisterActivity.this.getResources().getDrawable(R.drawable.corner));
			register_btn_register.setTextColor(Color.parseColor("#aaaaaa"));
	    }	
	}
	
	
	/**
	 *监听EditText长短变化
	 */
	class MyTextWatcher implements TextWatcher{  
	    private CharSequence temp;
	    private boolean[] fill;
	    private EditText et;
	    int index;
	    public MyTextWatcher(EditText et, boolean[] fill, int index ){
	    	this.et = et;
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
	        	if(et.getId()==register_et_account.getId()){
	        		register_iv_notice.setVisibility(View.INVISIBLE);
	        	}
	        }
	          
	    }
	};
	
	
}
