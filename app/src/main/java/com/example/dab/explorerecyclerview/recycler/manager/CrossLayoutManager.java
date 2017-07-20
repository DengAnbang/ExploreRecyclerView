package com.example.dab.explorerecyclerview.recycler.manager;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dab on 2017/7/14.
 */

public class CrossLayoutManager extends RecyclerView.LayoutManager {
    private static final String TAG = "CrossLayoutManager";
    private SparseArray<Rect> mItemRects;//key 是View的position，保存View的bounds 和 显示标志，
    //总的高度和宽度
    private int mTotalHeight;
    private int mTotalWidth;
    private int mVerticalOffset;//竖直方向的偏移
    private int mFirstVisiblePos;//屏幕可见的第一个View的Position
    private int mLastVisiblePos;//屏幕可见的最后一个View的Position
    public CrossLayoutManager() {
        setAutoMeasureEnabled(true);
        mItemRects = new SparseArray<>();
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

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
        offsetChildrenVertical(-dy);
//        detachAndScrapAttachedViews(recycler);
        fill(recycler, state,dy);
        mVerticalOffset += dy;
        return dy;
    }
//    @Override
//    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
//        //位移0、没有子View 当然不移动
//        if (dy == 0 || getChildCount() == 0) {
//            return 0;
//        }
//
//        int realOffset = dy;//实际滑动的距离， 可能会在边界处被修复
//        //边界修复代码
//        if (mVerticalOffset + realOffset < 0) {//上边界
//            realOffset = -mVerticalOffset;
//        } else if (realOffset > 0) {//下边界
//            //利用最后一个子View比较修正
//            View lastChild = getChildAt(getChildCount() - 1);
//            if (getPosition(lastChild) == getItemCount() - 1) {
//                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
//                if (gap > 0) {
//                    realOffset = -gap;
//                } else if (gap == 0) {
//                    realOffset = 0;
//                } else {
//                    realOffset = Math.min(realOffset, -gap);
//                }
//            }
//        }
//
//        realOffset = fill(recycler, state, realOffset);//先填充，再位移。
//
//        mVerticalOffset += realOffset;//累加实际滑动距离
//
//        offsetChildrenVertical(-realOffset);//滑动
//
//        return realOffset;
//    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() <= 0) {
            return;
        }
        detachAndScrapAttachedViews(recycler);
        mTotalHeight = getHeight() - getPaddingBottom();
        mTotalWidth = getWidth() - getPaddingRight();
        mFirstVisiblePos = 0;
        mLastVisiblePos = getItemCount() - 1;
        fill(recycler, state);
    }


    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        fill(recycler,state,0);
    }
    private int fill(RecyclerView.Recycler recycler, RecyclerView.State state,int dy) {
        int topOffset = getPaddingTop();//布局时的上偏移
        int leftOffset = getPaddingLeft();//布局时的左偏移
        int horizontalSpace = getHorizontalSpace();//每一行的空间
//        int lineMaxHeight = 0;//每一行最大的高度
//        int minPos = mFirstVisiblePos;
//        int maxPos = mLastVisiblePos;




        int childCount = getChildCount();//已经加载过的view
        if (childCount > 0) {//说明是滑动触发的
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (dy > 0) {//向下滑动
                    if (getDecoratedBottom(child) - dy < topOffset) {
                        removeAndRecycleView(child, recycler);
                        mFirstVisiblePos++;
                        continue;
                    }
                }
                if (dy < 0) {//往上滑动
                    if (getDecoratedTop(child) - dy > mTotalHeight) {
                        removeAndRecycleView(child, recycler);
                        mLastVisiblePos--;
                        continue;
                    }
                }
            }
        }

        if (dy >= 0) {//第一次布局或者下滑
            for (int i = mFirstVisiblePos; i < mLastVisiblePos; i++) {
                View child = recycler.getViewForPosition(i);
                addView(child);
                measureChildWithMargins(child, 0, 0);

                int decoratedMeasurementHorizontal = getDecoratedMeasurementHorizontal(child);
                int decoratedMeasurementVertical = getDecoratedMeasurementVertical(child);
                if (leftOffset + decoratedMeasurementHorizontal <= horizontalSpace) {
                    layoutDecoratedWithMargins(child, leftOffset, topOffset, leftOffset + decoratedMeasurementHorizontal, topOffset + decoratedMeasurementVertical);
                    Rect rect = new Rect(leftOffset, topOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child));
                    mItemRects.put(i, rect);
                    leftOffset = leftOffset + decoratedMeasurementHorizontal;
                } else {
                    leftOffset = getPaddingLeft();
                    topOffset = topOffset + decoratedMeasurementVertical;
                    if (topOffset - dy > mTotalHeight) {
                        //越界了 就回收
                        removeAndRecycleView(child, recycler);
                        mLastVisiblePos = i - 1;
                    } else {
                        layoutDecoratedWithMargins(child, leftOffset, topOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child));
                        //保存Rect供逆序layout用
                        Rect rect = new Rect(leftOffset, topOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child));
                        mItemRects.put(i, rect);
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
            //上滑
            int maxPos = getItemCount() - 1;
            mFirstVisiblePos = 0;
            if (getChildCount() > 0) {
                View firstView = getChildAt(0);
                maxPos = getPosition(firstView) - 1;
            }
            for (int i = maxPos; i >= mFirstVisiblePos; i--) {
                Rect rect = mItemRects.get(i);
                if (rect.bottom - mVerticalOffset - dy < getPaddingTop()) {
                    mFirstVisiblePos = i + 1;
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
