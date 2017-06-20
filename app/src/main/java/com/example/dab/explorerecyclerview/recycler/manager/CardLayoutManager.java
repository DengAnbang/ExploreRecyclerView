package com.example.dab.explorerecyclerview.recycler.manager;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dab on 2017/6/15.
 */

public class CardLayoutManager extends RecyclerView.LayoutManager{
    private static final String TAG = "CardLayoutManager";
    private boolean isDebug = true;
    private int mFirstVisiPos;//屏幕可见的第一个View的Position
    private int mLastVisiPos;//屏幕可见的最后一个View的Position
    private int mVerticalOffset;//竖直偏移量 每次换行时，要根据这个offset判断
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        if (getItemCount() == 0) {
            return;
        }
        mFirstVisiPos = 0;
//        mLastVisiPos = getItemCount();
        //初始化时调用 填充childView
        layout(recycler, state);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //位移0、没有子View 当然不移动
        if (dy == 0 || getChildCount() == 0) {
            return 0;
        }
        int realOffset = dy;//实际滑动的距离， 可能会在边界处被修复
        if (mVerticalOffset + realOffset < 0) {//上边界
            realOffset = -mVerticalOffset;
        }else if (realOffset > 0) {//下边界
            //利用最后一个子View比较修正
            View lastChild = getChildAt(getChildCount() - 1);
            if (getPosition(lastChild) == getItemCount() - 1) {
                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
                if (gap > 0) {
                    realOffset = -gap;
                } else if (gap == 0) {
                    realOffset = 0;
                } else {
                    realOffset = Math.min(realOffset, -gap);
                }
            }
        }
        realOffset = layout(recycler, state, realOffset);//先填充，再位移。

        mVerticalOffset += realOffset;//累加实际滑动距离

        offsetChildrenVertical(-realOffset);//滑动

        offsetChildrenVertical(-realOffset);
        return realOffset;
    }

    private int layout(RecyclerView.Recycler recycler, RecyclerView.State state, int realOffset) {
        int topOffset = getPaddingTop();//布局时的上偏移
        int leftOffset = getPaddingLeft();//布局时的左偏移
        int lineMaxHeight = 0;//每一行最大的高度
        int minPos = mFirstVisiPos;//初始化时，我们不清楚究竟要layout多少个子View，所以就假设从0~itemcount-1
        mLastVisiPos = getItemCount() - 1;
        for (int i = mFirstVisiPos; i <= mLastVisiPos; i++) {
            View child = recycler.getViewForPosition(i);
            addView(child);
            measureChildWithMargins(child, 0, 0);
            //当前行还排列的下
            int decoratedMeasurementHorizontal = getDecoratedMeasurementHorizontal(child);
            if (leftOffset + decoratedMeasurementHorizontal <= getHorizontalSpace()) {
                int decoratedMeasurementVertical = getDecoratedMeasurementVertical(child);
                layoutDecoratedWithMargins(child, leftOffset, topOffset, leftOffset + decoratedMeasurementHorizontal, topOffset + decoratedMeasurementVertical);
                leftOffset += decoratedMeasurementHorizontal;
                lineMaxHeight = Math.max(lineMaxHeight, decoratedMeasurementVertical);

            } else {
                //当前行排列不下
                leftOffset = getPaddingLeft();
                topOffset += lineMaxHeight;
                lineMaxHeight = 0;
                //新起一行的时候要判断一下边界
                if (topOffset-realOffset > getHeight() - getPaddingBottom()) {
                    //越界了 就回收
                    removeAndRecycleView(child, recycler);
                    mLastVisiPos = i - 1;
                } else {
                    layoutDecoratedWithMargins(child, leftOffset, topOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child));
                    //改变 left  lineHeight
                    leftOffset += getDecoratedMeasurementHorizontal(child);
                    lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
                }
            }

        }
        return realOffset;
    }

    private int layout(RecyclerView.Recycler recycler, RecyclerView.State state) {
        return layout(recycler, state,0);
    }

    /**
     * 获取某个childView在水平方向所占的空间
     *
     * @param view
     * @return
     */
    public int getDecoratedMeasurementHorizontal(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
    }
    /**
     * 获取某个childView在竖直方向所占的空间
     *
     * @param view
     * @return
     */
    public int getDecoratedMeasurementVertical(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredHeight(view) + params.topMargin
                + params.bottomMargin;
    }

    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }
    public int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }
}
