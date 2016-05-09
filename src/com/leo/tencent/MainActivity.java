package com.leo.tencent;

import java.util.Random;

import com.leo.tencent.drag.DragLayout;
import com.leo.tencent.drag.MyLinearLayout;
import com.leo.tencent.drag.DragLayout.Direction;
import com.leo.tencent.drag.DragLayout.OnDragStatusChangeListener;
import com.leo.tencent.utils.TempData;
import com.leo.tencent.utils.Utils;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{
	private static final String TAG = "TAG";
	private ListView mLeftList;
	private ListView mMainList;
	private ImageView mHeaderImage;
	private DragLayout mDragLayout;
	private ImageView mHeader;
	private View mBtRight;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		initLeftContent();
		intiMainContent();
	}
	
	
	private void initLeftContent() {
		mLeftList = (ListView) findViewById(R.id.lv_left);	
		mLeftList.setAdapter(
		new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, TempData.sCheeseStrings) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView mText = ((TextView) view);
				mText.setTextColor(Color.WHITE);
				return view;
			}
		});
	}

	private void intiMainContent() {
		// 查找Draglayout, 设置监听
		mDragLayout = (DragLayout) findViewById(R.id.dl);
		mMainList = (ListView) findViewById(R.id.lv_main);
		mHeaderImage = (ImageView) findViewById(R.id.iv_header);
		mHeaderImage.setOnClickListener(this);
		mBtRight = findViewById(R.id.iv_head_right);
		mBtRight.setOnClickListener(this);
		
		MyLinearLayout mLinearLayout = (MyLinearLayout) findViewById(R.id.mll);
		// 设置引用
		mLinearLayout.setDragLayout(mDragLayout);
		mDragLayout.setDragStatusListener(mDragListener);
		mMainList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, TempData.NAMES));
	}
	
	
	private OnDragStatusChangeListener mDragListener = new OnDragStatusChangeListener() {
		@Override
		public void onOpen() {
			Utils.showToast(MainActivity.this, "onOpen");
			// 左面板ListView随机设置一个条目
			Random random = new Random();
			int nextInt = random.nextInt(20);
			mLeftList.smoothScrollToPosition(nextInt);
		}
		@Override
		public void onDraging(float percent) {
			Log.d(TAG, "onDraging: " + percent); // 0 -> 1
			// 更新图标的透明度
			// 1.0 -> 0.0
			ViewHelper.setAlpha(mHeaderImage, 1 - percent);
		}
		@Override
		public void onClose() {
			Utils.showToast(MainActivity.this, "onClose");
			// 让图标晃动
			// mHeaderImage.setTranslationX(translationX)
			ObjectAnimator mAnim = ObjectAnimator.ofFloat(mHeaderImage, "translationX", 15.0f);
			mAnim.setInterpolator(new CycleInterpolator(4));
			mAnim.setDuration(500);
			mAnim.start();
		}
		@Override
		public void onStartOpen(Direction direction) {
			// TODO Auto-generated method stub
			
		}
	};


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_header:
			mDragLayout.open();
			break;
		case R.id.iv_head_right:
			mDragLayout.open(true,Direction.Right);
			mBtRight.setSelected(true);
			break;

		default:
			break;
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setContentView(R.layout.activity_main);
//		final ListView mLeftList = (ListView) findViewById(R.id.lv_left);
//		final ListView mMainList = (ListView) findViewById(R.id.lv_main);
//		final ImageView mHeaderImage = (ImageView) findViewById(R.id.iv_header);
//		MyLinearLayout mLinearLayout = (MyLinearLayout) findViewById(R.id.mll);
//		
//		// 查找Draglayout, 设置监听
//		DragLayout mDragLayout = (DragLayout) findViewById(R.id.dl);
//		// 设置引用
//		mLinearLayout.setDragLayout(mDragLayout);
//		mDragLayout.setDragStatusListener(new OnDragStatusChangeListener() {
//			@Override
//			public void onOpen() {
//				Utils.showToast(MainActivity.this, "onOpen");
//				// 左面板ListView随机设置一个条目
//				Random random = new Random();
//				int nextInt = random.nextInt(20);
//				mLeftList.smoothScrollToPosition(nextInt);
//			}
//			
//			@Override
//			public void onDraging(float percent) {
//				Log.d(TAG, "onDraging: " + percent); // 0 -> 1
//				// 更新图标的透明度
//				// 1.0 -> 0.0
//				ViewHelper.setAlpha(mHeaderImage, 1 - percent);
//			}
//			
//			@Override
//			public void onClose() {
//				Utils.showToast(MainActivity.this, "onClose");
//				// 让图标晃动
//				// mHeaderImage.setTranslationX(translationX)
//				ObjectAnimator mAnim = ObjectAnimator.ofFloat(mHeaderImage, "translationX", 15.0f);
//				mAnim.setInterpolator(new CycleInterpolator(4));
//				mAnim.setDuration(500);
//				mAnim.start();
//			}
//		});
//		mLeftList.setAdapter(
//				new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, TempData.sCheeseStrings) {
//					@Override
//					public View getView(int position, View convertView, ViewGroup parent) {
//						View view = super.getView(position, convertView, parent);
//						TextView mText = ((TextView) view);
//						mText.setTextColor(Color.WHITE);
//						return view;
//					}
//				});
//		mMainList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, TempData.NAMES));
//	}
}
