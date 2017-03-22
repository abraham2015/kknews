
package com.cobo.kknews;

import java.util.ArrayList;
import java.util.List;

import com.cobo.lrslide.TabsView;
import com.cobo.lrslide.TabsView.OnTabsItemClickListener;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class HomeActivity extends FragmentActivity {
    private List<Fragment> fragments;
    private MyAdapter adapter;
    private ViewPager mViewPager;
    private TabsView mTabs;
    private SharedPreferences sp;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        
        
        ImageView home_iv_me = (ImageView) findViewById(R.id.home_iv_me);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabs = (TabsView) findViewById(R.id.tabslayout);

        initData();
        
        /**
         * 根据是否有账号，选择个人中心页面或登陆页面
         */
        home_iv_me.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sp = getSharedPreferences("current_user",MODE_PRIVATE);
				if(sp.getString("account", null)!=null){
					Intent i = new Intent(HomeActivity.this,MeActivity.class);
					startActivity(i);
				}else{
					Intent i = new Intent(HomeActivity.this,LoginActivity.class);
					startActivity(i);
				}
			}
		});
        
    }

    private void initData() {
        fragments = new ArrayList<Fragment>();
        fragments.add(new FragmentHeadline());
        fragments.add(new FragmentSociety());
        fragments.add(new FragmentEntertainment());
        fragments.add(new FragmentSports());
        fragments.add(new FragmentTechnology());
    
        adapter = new MyAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
      
        mTabs.setTabs("头条","社会","娱乐","体育","科技");
        mTabs.setOnTabsItemClickListener(new OnTabsItemClickListener() {

            @Override
            public void onClick(View view, int position) {
                mViewPager.setCurrentItem(position, true);
            }
        });
        //
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                mTabs.setCurrentTab(position, true);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

	
}
