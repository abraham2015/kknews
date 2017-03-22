package com.cobo.kknews;



import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

public class MyEditText extends EditText {
	private BackListener listener;
	public MyEditText(Context context) {
		super(context);
	}
	public MyEditText(Context context,AttributeSet attrs) {
		super(context,(AttributeSet) attrs);
	}
	public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
	}
	public interface BackListener{
		void back(TextView textView);
	}
	public void setBackListener(BackListener listener){
		this.listener = listener;
	}
	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(listener!=null){
				listener.back(this);
			}
		}
		return false;
	}
	

}
