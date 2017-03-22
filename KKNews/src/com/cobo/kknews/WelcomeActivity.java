package com.cobo.kknews;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
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
	    
	    new Handler().postDelayed(new Runnable() {
			public void run() {
				Intent i = new Intent(WelcomeActivity.this,HomeActivity.class);
				startActivity(i);
				WelcomeActivity.this.finish();
			}
		}, 2000);
	}
}
