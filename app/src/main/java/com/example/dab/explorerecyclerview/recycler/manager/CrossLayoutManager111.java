package com.example.dab.explorerecyclerview.recycler.manager;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dab on 2017/7/14.
 */

public class CrossLayoutManager111 extends RecyclerView.LayoutManager {
    private static final String TAG = "CrossLayoutManager";
    private SparseArray<Rect> mItemRects;//key 是View的position，保存View的bounds 和 显示标志，
    private int mFirstVisiPos;//屏幕可见的第一个View的Position
    private int mLastVisiPos;//屏幕可见的最后一个View的Position
    private int mVerticalOffset;

    public CrossLayoutManager111() {
        setAutoMeasureEnabled(true);
        mItemRects = new SparseArray<>();
    }

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
        mLastVisiPos = getItemCount();
        //初始化时调用 填充childView
        layout(recycler);
//        layout(recycler,state);
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
        //边界修复代码
        if (mVerticalOffset + realOffset < 0) {//上边界
            realOffset = -mVerticalOffset;
        } else if (realOffset > 0) {//下边界
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

        realOffset = layout(recycler, realOffset);//先填充，再位移。

        mVerticalOffset += realOffset;//累加实际滑动距离

        offsetChildrenVertical(-realOffset);//滑动

        return realOffset;
    }


    private void layout(RecyclerView.Recycler recycler) {
        layout(recycler, 0);
    }

    private int layout(RecyclerView.Recycler recycler, int dy) {
        int topOffset = getPaddingTop();//布局时的上偏移
        //回收越界子View
        if (getChildCount() > 0) {//滑动时进来的
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (dy > 0) {//需要回收当前屏幕，上越界的View
                    if (getDecoratedBottom(child) - dy < topOffset) {
//                        removeAndRecycleView(child, recycler);
                        mFirstVisiPos++;

                    }
                } else if (dy < 0) {//回收当前屏幕，下越界的View
                    if (getDecoratedTop(child) - dy > getHeight() - getPaddingBottom()) {
//                        removeAndRecycleView(child, recycler);
                        mLastVisiPos--;

                    }
                }
            }
            //detachAndScrapAttachedViews(recycler);
        }


        int leftOffset = getPaddingLeft();//布局时的左偏移
        int lineMaxHeight = 0;//每一行最大的高度
        int horizontalSpace = getHorizontalSpace();

        if (dy >= 0) {
            int minPos = mFirstVisiPos;
            mLastVisiPos = getItemCount() - 1;
            if (getChildCount() > 0) {
                View lastView = getChildAt(getChildCount() - 1);
                minPos = getPosition(lastView) + 1;//从最后一个View+1开始吧
                topOffset = getDecoratedTop(lastView);
                leftOffset = getDecoratedRight(lastView);
                lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(lastView));
            }
            for (int i = minPos; i <= mLastVisiPos; i++) {
                View child = recycler.getViewForPosition(i);
                addView(child);
                measureChildWithMargins(child, 0, 0);
                int decoratedMeasurementVertical = getDecoratedMeasurementVertical(child);
                int decoratedMeasurementHorizontal = getDecoratedMeasurementHorizontal(child);
                //当前列可以排列下当前的item
                if (leftOffset + decoratedMeasurementHorizontal <= horizontalSpace) {

                    if ((leftOffset / decoratedMeasurementHorizontal) % 2 == 1) {
                        //如果是列数为单数,需要向下平移item的一半
                        layoutDecoratedWithMargins(child, leftOffset, topOffset + decoratedMeasurementVertical / 2, leftOffset + decoratedMeasurementHorizontal, topOffset + decoratedMeasurementVertical + decoratedMeasurementVertical / 2);
                        Rect rect = new Rect(leftOffset, topOffset + mVerticalOffset+ decoratedMeasurementVertical / 2, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + decoratedMeasurementVertical / 2+ getDecoratedMeasurementVertical(child) + mVerticalOffset);
                        mItemRects.put(i, rect);
                    } else {
                        layoutDecoratedWithMargins(child, leftOffset, topOffset, leftOffset + decoratedMeasurementHorizontal, topOffset + decoratedMeasurementVertical);
                        //保存Rect供逆序layout用
                        Rect rect = new Rect(leftOffset, topOffset + mVerticalOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child) + mVerticalOffset);
                        mItemRects.put(i, rect);
                    }


                    leftOffset += decoratedMeasurementHorizontal;
                    lineMaxHeight = Math.max(lineMaxHeight, decoratedMeasurementVertical);
                } else {
                    //当前行排列不下
                    leftOffset = getPaddingLeft();
                    topOffset += lineMaxHeight;
                    lineMaxHeight = 0;
                    //新起一行的时候要判断一下边界
                    if (topOffset > getHeight() - getPaddingBottom()) {
                        //越界了 就回收
                        removeAndRecycleView(child, recycler);
                        mLastVisiPos = i - 1;
                    } else {
                        layoutDecoratedWithMargins(child, leftOffset, topOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child));

                        //保存Rect供逆序layout用
                        Rect rect = new Rect(leftOffset, topOffset + mVerticalOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child) + mVerticalOffset);
                        mItemRects.put(i, rect);

                        //改变 left  lineHeight
                        leftOffset += getDecoratedMeasurementHorizontal(child);
                        lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
                    }
                }
            }
            //添加完后，判断是否已经没有更多的ItemView，并且此时屏幕仍有空白，则需要修正dy
            View lastChild = getChildAt(getChildCount() - 1);
            if (getPosition(lastChild) == getItemCount() - 1) {
                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
                if (gap > 0) {
                    dy -= gap;
                }
            }


        } else {
            int maxPos = getItemCount() - 1;
            mFirstVisiPos = 0;
            if (getChildCount() > 0) {
                View firstView = getChildAt(0);
                maxPos = getPosition(firstView) - 1;
            }
            for (int i = maxPos; i >= mFirstVisiPos; i--) {
                Rect rect = mItemRects.get(i);

                if (rect.bottom - mVerticalOffset - dy < getPaddingTop()) {
                    mFirstVisiPos = i + 1;
                    break;
                } else {
                    View child = recycler.getViewForPosition(i);
                    addView(child, 0);//将View添加至RecyclerView中，childIndex为1，但是View的位置还是由layout的位置决定
                    measureChildWithMargins(child, 0, 0);

                    layoutDecoratedWithMargins(child, rect.left, rect.top - mVerticalOffset, rect.right, rect.bottom - mVerticalOffset);
                }
            }

        }


        return dy;
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
