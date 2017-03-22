package com.cobo.kknews;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressWarnings("unused")
public class MeSetActivity extends Activity {
	public static final int TAKE_PHOTO = 1;
	public static final int CROP_PHOTO = 2;
	public static final int CHOOSE_PHOTO = 3;
	private ImageView meset_iv_back;
	private TextView meset_tv_account;
	private TextView meset_tv_nickname;
	private MyRoundImageView meset_riv_inco;
	private Button meset_btn_quit;
	private SharedPreferences sp;
	private File incoFile;
	private Uri incoUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meset);
		
		meset_iv_back = (ImageView) findViewById(R.id.meset_iv_back);
		meset_tv_account = (TextView) findViewById(R.id.meset_tv_account);
		meset_tv_nickname = (TextView) findViewById(R.id.meset_tv_nickname);
		meset_riv_inco = (MyRoundImageView) findViewById(R.id.meset_riv_inco);
		meset_btn_quit = (Button) findViewById(R.id.meset_btn_quit);
		
		sp = getSharedPreferences("current_user", MODE_PRIVATE);
		String currentUser = sp.getString("account", null);
		sp = getSharedPreferences("user_"+currentUser,MODE_PRIVATE);
		meset_tv_account.setText(sp.getString("account", null));
		meset_tv_nickname.setText(sp.getString("nickname", null));
		setInco();//设置头像
		
		
		/**
		 * 选择相机或相册获取照片重新设置头像
		 */
		meset_riv_inco.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder adb = new AlertDialog.Builder(MeSetActivity.this);
				adb.setMessage("请选择获取照片的方式:");
				adb.setPositiveButton("相机", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
						i.putExtra(MediaStore.EXTRA_OUTPUT,incoUri);
						startActivityForResult(i, TAKE_PHOTO);//启动相机程序
					}
				});
				adb.setNegativeButton("相册", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent("android.intent.action.GET_CONTENT");
						i.setType("image/*");
						startActivityForResult(i, CHOOSE_PHOTO);//启动相册程序
					}
				});
				adb.show();
			}
		});
		
		
		meset_btn_quit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//清除保存在SharedPreferences里当前登录账号
				sp = getSharedPreferences("current_user", MODE_PRIVATE);
				Editor edit = sp.edit();
				edit.remove("account");
				edit.apply();
				finish();
				Intent i = new Intent(MeSetActivity.this,LoginActivity.class);
				startActivity(i);
			}
		});
		
		
		meset_iv_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
 	}
    
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case TAKE_PHOTO:
			if(resultCode == RESULT_OK){
				Intent i = new Intent("com.android.camera.action.CROP");
				i.setDataAndType(incoUri, "image/*");
				i.putExtra("scale", true);
				i.putExtra(MediaStore.EXTRA_OUTPUT,incoUri);
				startActivityForResult(i, CROP_PHOTO);
			}
			break;
		case CROP_PHOTO:
			if(resultCode == RESULT_OK){
				try{
					Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(incoUri));
					meset_riv_inco.setImageBitmap(bitmap);
				}catch( FileNotFoundException e){
					e.printStackTrace();
				}
			}
			uploadInco();//上传图片到服务器
			break;
		case CHOOSE_PHOTO:
			Uri uri = data.getData();
			Intent i = new Intent("com.android.camera.action.CROP");
			i.setDataAndType(Uri.fromFile(new File(uri.getPath())), "image/*");
			i.putExtra("scale", true);
			i.putExtra(MediaStore.EXTRA_OUTPUT,incoUri);
			startActivityForResult(i, CROP_PHOTO);
			break;
		default:
			break;
		}
	}
    
    
    /**
     * 携带文件名上传头像到服务器
     */
    private void uploadInco(){
    	new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection conn = null;
				String result = null;
			    String  BOUNDARY =  UUID.randomUUID().toString();  //边界标识   随机生成
			    String PREFIX = "--" , LINE_END = "\r\n"; 
			    String CONTENT_TYPE = "multipart/form-data";   //内容类型
			    String CHARSET = "utf-8";
				try {
					URL url = new URL("http://www.skycobo.com:8080/KKNewsServer/UploadInco");
					conn = (HttpURLConnection)url.openConnection();
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);
					conn.setDoInput(true);
					conn.setUseCaches(false);
					conn.setConnectTimeout(8000);
					conn.setReadTimeout(8000);
		            conn.setRequestProperty("Charset", CHARSET);  //设置编码
		            conn.setRequestProperty("connection", "keep-alive");
		            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
		            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY); 
		            
		            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
		            StringBuffer sb = new StringBuffer();
		            sb.append(PREFIX);
		            sb.append(BOUNDARY);
		            sb.append(LINE_END);
		            sb.append("Content-Disposition: form-data; name=\"img\"; filename=\""+incoFile.getName()+"\""+LINE_END); 
	                sb.append("Content-Type: application/octet-stream; charset="+CHARSET+LINE_END);
	                sb.append(LINE_END);
	                dos.write(sb.toString().getBytes());
		            
		            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(incoFile));
		    		byte[] b = new byte[1024];
		    		int len = 0;
		    		while((len=bis.read(b))!=-1){
		    			dos.write(b,0,len);
		    		}
		    		bis.close();
		    		dos.write(LINE_END.getBytes());
		    		byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();
		    		dos.write(end_data);
		    		dos.flush();
		    		dos.close();
		    		conn.getResponseCode();  
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(conn != null){
					conn.disconnect();
				}
			}
			}
		}).start();
    }
    
    /**
     * 从sd卡中找到头像，设置给ImageView
     */
	private void setInco() {
		incoFile = new File(Environment.getExternalStorageDirectory()+"/KKNews/user_icon",sp.getString("account", null)+".jpg");
		incoUri = Uri.fromFile(incoFile);
		if(incoFile.exists()){
			//文件不为空
			Bitmap bitmap =null;
			try {
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(incoUri));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			meset_riv_inco.setImageBitmap(bitmap);
		}
	}
}
