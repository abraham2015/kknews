package com.cobo.kknews;

import java.io.Serializable;

public class CollectNews extends News implements Serializable{
	private static final long serialVersionUID = 1L;
	private String collectTime; 
	
	public CollectNews(){}

	public CollectNews(int id, String title, String kind, String source, String body, String imageUrl,
			String publishTime, int commentAmount ,String collectTime) {
		super(id, title, kind, source, body, imageUrl, publishTime, commentAmount);
		this.collectTime = collectTime;
	}

	public String getCollectTime() {
		return collectTime;
	}

	public void setCollectTime(String collectTime) {
		this.collectTime = collectTime;
	}
	
	

}
