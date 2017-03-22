package com.cobo.kknews;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import com.cobo.kknews.utils.DownloadSaveImage;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class MeActivity extends Activity {
	private ImageView me_iv_back;
	private MyRoundImageView me_riv_icon;
	private TextView me_tv_nickname;
	private TextView me_tv_collect;
	private TextView me_tv_read;
	private SharedPreferences sp;
	private File incoFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me);
		
		me_iv_back = (ImageView) findViewById(R.id.me_iv_back);
		me_riv_icon = (MyRoundImageView) findViewById(R.id.me_riv_icon);
		me_tv_nickname = (TextView) findViewById(R.id.me_tv_nickname);
		me_tv_collect = (TextView) findViewById(R.id.me_tv_collect);
		me_tv_read = (TextView) findViewById(R.id.me_tv_read);
		
		sp = getSharedPreferences("current_user", MODE_PRIVATE);
		sp = getSharedPreferences("user_"+sp.getString("account", null),MODE_PRIVATE);
		me_tv_nickname.setText(sp.getString("nickname", null));
		incoFile = new File(Environment.getExternalStorageDirectory()+"/KKNews/user_icon",sp.getString("account", null)+".jpg");
		setInco();//设置头像
		
		
		me_riv_icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MeActivity.this,MeSetActivity.class);
				startActivity(i);
			}
		});
		
		/**
		 * 点击跳转收藏页面
		 */
		me_tv_collect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MeActivity.this,CollectActivity.class));
			}
		});
		
		
		/**
		 * 点击跳转最近阅读界面
		 */
		me_tv_read.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MeActivity.this,ReadActivity.class));
			}
		});
		
		
		
		me_iv_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}
	
	/*@Override
	protected void onRestart() {
		onCreate(null);
	}*/
	
	
	/**
	 * 设置头像，如果在本地找不到，就从服务器下载并且重新存到本地
	 */
	private void setInco() {
		if(!incoFile.exists()){
			//文件不存在，下载头像
			try {
				new DownloadSaveImage(incoFile,new URL("http://www.skycobo.com:8080/KKNews_data/user_inco/"+sp.getString("account", null)+".jpg")).ds();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			me_riv_icon.setImageUrl("http://www.skycobo.com:8080/KKNews_data/user_inco/"+sp.getString("account", null)+".jpg");
		}else{
			Uri incoUri = Uri.fromFile(incoFile);
			Bitmap bitmap =null;
			try {
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(incoUri));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			me_riv_icon.setImageBitmap(bitmap);
		}
		
	}
	
}
