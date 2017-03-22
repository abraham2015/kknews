package com.cobo.kknews;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class CommentAdapter extends ArrayAdapter<Comment> {
	private Context context;
	private List<Comment> commentList;
	
	public CommentAdapter(Context context, int textViewresourceId, List<Comment> commentList) {
		super(context, textViewresourceId, commentList);
		this.context = context;
		this.commentList = commentList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Comment comment = commentList.get(position);
		View view;
		ViewHolder viewHolder;
		if(convertView==null){
			view= LayoutInflater.from(context).inflate(R.layout.item_comment, null);
			viewHolder = new ViewHolder();
			viewHolder.icon = (MyRoundImageView) view.findViewById(R.id.item_comment_riv_icon);
			viewHolder.nickname = (TextView) view.findViewById(R.id.item_comment_tv_nickname);
			viewHolder.time = (TextView) view.findViewById(R.id.item_comment_tv_time);
			viewHolder.content = (TextView) view.findViewById(R.id.item_comment_tv_content);
			view.setTag(viewHolder);
		}else{
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}
		if(comment.getIconUrl()!=""){
			viewHolder.icon.setImageUrl("http://www.skycobo.com:8080/KKNews_data/user_inco/"+comment.getIconUrl());
		}
		viewHolder.nickname.setText(comment.getNickname());
		viewHolder.time.setText(comment.getTime());
		viewHolder.content.setText(comment.getContent());
		return view;
		
	}
	
	class ViewHolder{
		MyRoundImageView icon;
		TextView nickname;
		TextView time;
		TextView content;
	}
}
