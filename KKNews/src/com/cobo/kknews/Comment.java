package com.cobo.kknews;

public class Comment {
	private int cid;
	private int nid;
	private String account;
	private String nickname;
	private String iconUrl;
	private String content;
	private String time;
	
	public Comment() {}

	public Comment(int cid, int nid, String account, String nickname, String iconUrl, String content, String time) {
		super();
		this.cid = cid;
		this.nid = nid;
		this.account = account;
		this.nickname = nickname;
		this.iconUrl = iconUrl;
		this.content = content;
		this.time = time;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public int getNid() {
		return nid;
	}

	public void setNid(int nid) {
		this.nid = nid;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	
	
}
