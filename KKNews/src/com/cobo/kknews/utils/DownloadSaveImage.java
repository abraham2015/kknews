package com.cobo.kknews.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 用于下载图片并保存的工具类
 * @author cobo
 *
 */
public class DownloadSaveImage {
	private File savePath;
	private URL downloadUrl;
	
	public  DownloadSaveImage(){}

	public DownloadSaveImage(File savePath, URL downloadUrl) {
		this.savePath = savePath;
		this.downloadUrl = downloadUrl;
	}
	
	public void ds(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection conn = null;
				BufferedInputStream bis = null;
				BufferedOutputStream bos = null;
				try {
					conn = (HttpURLConnection)downloadUrl.openConnection();
					bis = new BufferedInputStream(conn.getInputStream());
					bos = new BufferedOutputStream(new FileOutputStream(savePath));
					int len = 0;
					byte[] bys = new byte[1024];
					while((len = bis.read(bys))!=-1){
						bos.write(bys, 0, len);
					}
					bos.flush();
					bos.close();
					bis.close();
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
	
}
