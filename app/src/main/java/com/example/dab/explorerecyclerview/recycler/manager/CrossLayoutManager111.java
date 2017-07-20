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
    private int mFirstVisiblePos;//屏幕可见的第一个View的Position
    private int mLastVisiblePos;//屏幕可见的最后一个View的Position
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
        mFirstVisiblePos = 0;
        mLastVisiblePos = getItemCount()-1;
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
//        //边界修复代码
        if (mVerticalOffset + realOffset < 0) {//上边界
            realOffset = -mVerticalOffset;
        } else if (realOffset > 0) {//下边界
            //利用最后一个子View比较修正
            View lastChild = getChildAt(getChildCount() - 1);
            if (getPosition(lastChild) == getItemCount() - 1) {
                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
                if (gap > 0) {
                    realOffset = 0;
                } else if (gap == 0) {
                    realOffset = 0;
                } else {
                    realOffset = Math.min(realOffset, -gap);
                }
            }
        }

         layout(recycler, realOffset);//先填充，再位移。

        mVerticalOffset += realOffset;//累加实际滑动距离

        offsetChildrenVertical(-realOffset);//滑动

        return realOffset;
    }


    private void layout(RecyclerView.Recycler recycler) {
        layout(recycler, 0);
    }

    private void layout(RecyclerView.Recycler recycler, int dy) {
        int topOffset = getPaddingTop();//布局时的上偏移
        //回收越界子View
        if (getChildCount() > 0) {//滑动时进来的
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (dy > 0) {//需要回收当前屏幕，上越界的View
                    if (getDecoratedBottom(child) - dy < topOffset) {
//                        removeAndRecycleView(child, recycler);
                        mFirstVisiblePos++;

                    }
                } else if (dy < 0) {//回收当前屏幕，下越界的View
                    if (getDecoratedTop(child) - dy > getHeight() - getPaddingBottom()) {
//                        removeAndRecycleView(child, recycler);
                        mLastVisiblePos--;
                    }
                }
            }
        }


        int leftOffset = getPaddingLeft();//布局时的左偏移
        int lineMaxHeight = 0;//每一行最大的高度
        int rightOffset=0;
        int bottomOffset=0;
        int horizontalSpace = getHorizontalSpace();
        int columnNumber = 0;//列数
        int lineNumber = 0;//行数
        if (dy >= 0) {
            int minPos = mFirstVisiblePos;
            mLastVisiblePos = getItemCount() - 1;
            if (getChildCount() > 0) {
                View lastView = getChildAt(getChildCount() - 1);
                minPos = getPosition(lastView) + 1;//从最后一个View+1开始吧
                topOffset = getDecoratedTop(lastView);
                leftOffset = getDecoratedRight(lastView);
                lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(lastView));
            }

            for (int i = minPos; i <= mLastVisiblePos; i++) {
                View child = recycler.getViewForPosition(i);
                addView(child);
                measureChildWithMargins(child, 0, 0);
                int decoratedMeasurementVertical = getDecoratedMeasurementVertical(child);
                int decoratedMeasurementHorizontal = getDecoratedMeasurementHorizontal(child);
                int itemRight = leftOffset + decoratedMeasurementHorizontal;
                if (i == 0) {
                    topOffset=  topOffset + decoratedMeasurementVertical / 2;
                }
                //如果是单行,向右移动一个位置
                if (columnNumber % 2 == 1) {
                    leftOffset=leftOffset + decoratedMeasurementHorizontal;
                    itemRight = leftOffset;
                }

                if (columnNumber == 2) {
                    leftOffset = leftOffset - decoratedMeasurementHorizontal - decoratedMeasurementHorizontal ;
                    itemRight = leftOffset;
                    topOffset = topOffset + decoratedMeasurementVertical / 2;
                    lineMaxHeight = lineMaxHeight + decoratedMeasurementVertical / 2;
                }
                if (columnNumber > 2) {
                    leftOffset = leftOffset + decoratedMeasurementHorizontal + decoratedMeasurementHorizontal;
                    itemRight = leftOffset;
                    topOffset = topOffset - decoratedMeasurementVertical / 2;
                }
                if (i % 3 == 0) {
                    itemRight = horizontalSpace + 1;
                }
                //当前列可以排列下当前的item
                if (itemRight <= horizontalSpace) {
                    rightOffset = leftOffset + decoratedMeasurementHorizontal;
                    bottomOffset = topOffset + decoratedMeasurementVertical;

                    layoutDecoratedWithMargins(child, leftOffset, topOffset, rightOffset, bottomOffset);
                    //保存Rect供逆序layout用
                    Rect rect = new Rect(leftOffset, topOffset + mVerticalOffset, rightOffset, bottomOffset + mVerticalOffset);
                    mItemRects.put(i, rect);
                    columnNumber++;
                    leftOffset += decoratedMeasurementHorizontal;
                    lineMaxHeight = Math.max(lineMaxHeight, decoratedMeasurementVertical);
                } else {
                    //当前行排列不下
                    leftOffset = getPaddingLeft();
                    topOffset += lineMaxHeight-decoratedMeasurementVertical / 2;
                    lineMaxHeight = 0;
                    columnNumber = 0;
                    //新起一行的时候要判断一下边界
                    if (topOffset > getHeight() - getPaddingBottom()) {
                        //越界了 就回收
                        removeAndRecycleView(child, recycler);
                        mLastVisiblePos = i - 1;
                    } else {
                        lineNumber++;
                        layoutDecoratedWithMargins(child, leftOffset, topOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child));

                        //保存Rect供逆序layout用
                        Rect rect = new Rect(leftOffset, topOffset + mVerticalOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child) + mVerticalOffset);
                        mItemRects.put(i, rect);
                        columnNumber++;
                        //改变 left  lineHeight
                        leftOffset += getDecoratedMeasurementHorizontal(child);
                        lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
                    }
                }
            }
        } else {
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
                    addView(child, 0);
                    measureChildWithMargins(child, 0, 0);
                    layoutDecoratedWithMargins(child, rect.left, rect.top - mVerticalOffset, rect.right, rect.bottom - mVerticalOffset);
                }
            }
        }
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
