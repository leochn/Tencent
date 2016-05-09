package com.leo.tencent.drag;




import com.nineoldandroids.view.ViewHelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class DragLayout extends FrameLayout {
	private static final String TAG = "DragLayout";
	private ViewDragHelper mDragHelper;
	private ViewGroup mLeftContent;
	private ViewGroup mMainContent;
	private View mRightContent;

	private int mHeight;   //mMainContent的高
	private int mWidth;    //mMainContent的宽
	private int mRangeLeft;  //mMainContent往右拉时,左侧的值
	private int mRangeRight; //
	private int mRightWidth;
	
	private OnDragStatusChangeListener mDragStatusListener;
	private boolean mScaleEnable = true;
	private Status mStatus = Status.Close;
	private Direction mDirection = Direction.Left;
	
	public static enum Status {
		Close, Open, Draging;
	}
	
	public static enum Direction{
		Left,Right,Default
	}
	public interface OnDragStatusChangeListener{
		void onClose();
		void onStartOpen(Direction direction);
		void onOpen();
		void onDraging(float percent);
	}
	public void setDragStatusListener(OnDragStatusChangeListener mListener){
		this.mDragStatusListener = mListener;
	}
	public Status getStatus(){
		System.out.println("getStatus...");
		return mStatus;
	}
	public void setStatus(Status mStatus){
		this.mStatus = mStatus;
	}

	public DragLayout(Context context) {
		// super(context);
		this(context, null); // change: Call your own constructor
	}

	public DragLayout(Context context, AttributeSet attrs) {
		// super(context, attrs);
		this(context, attrs, 0);
	}

	public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// 1.初始化（通过静态方法）
		mDragHelper = ViewDragHelper.create(this, mCallback);
	}
	
	// 什么时候被调用??(在onMeasure方法之后,并且当尺寸发生改变的时候调用),(获取屏幕的宽高)
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// 当尺寸有变化的时候调用
		mHeight = mMainContent.getMeasuredHeight();
		mWidth = mMainContent.getMeasuredWidth();
		mRightWidth= mRightContent.getMeasuredWidth();
		// 主面板移动的范围(0--mRange)
		mRangeLeft = (int) (mWidth * 0.6f);
		mRangeRight = mRightWidth;
	}
	
	private int mMainLeft = 0;

	// 3. 重写事件
	ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
		// 3.1 根据返回结果决定当前child是否可以拖拽
		// child 当前被拖拽的View, pointerId 区分多点触摸的id
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			// return child == mMainContent;
			return true; // 返回true,表示所有的child都能被拖拽
		}

		public void onViewCaptured(View capturedChild, int activePointerId) {
			// 当capturedChild被捕获时,调用
			Log.d(TAG, "onViewCaptured" + capturedChild);
			super.onViewCaptured(capturedChild, activePointerId);
		}

		public int getViewHorizontalDragRange(View child) {
			// 返回拖拽的范围, 不对拖拽进行真正的限制. 仅仅决定了动画执行速度
			return mRangeLeft;

		}

		// 3.2(重要) 根据建议值修正将要移动到的(横向)位置
		// child:当前拖拽的View, left:新的位置的建议值, dx:位置变化量,向右移动dx为正值
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			// left = oldLeft + dx
			Log.d(TAG, "clampViewPositionHorizontal...oldLeft:" + child.getLeft() + "...dx:" + dx + "...left:" + left);
			// if (child == mMainContent) {
			// left = fixLeft(left);//(保证在0--mRange范围)
			// }
			// return left;
			return clampResult(mMainLeft + dx, left);
		}

		// 3.3 当View位置改变的时候, 处理要做的事情 (更新状态, 伴随动画, 重绘界面)
		// 此时,View已经发生了位置的改变
		public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//			// changedView 改变位置的View; left:新的左边值; dx:水平方向变化量
//			// 拖拽changedView时,引起left,top,dx,dy值的变化
//			super.onViewPositionChanged(changedView, left, top, dx, dy);
//			int newLeft = left;
//			if (changedView == mLeftContent) {
//				// 把拖拽mLeftContent的变化量dx传递给mMainContent
//				newLeft = mMainContent.getLeft() + dx;
//			}
//			// 进行修正(保证在0--mRange范围)
//			newLeft = fixLeft(newLeft);
//			Log.d(TAG, "onViewPositionChanged: " + "left: " + left + " dx: " + dx
//					+ " newLeft: " + newLeft);
//			if (changedView == mLeftContent) {
//				// 当左面板移动之后, 再强制返回到初始状态.
//				mLeftContent.layout(0, 0, 0 + mWidth, 0 + mHeight);
//				// 把拖拽mLeftContent的变化量dx传递给mMainContent
//				mMainContent.layout(newLeft, 0, newLeft + mWidth, 0 + mHeight);
//			}
			
			if (changedView == mMainContent) {
				mMainLeft = left;
			}else {
				mMainLeft += dx;
			}
			mMainLeft = clampResult(mMainLeft, mMainLeft);
			if (changedView == mLeftContent || changedView == mRightContent) {
				layoutContent();
			}
			// 更新状态,执行动画
			dispatchDragEvent(mMainLeft);
			// 为了兼容低版本, 每次修改值之后, 进行重绘
			invalidate();
		}
		//3.4 当View被释放的时候, 处理的事情(执行动画)
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			// View releasedChild 被释放的子View
			// float xvel 水平方向的速度, 向右为+
			// float yvel 竖直方向的速度, 向下为+
			
			boolean scrollRight = xvel > 1.0f;
			boolean scrollLeft = xvel < -1.0f;
			if (scrollRight || scrollLeft) {
				if (scrollRight && mDirection == Direction.Left) {
					open(true,mDirection);
				}else if (scrollLeft && mDirection == Direction.Right) {
					open(true,mDirection);
				}else {
					close(true);
				}
				return;
			}
			
			if (releasedChild == mLeftContent && mMainLeft > mRangeLeft * 0.7f) {
				open(true, mDirection);
			} else if (releasedChild == mMainContent) {
				if (mMainLeft > mRangeLeft * 0.3f)
					open(true, mDirection);
				else if (-mMainLeft > mRangeRight * 0.3f)
					open(true, mDirection);
				else
					close(true);
			} else if (releasedChild == mRightContent
					&& -mMainLeft > mRangeRight * 0.7f) {
				open(true, mDirection);
			} else {
				close(true);
			}
			
			
			
//			super.onViewReleased(releasedChild, xvel, yvel);
//			Log.d(TAG, "onViewReleased: " + "xvel: " + xvel + " yvel: " + yvel);
//			// 判断执行 关闭/开启
//			// 先考虑所有开启的情况,剩下的就都是关闭的情况
//			if(xvel == 0 && mMainContent.getLeft() > mRangeLeft / 2.0f){
//				open();
//			}else if (xvel > 0) {
//				open();
//			}else {
//				close();
//			}
		}
		
		
		
		@Override
		public void onViewDragStateChanged(int state) {
			if (mStatus == Status.Close && state == ViewDragHelper.STATE_IDLE
					&& mDirection == Direction.Right) {
				mDirection = Direction.Left;
			}
		}

		

	};
	
	private void layoutContent(){
		mLeftContent.layout(0, 0, mWidth, mHeight);
		mRightContent.layout(mWidth - mRightWidth, 0, mWidth, mHeight);
		mMainContent.layout(mMainLeft, 0, mMainLeft + mWidth, mHeight);
	}
	
	private int clampResult(int tempValue,int defaultValue){
		Integer minLeft = null;
		Integer maxLeft = null;
		if (mDirection == Direction.Left) {
			minLeft = 0;
			maxLeft = 0 + mRangeLeft;
		}else if (mDirection == Direction.Right) {
			minLeft = 0 - mRangeRight;
			maxLeft = 0;
		}
		if (minLeft != null && tempValue < minLeft) {
			return minLeft;
		}else if (maxLeft != null && tempValue > maxLeft) {
			return maxLeft;
		}else {
			return defaultValue;
		}
	}


	// 根据范围修正左边值(保证在0--mRange范围)
	private int fixLeft(int left) {
		if (left < 0) {
			return 0;
		} else if (left > mRangeLeft) {
			return mRangeLeft;
		}
		return left;
	}
	
	public void close(){
		close(true);
	}
	public void close(boolean isSmooth) {
		int finalLeft = 0;
		if(isSmooth){
			// 3.4.1. 触发一个平滑动画
			if(mDragHelper.smoothSlideViewTo(mMainContent, finalLeft, 0)){
				// 返回true代表还没有移动到指定位置, 需要刷新界面.
				// 参数传this(child所在的ViewGroup)
				ViewCompat.postInvalidateOnAnimation(this);
				System.out.println("onViewReleased....isSmooth");
			}
		}else {
			mMainContent.layout(finalLeft, 0, finalLeft + mWidth, 0 + mHeight);
			System.out.println("onViewReleased....");
		}
	}
	public void open(){
		open(true);
	}
	
	public void open(boolean withAnim) {
		open(withAnim, Direction.Left);
	}
	
	public void open(boolean withAnim,Direction d){
		mDirection = d;

		if (mDirection == Direction.Left)
			mMainLeft = mRangeLeft;
		else if (mDirection == Direction.Right)
			mMainLeft = -mRangeRight;

		if (withAnim) {
			// 引发动画的开始
			if (mDragHelper.smoothSlideViewTo(mMainContent, mMainLeft, 0)) {
				// 需要在computeScroll中使用continueSettling方法才能将动画继续下去（因为ViewDragHelper使用了scroller）。
				ViewCompat.postInvalidateOnAnimation(this);
			}
		} else {
			layoutContent();
			
			dispatchDragEvent(mMainLeft);
		}
	}
//	public void open(boolean isSmooth) {
//		int finalLeft = mRangeLeft;
//		if(isSmooth){
//			// 3.4.1. 触发一个平滑动画
//			if(mDragHelper.smoothSlideViewTo(mMainContent, finalLeft, 0)){
//				// 返回true代表还没有移动到指定位置, 需要刷新界面.
//				// 参数传this(child所在的ViewGroup)
//				ViewCompat.postInvalidateOnAnimation(this);
//			}
//		}else {
//			mMainContent.layout(finalLeft, 0, finalLeft + mWidth, 0 + mHeight);
//		}
//	}
	
	@Override
	public void computeScroll() {
		super.computeScroll();
		//3.4.2 持续平滑动画(高频率调用)
		if (mDragHelper.continueSettling(true)) {
			//  如果返回true, 动画还需要继续执行
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}
	

	private void dispatchDragEvent(int mainLeft) {
		// 注意转换成float
		float percent = 0;
		if (mDirection == Direction.Left)
			percent = mainLeft / (float) mRangeLeft;
		else if (mDirection == Direction.Right)
			percent = Math.abs(mainLeft) / (float) mRangeRight;

		if (mDragStatusListener != null) {
			mDragStatusListener.onDraging(percent);
		}

		// 更新动画
		if (mScaleEnable) {
			animViews(percent);
		}
		// 更新状态
		Status lastStatus = mStatus;
		if (updateStatus() != lastStatus) {
			if (lastStatus == Status.Close && mStatus == Status.Draging) {
				mLeftContent.setVisibility(mDirection == Direction.Left ? View.VISIBLE : View.GONE);
				mRightContent.setVisibility(mDirection == Direction.Right ? View.VISIBLE : View.GONE);

				if (mDragStatusListener != null) {
					mDragStatusListener.onStartOpen(mDirection);
				}
			}

			if (mStatus == Status.Close) {
				if (mDragStatusListener != null)
					mDragStatusListener.onClose();
			} else if (mStatus == Status.Open) {
				if (mDragStatusListener != null)
					mDragStatusListener.onOpen();
			}
		}
	}
	
	
	
//	private void dispatchDragEvent(int newLeft) {
//		//newLeft的范围在0--mRange
//		float percent = newLeft * 1.0f / mRangeLeft;
//		if (mDragStatusListener != null) {
//			mDragStatusListener.onDraging(percent);
//		}
//		//更新状态,执行回调
//		Status preStatus = mStatus;
//		mStatus = updateStatus(percent);
//		if (mStatus != preStatus) {
//			//状态发生变化
//			if (mStatus == Status.Close) {
//				//当前为关闭状态
//				if (mDragStatusListener != null) {
//					mDragStatusListener.onClose();
//				}
//			}else if (mStatus == Status.Open) {
//				if (mDragStatusListener != null) {
//					mDragStatusListener.onOpen();
//				}
//			}
//		}
//		//伴随动画
//		animViews(percent);
//	}
	
//	private void dispatchDragEvent(int newLeft) {
//		//newLeft的范围在0--mRange
//		float percent = newLeft * 1.0f / mRangeLeft;
//		if (mDragStatusListener != null) {
//			mDragStatusListener.onDraging(percent);
//		}
//		//更新状态,执行回调
//		Status preStatus = mStatus;
//		mStatus = updateStatus(percent);
//		if (mStatus != preStatus) {
//			//状态发生变化
//			if (mStatus == Status.Close) {
//				//当前为关闭状态
//				if (mDragStatusListener != null) {
//					mDragStatusListener.onClose();
//				}
//			}else if (mStatus == Status.Open) {
//				if (mDragStatusListener != null) {
//					mDragStatusListener.onOpen();
//				}
//			}
//		}
//		//伴随动画
//		animViews(percent);
//	}
	
	
	private Status updateStatus(){
		if (mDirection == Direction.Left) {
			if (mMainLeft == 0) {
				mStatus = Status.Close;
			} else if (mMainLeft == mRangeLeft) {
				mStatus = Status.Open;
			} else {
				mStatus = Status.Draging;
			}
		} else if (mDirection == Direction.Right) {
			if (mMainLeft == 0) {
				mStatus = Status.Close;
			} else if (mMainLeft == 0 - mRangeRight) {
				mStatus = Status.Open;
			} else {
				mStatus = Status.Draging;
			}
		}
		return mStatus;
	}
	
	
	
	private void animViews(float percent) {
		Log.d(TAG, "percent: " + percent);
		animMainView(percent);

		animBackView(percent);
	}

	private void animBackView(float percent) {
		if (mDirection == Direction.Right) {
			// 右边栏X, Y放大，向左移动, 逐渐显示
			ViewHelper.setScaleX(mRightContent, 0.5f + 0.5f * percent);
			ViewHelper.setScaleY(mRightContent, 0.5f + 0.5f * percent);
			ViewHelper.setTranslationX(mRightContent,
					evaluate(percent, mRightWidth + mRightWidth / 2.0f, 0.0f));

			ViewHelper.setAlpha(mRightContent, percent);
		} else {
			// 左边栏X, Y放大，向右移动, 逐渐显示
			ViewHelper.setScaleX(mLeftContent, 0.5f + 0.5f * percent);
			ViewHelper.setScaleY(mLeftContent, 0.5f + 0.5f * percent);
			ViewHelper.setTranslationX(mLeftContent,
					evaluate(percent, -mWidth / 2f, 0.0f));
			ViewHelper.setAlpha(mLeftContent, percent);
		}
		// 背景逐渐变亮
		getBackground().setColorFilter(
				evaluateColor(percent, Color.BLACK, Color.TRANSPARENT),
				PorterDuff.Mode.SRC_OVER);
	}

	private void animMainView(float percent) {
		Float inverseP = null;
		if (mDirection == Direction.Left) {
			inverseP = 1 - percent * 0.25f;
		} else if (mDirection == Direction.Right) {
			inverseP = 1 - percent * 0.25f;
		}
		// 主界面X,Y缩小
		if (inverseP != null) {
			if (mDirection == Direction.Right) {
				ViewHelper.setPivotX(mMainContent, mWidth);
				ViewHelper.setPivotY(mMainContent, mHeight / 2.0f);
			} else {
				ViewHelper.setPivotX(mMainContent, mWidth / 2.0f);
				ViewHelper.setPivotY(mMainContent, mHeight / 2.0f);
			}
			ViewHelper.setScaleX(mMainContent, inverseP);
			ViewHelper.setScaleY(mMainContent, inverseP);
		}
	}
//	private Status updateStatus(float percent){
//		if (percent == 0.0f) {
//			return Status.Close;
//		}else if (percent == 1.0f) {
//			return Status.Open;
//		}else {
//			return Status.Draging;
//		}
//	}
	
//	private void animViews(float percent){
//		// 1. 左面板: 缩放动画, 平移动画, 透明度动画
//		// 缩放动画 0.0 -> 1.0 >>> 0.5f -> 1.0f >>> 0.5f * percent + 0.5f
//		// mLeftContent.setScaleX(0.5f + 0.5f * percent);
//		// mLeftContent.setScaleY(0.5f + 0.5f * percent);
//		ViewHelper.setScaleX(mLeftContent, evaluate(percent, 0.5f, 1.0f));
//		ViewHelper.setScaleY(mLeftContent, 0.5f + 0.5f * percent);
//		// 平移动画: -mWidth / 2.0f -> 0.0f
//		ViewHelper.setTranslationX(mLeftContent, evaluate(percent, -mWidth / 2.0f, 0));
//		// 透明度: 0.5 -> 1.0f
//		ViewHelper.setAlpha(mLeftContent, evaluate(percent, 0.5f, 1.0f));
//
//		// 2. 主面板: 缩放动画
//		// 1.0f -> 0.8f
//		ViewHelper.setScaleX(mMainContent, evaluate(percent, 1.0f, 0.8f));
//		ViewHelper.setScaleY(mMainContent, evaluate(percent, 1.0f, 0.8f));
//
//		// 3. 背景动画: 亮度变化 (颜色变化)
//		getBackground().setColorFilter((Integer)evaluateColor(percent, Color.BLACK, Color.TRANSPARENT)
//				, Mode.SRC_OVER);
//	}
	
	/**
     * 估值器
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }
    
    /**
     * 颜色变化过度
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public int evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int)((startA + (int)(fraction * (endA - startA))) << 24) |
                (int)((startR + (int)(fraction * (endR - startR))) << 16) |
                (int)((startG + (int)(fraction * (endG - startG))) << 8) |
                (int)((startB + (int)(fraction * (endB - startB))));
    }

	// 2.传递触摸事件
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// 传递给mDragHelper
		return mDragHelper.shouldInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			mDragHelper.processTouchEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true; // 返回true,持续接收事件
	}

	// 什么时候被调用:
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Log.d(TAG, "onFinishInflate............");
		if (getChildCount() < 2) {
			throw new IllegalStateException("布局至少有俩孩子." + "Your ViewGroup must have 2 children at least!");
		}
		if (!(getChildAt(0) instanceof ViewGroup && getChildAt(1) instanceof ViewGroup)) {
			throw new IllegalArgumentException(
					"子View必须是ViewGroup的子类. " + "Your children must be an instance of ViewGroup");
		}
		mLeftContent = (ViewGroup) getChildAt(0);
		mRightContent = getChildAt(1);
		mMainContent = (ViewGroup) getChildAt(2);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		layoutContent();
	}


}
