package com.jyuesong.dragview2fill.dragview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.jyuesong.dragview2fill.FastModel;
import com.jyuesong.dragview2fill.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by jiang on 29/01/2018.
 */
public class DragViewGroup extends ViewGroup {

    private static final String TAG = "DragViewGroup";

    public static final int DEFAULT = -0x110011;

    private View[] view1 = new View[2];
    private View[] view2 = new View[2];
    private View[] view3 = new View[2];

    private Rect[] mBorders = new Rect[2];
    private ViewDragHelper mViewDragHelper;

    private Paint mBgPaint;

    private LayoutInflater mLayoutInflater;
    private int mWidth;
    private int mHeight;

    private RecyclerViewHelper mRecyclerViewHelper;
    private boolean dragEnable = true;
    private PublishSubject<int[]> mPublishSubject;


    public DragViewGroup(Context context) {
        this(context, null);
    }

    public DragViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mLayoutInflater = LayoutInflater.from(context);
        mRecyclerViewHelper = RecyclerViewHelper.create(this);
        initViews();
    }


    private void initViews() {
        initSize();

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL);
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new CallBack());

        //可以直接通过布局文件inflate进来
        addView(mRecyclerViewHelper.generateRecyclerView());
        addView(view1[0] = createView1());
        addView(view2[0] = createView2());
        addView(view3[0] = createView3());
        addView(view1[1] = createView1());
        addView(view2[1] = createView2());
        addView(view3[1] = createView3());

        LayoutParams layoutParams = mRecyclerViewHelper.getRecyclerView().getLayoutParams();
        layoutParams.width = mWidth * 2 / 3;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        mRecyclerViewHelper.getRecyclerView().setLayoutParams(layoutParams);
        mRecyclerViewHelper.init();
    }

    private void initSize() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
    }

    public void dragEnable(boolean can) {
        dragEnable = can;
    }


    private View createView2() {
        View view = mLayoutInflater.inflate(R.layout.fast_item_left2, this, false);
        LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = mWidth / 3;
        view.setLayoutParams(layoutParams);
        view.setTag(T_PANDUAN);
        return view;
    }


    private View createView3() {
        View view = mLayoutInflater.inflate(R.layout.fast_item_left3, this, false);
        LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = mWidth / 3;
        view.setLayoutParams(layoutParams);
        view.setTag(T_ZHUGUAN);
        return view;
    }


    private View createView1() {
        View view = mLayoutInflater.inflate(R.layout.fast_item_left, this, false);
        LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = mWidth / 3;
        view.setLayoutParams(layoutParams);
        view.setTag(T_XUANZE);
        return view;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (getChildCount() < 0) return;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            // 测量逻辑不用管太多
            int childWidth;
            int childHeight;
            //直接强制写死宽高
            childWidth = MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), MeasureSpec.EXACTLY);
            childHeight = MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY);
            child.measure(childWidth, childHeight);
        }


        //这的宽高可根据需求自己写测量逻辑
        super.onMeasure(MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
        Rect rect1 = new Rect(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + getMeasuredWidth() / 3, getPaddingTop() + getMeasuredHeight());
        mBorders[0] = rect1;
        Rect rect2 = new Rect(getPaddingLeft() + getMeasuredWidth() / 3, getPaddingTop(), getPaddingLeft() + getMeasuredWidth(), getPaddingTop() + getMeasuredHeight());
        mBorders[1] = rect2;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() <= 0) return;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof RecyclerView) {
                //recyclerview
                LayoutHelper.layoutRecyclerView(this, view);
            } else if (view instanceof View) {
                if (mDragView != null && mDragView == view && mDragLocation[0] != DEFAULT && mDragLocation[1] != DEFAULT) {
                    view.layout(mDragLocation[0], mDragLocation[1],
                            view.getMeasuredWidth() + mDragLocation[0], view.getMeasuredHeight() + mDragLocation[1]);
                } else {
                    LayoutHelper.layoutChild(this, view);
                }
            }
        }


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBgPaint.setColor(Color.parseColor("#ffffff"));
        canvas.drawRect(mBorders[0], mBgPaint);
        mBgPaint.setColor(Color.parseColor("#f1f1f1"));
        canvas.drawRect(mBorders[1], mBgPaint);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mViewDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    public void registerSizeChangeCallBack(RecyclerViewHelper.SizeChangedCallBack sizeChangedCallBack) {

        if (sizeChangedCallBack != null && mRecyclerViewHelper != null) {
            mRecyclerViewHelper.setSizeChangedCallBack(sizeChangedCallBack);
        }
    }

    public List<FastModel> getTopicStructure() {
        if (mRecyclerViewHelper != null) {
            return mRecyclerViewHelper.mList;
        }
        return null;
    }

    public void fillData(List<FastModel> data) {
        if (mRecyclerViewHelper != null) {
            mRecyclerViewHelper.init(data);
        }
    }


    public class CallBack extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if (!dragEnable) return false;
            boolean can = canDrag(child);
            if (can) isDraging = true;
            mDragView = child;
            mRecyclerViewHelper.reset();
            if (can) {
                child.setBackgroundColor(Color.parseColor("#f7f7f9"));
                bringChildToFront(child);
                initRxSubject();
            }
            mDragLocation[0] = DEFAULT;
            mDragLocation[1] = DEFAULT;
            return can;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int leftBound = getPaddingLeft();
            final int rightBound = getWidth() - leftBound - child.getMeasuredWidth();

            final int newLeft = Math.min(Math.max(left, leftBound), rightBound);

            return newLeft;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return top;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mDragLocation[0] = left;
            mDragLocation[1] = top;

            if (!isDraging) return;

            if (left < (getWidth() / 3 - changedView.getWidth() / 2)) {
                mRecyclerViewHelper.cancel();
            } else {
                int[] ints = new int[4];
                ints[0] = left;
                ints[1] = top;
                ints[2] = changedView.getBottom();
                ints[3] = (int) changedView.getTag();
                if (mPublishSubject != null) {
                    mPublishSubject.onNext(ints);
                } else {
                    mRecyclerViewHelper.draging(ints);
                }
            }

        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            isDraging = false;
            if (mPublishSubject != null)
                mPublishSubject.onComplete();
            if (releasedChild.getTag() != null) {
                int[] ints = new int[4];
                ints[0] = releasedChild.getLeft();
                ints[1] = releasedChild.getTop();
                ints[2] = releasedChild.getBottom();
                ints[3] = (int) releasedChild.getTag();
                if (ints[0] < (getWidth() / 3 - releasedChild.getWidth() / 2)) {
                    mRecyclerViewHelper.cancel();
                } else {
                    mRecyclerViewHelper.dragFinish(ints[3]);
                }
                int tag = (int) releasedChild.getTag();
                if (mDragView != null) {
                    mDragView.setVisibility(INVISIBLE);
                }

                mViewDragHelper.settleCapturedViewAt(LayoutHelper.locations.get(tag)[0], LayoutHelper.locations.get(tag)[1]);
                invalidate();
            }
        }
    }

    private void initRxSubject() {

        //通过throttleLast做延迟，防止拖动的时候闪的太快
        mPublishSubject = PublishSubject.create();
        mPublishSubject.throttleLast(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<int[]>() {
                    @Override
                    public void accept(int[] ints) throws Exception {
                        if (mRecyclerViewHelper != null)
                            mRecyclerViewHelper.draging(ints);

                    }
                });
    }

    private View mDragView;
    public static boolean isDraging; //是否正在拖拽
    private int[] mDragLocation = new int[2];

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            invalidate();
        } else {
            if (mDragView != null && mDragView.getVisibility() != View.VISIBLE) {
                mDragView.setVisibility(VISIBLE);
                mDragView.setBackgroundColor(Color.parseColor("#ffffff"));
                if (mRecyclerViewHelper != null) {
                    mRecyclerViewHelper.cancel();
                }
            }
        }
    }

    private boolean canDrag(View child) {

        if (child == view1[1]) return true;
        if (child == view2[1]) return true;
        if (child == view3[1]) return true;
        return false;
    }

    public String getSubjectTitle() {
        if (mRecyclerViewHelper != null) {
            return mRecyclerViewHelper.mSubjectTitle;
        }
        return RecyclerViewHelper.getSubjectTitle();
    }


    //必须从0开始
    public static final int T_XUANZE = 0;
    public static final int T_PANDUAN = 1;
    public static final int T_ZHUGUAN = 2;
    public static final int T_TITLE = 3;
    public static final int T_EMPTY = 4;


}
