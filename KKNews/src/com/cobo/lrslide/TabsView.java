package com.cobo.lrslide;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * �?个简单的Tabs选项卡视�?
 * 
 * @author zwb
 *
 */
public class TabsView extends LinearLayout {

    private int mSelectedColor = 0xffff0000;// 选中的字体颜�?
    private int mNotSelectedColor = ((mSelectedColor >>> 25) << 24) | (mSelectedColor & 0x00ffffff);// 未�?�中的字体颜�?

    private int mIndicatorColor = 0xffff0000;// 指示器的颜色

    private LinearLayout mTabsContainer;// 放置tab的容�?
    private View mIndicator;// 指示器和底部横线

    private OnTabsItemClickListener listener;

    public TabsView(Context context) {
        this(context, null);
    }

    public TabsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        // 初始化容�?
        mTabsContainer = new LinearLayout(getContext());
        mTabsContainer.setOrientation(HORIZONTAL);
        mTabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(mTabsContainer);
        // 初始化指示器
        mIndicator = new View(getContext());
        mIndicator.setBackgroundColor(mIndicatorColor);
        mIndicator.setLayoutParams(new LayoutParams(300, 8));// 先任意设置宽�?
        addView(mIndicator);
      
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetIndicator();
    }

    /**
     * 重新设置指示�?
     */
    private void resetIndicator() {
        int childCount = mTabsContainer.getChildCount();
        ViewGroup.LayoutParams layoutParams = mIndicator.getLayoutParams();
        if (childCount <= 0) {
            layoutParams.width = 0;
        } else {
            layoutParams.width = getWidth() / childCount;
        }
        mIndicator.setLayoutParams(layoutParams);
        // mIndicator.setX(0f);
    }

    /**
     * 设置选项�?
     * 
     * @param titles
     */
    public void setTabs(String... titles) {
        mTabsContainer.removeAllViews();
        if (titles != null) {
            for (int i = 0; i < titles.length; i++) {
                TextView textView = new TextView(getContext());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                // textView.setTextColor(mNotSelectedColor);
                textView.setText(titles[i]);
                textView.setClickable(true);
                textView.setTextSize(18);
                textView.setPadding(0, 10, 0, 10);
                textView.setGravity(Gravity.CENTER);
                textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
                textView.setTag(i);
                textView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int position = (Integer) v.getTag();
                        setCurrentTab(position, true);
                        if (listener != null) {
                            listener.onClick(v, position);
                        }
                    }
                });
                mTabsContainer.addView(textView);
            }
            // 初始化，默认选中第一�?
            setCurrentTab(0, false);
            // 设置指示�?
            post(new Runnable() {
                @Override
                public void run() {
                    // 设置指示�?
                    resetIndicator();
                }
            });
        }
    }

    /**
     * 设置当前的tab
     * 
     * @param position
     */
    public void setCurrentTab(int position, boolean anim) {
        int childCount = mTabsContainer.getChildCount();
        if (position < 0 || position >= childCount) {
            return;
        }
        // 设置每个tab的状�?
        for (int i = 0; i < childCount; i++) {
            TextView childView = (TextView) mTabsContainer.getChildAt(i);
            if (i == position) {
                childView.setTextColor(mSelectedColor);
            } else {
                childView.setTextColor(mNotSelectedColor);
            }
        }
        // 指示器的移动
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mIndicator, "x", position * mIndicator.getWidth());
        if (anim) {
            objectAnimator.setDuration(200).start();
        } else {
            objectAnimator.setDuration(0).start();
        }
    }

    /**
     * Tabs点击的监听事�?
     * 
     * @param listener
     */
    public void setOnTabsItemClickListener(OnTabsItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnTabsItemClickListener {
        public void onClick(View view, int position);
    }
}

