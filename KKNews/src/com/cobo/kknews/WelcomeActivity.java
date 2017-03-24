package com.cobo.kknews;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

public class WelcomeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_welcome);
	    
	    //新建两个文件夹，用于存储应用的数据
	    File file  = new File(Environment.getExternalStorageDirectory()+"/KKNews","news_image");
	    if(!file.exists()){
	    	file.mkdirs();
	    }
	    file =  new File(Environment.getExternalStorageDirectory()+"/KKNews","user_icon");
	    if(!file.exists()){
	    	file.mkdir();
	    }
	    
	    //
	    
	    //初始化新闻阅读位置
	    SharedPreferences sp = getSharedPreferences("position_1", MODE_PRIVATE);
	    Editor edit = sp.edit();
	    edit.putInt("position", -1);
	    edit.apply();
	    sp = getSharedPreferences("position_2", MODE_PRIVATE);
	    edit = sp.edit();
	    edit.putInt("position", -1);
	    edit.apply();
	    sp = getSharedPreferences("position_3", MODE_PRIVATE);
	    edit = sp.edit();
	    edit.putInt("position", -1);
	    edit.apply();
	    sp = getSharedPreferences("position_4", MODE_PRIVATE);
	    edit = sp.edit();
	    edit.putInt("position", -1);
	    edit.apply();
	    sp = getSharedPreferences("position_5", MODE_PRIVATE);
	    edit = sp.edit();
	    edit.putInt("position", -1);
	    edit.apply();
	    
	    new Handler().postDelayed(new Runnable() {
			public void run() {
				Intent i = new Intent(WelcomeActivity.this,HomeActivity.class);
				startActivity(i);
				WelcomeActivity.this.finish();
			}
		}, 2000);
	}
}
