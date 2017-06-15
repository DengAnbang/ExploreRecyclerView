package com.example.dab.explorerecyclerview.recycler.decoration;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.example.dab.explorerecyclerview.recycler.decoration.SuspendDecoration.TitleGravity.CENTER;
import static com.example.dab.explorerecyclerview.recycler.decoration.SuspendDecoration.TitleGravity.LEFT;
import static com.example.dab.explorerecyclerview.recycler.decoration.SuspendDecoration.TitleGravity.RIGHT;


/**
 * Created by DAB on 2016/12/29 13:25.
 * 自定义的RecyclerView的ItemDecoration,实现挤动悬浮的效果
 */

public abstract class SuspendDecoration extends RecyclerView.ItemDecoration {
    @IntDef({CENTER, LEFT, RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TitleGravity {
        int CENTER = 0;
        int LEFT = 1;
        int RIGHT = 2;
    }

    private static final String TAG = "SuspendDecoration";

    private int mTitleHeight=100;//悬浮窗的高度
    private Paint mBackgroundPaint;//悬浮窗的画笔
    private TextPaint mTextPaint;//悬浮窗内容的画笔
    @TitleGravity
    private int mTitleGravity=TitleGravity.LEFT;//悬浮窗的内容显示位置(左,中,右)
    private int mTextOffsetX, mTextOffsetY;

    private Rect mRect;//悬浮窗的的矩形(动态改变的)

    public SuspendDecoration setTitleHeight(int titleHeight) {
        mTitleHeight = titleHeight;
        return this;
    }

    public SuspendDecoration setTitleSize(int titleSize) {
        mTextPaint.setTextSize(titleSize);
        return this;
    }

    public SuspendDecoration setTitleGravity(@TitleGravity int titleGravity) {
        mTitleGravity = titleGravity;
        return this;
    }

    /**
     * @param titleHeight  悬浮窗的的高度
     * @param titleSize    悬浮窗的字体大小
     * @param titleGravity 悬浮窗的显示位置
     */
    public SuspendDecoration(int titleHeight, int titleSize, @TitleGravity int titleGravity) {
        mTitleHeight = titleHeight;
        mTitleGravity = titleGravity;
        mBackgroundPaint = new Paint();
//        mBackgroundPaint.setColor(Color.GREEN);
        //设置悬浮栏中文本的画笔
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(titleSize);
        mTextPaint.setColor(Color.DKGRAY);
        mRect = new Rect();
    }


    public SuspendDecoration() {
        mTitleHeight = 100;
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.GREEN);
        //设置悬浮栏中文本的画笔
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(30);
        mTextPaint.setColor(Color.DKGRAY);
        mRect = new Rect();
        init();
    }

    private void init() {

    }
    /**
     * 设置字体的颜色
     *
     * @param color
     */
    public SuspendDecoration setTextColor(int color) {
        mTextPaint.setColor(color);
        return this;
    }

    /**
     * 设置文字在X轴的偏移
     *
     * @param textOffsetX
     */
    public SuspendDecoration setTextOffsetX(int textOffsetX) {
        mTextOffsetX = textOffsetX;
        return this;
    }

    /**
     * 设置文字在Y轴的偏移
     *
     * @param textOffsetY
     */
    public SuspendDecoration setTextOffsetY(int textOffsetY) {
        mTextOffsetY = textOffsetY;
        return this;
    }

    /**
     * 设置背景的颜色
     *
     * @param color
     */
    public SuspendDecoration setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
        return this;
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        canvas.save();
        int firstVisibleItemPosition= getFirstVisibleItemPosition(parent);
        int lastVisibleItemPosition= getLastVisibleItemPosition(parent);
        for (int i = firstVisibleItemPosition; i < lastVisibleItemPosition; i++) {
            View child = parent.getLayoutManager().findViewByPosition(i);
            //判断是否需要显示悬浮窗
            try {
                if (shouldShowTitle(i)) {
                    //悬浮窗的矩形边框
                    mRect.set(child.getLeft(), child.getTop() - mTitleHeight, child.getRight(), child.getBottom() - child.getHeight());
                    canvas.drawRect(mRect, mBackgroundPaint);
                    //悬浮窗的内容
                    try {
                        canvas.drawText(showTitle(i), getBaseLineX(mRect), getBaseLineY(mRect), mTextPaint);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        canvas.restore();
    }

    public int getLastVisibleItemPosition(RecyclerView parent) {
        int lastVisibleItemPosition=-1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
        }
        if (lastVisibleItemPosition == -1) {
            throw new IllegalArgumentException("需要重写getLastVisibleItemPosition()方法,提供最后一个可见item的Position");
        }
        return lastVisibleItemPosition;
    }

    public int getFirstVisibleItemPosition(RecyclerView parent) {
        int firstVisibleItemPosition=-1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }
        if (firstVisibleItemPosition == -1) {
            throw new IllegalArgumentException("需要重写getFirstVisibleItemPosition()方法,提供第一个可见item的Position");
        }
        return firstVisibleItemPosition;
    }

    //是否需要显示悬浮窗
    private boolean shouldShowTitle(int position) throws Exception {
        if (position == 0) return true;
        boolean sameGroup = isSameGroup(position - 1, position);
//        if (MainActivity.isDebug) Log.e(TAG, "shouldShowTitle: "+position + "***" + sameGroup);
        if (sameGroup) return false;

        return true;


//        return position >= 0 && (position == 0 || !isSameGroup(position - 1, position));
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        //拿到当前的position
        int pos = parent.getChildAdapterPosition(view);
        //判断是否需要给这个position预留显示悬浮窗的位置
        try {
            if (shouldShowTitle(pos)) {
                outRect.top = mTitleHeight;
            } else {
                outRect.top = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);
        canvas.save();
        //初始化悬浮窗的高度
        float textY = mTitleHeight;
        int childCount = parent.getChildCount();
        if (childCount < 1) return;
        //通过获取布局管理器来找到当前显示的第一个position
//        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
        int firstVisibleItemPosition = getFirstVisibleItemPosition(parent);
        int lastVisibleItemPosition = getLastVisibleItemPosition(parent);

        for (int i = firstVisibleItemPosition; i < lastVisibleItemPosition; i++) {
            View child = parent.getLayoutManager().findViewByPosition(i);
            try {
                if (shouldShowTitle(i)) {
                    //判断当前悬浮窗是否需要被挤压,如果需要,就改变矩形的bottom
                    if (mTitleHeight < child.getTop() && child.getTop() < mTitleHeight * 2) {
                        textY = child.getTop() - mTitleHeight;
                    }
                    //不能依据当前的第一个来画悬浮
                    if (i == 0) continue;

                    mRect.set(0, (int) textY - mTitleHeight, child.getRight(), (int) textY);
                    canvas.drawRect(mRect, mBackgroundPaint);
                    try {
                        canvas.drawText(showTitle(firstVisibleItemPosition), getBaseLineX(mRect), getBaseLineY(mRect), mTextPaint);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //画一个悬浮就结束循环
                    canvas.restore();
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        View child = parent.getLayoutManager().findViewByPosition(firstVisibleItemPosition);
        mRect.set(0, 0, child.getRight(), (int) textY);
        canvas.drawRect(mRect, mBackgroundPaint);
        //悬浮窗的内容
        try {
            canvas.drawText(showTitle(firstVisibleItemPosition), getBaseLineX(mRect), getBaseLineY(mRect), mTextPaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
        canvas.restore();
    }

    /**
     * 决定文字绘制在X轴左,中,右
     *
     * @param rect
     * @return
     */
    private int getBaseLineX(Rect rect) {
        switch (mTitleGravity) {
            case RIGHT:
                mTextPaint.setTextAlign(Paint.Align.RIGHT);
                return rect.right + mTextOffsetX;
            case CENTER:
                mTextPaint.setTextAlign(Paint.Align.CENTER);
                return rect.centerX() + mTextOffsetX;
            default:
                mTextPaint.setTextAlign(Paint.Align.LEFT);
                return rect.left + mTextOffsetX;
        }

    }

    /**
     * 文字显示在Y轴居中
     *
     * @param rect
     * @return
     */
    private int getBaseLineY(Rect rect) {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
        return (int) (rect.centerY() - top / 2 - bottom / 2) + mTextOffsetY;
    }

    /**
     * 是否需要显示悬浮窗
     *
     * @param priorGroupId
     * @param nowGroupId
     * @return
     */
    public abstract boolean isSameGroup(int priorGroupId, int nowGroupId) throws Exception;

    /**
     * 悬浮窗的内容
     *
     * @param position
     * @return
     */
    public abstract String showTitle(int position) throws Exception;
}
