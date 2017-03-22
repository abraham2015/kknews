package com.cobo.kknews;

import java.io.Serializable;

public class News implements Serializable{
	private static final long serialVersionUID = 1L;
	private int id;
	private String title;
	private String kind;
	private String source;
	private String body;
	private String imageUrl;
	private String publishTime;
	private int commentAmount;
	
	public News() {
		
	}

	public News(int id, String title, String kind, String source, String body, String imageUrl, String publishTime,
			int commentAmount) {
		super();
		this.id = id;
		this.title = title;
		this.kind = kind;
		this.source = source;
		this.body = body;
		this.imageUrl = imageUrl;
		this.publishTime = publishTime;
		this.commentAmount = commentAmount;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(String publishTime) {
		this.publishTime = publishTime;
	}

	public int getCommentAmount() {
		return commentAmount;
	}

	public void setCommentAmount(int commentAmount) {
		this.commentAmount = commentAmount;
	}
	
	
	
	

}
