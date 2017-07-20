package com.example.dab.explorerecyclerview.recycler.manager;

import android.graphics.Rect;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class NyLayoutManager extends RecyclerView.LayoutManager {
  
    private static final String TAG = "lzy";  
    //保存所有item的偏移信息  
    private SparseArrayCompat<Rect> itemFrames = new SparseArrayCompat<>();
    //总的高度和宽度  
    private int mTotalHeight;  
    private int mTotalWidth;  
  
    private int verticalOffset;//竖直方向的偏移  
    private int horizontalOffset;//水平方向的偏移  
  
    @Override  
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {  
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);  
    }  
  
  
    @Override  
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {  
        if (getItemCount() <= 0) {  
            return;  
        }
        detachAndScrapAttachedViews(recycler);

        int totalHeight = 0;  
        int totalWidth = 0;  
        int offsetX = 0;  
        int offsetY = 0;  
        //计算每个item的位置信息,存储在itemFrames里面  
        for (int i = 0; i < getItemCount(); i++) {  
            //从缓存中取出  
            View view = recycler.getViewForPosition(i);
            //添加到RecyclerView中  
            addView(view);  
            //测量  
            measureChildWithMargins(view, 0, 0);  
            //获取测量后的宽高  
            int height = getDecoratedMeasuredHeight(view);  
            int width = getDecoratedMeasuredWidth(view);  
            //把每一个子View的宽高加起来获得总的  
            totalHeight += height;  
            totalWidth += width;  
            //边界信息保存到Rect里面  
            Rect rect = itemFrames.get(i);  
            if (rect == null) {  
                rect = new Rect();  
            }  
  
            rect.set(offsetX, offsetY, offsetX + width, offsetY + height);  
            itemFrames.put(i, rect);  
            //横竖方向的偏移  
            offsetX += width;  
            offsetY += height;  
        }  
        mTotalHeight = Math.max(totalHeight, getVerticalSpace());  
        mTotalWidth = Math.max(totalWidth, getHorizontalSpace());  
  
        fill(recycler, state);  
  
    }  
  
    //回收不必要的view（超出屏幕的），取出需要的显示出来  
    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {  
        //获得屏幕的边界信息  
        Rect displayFrame = new Rect(horizontalOffset, verticalOffset, horizontalOffset + getHorizontalSpace(),  
                verticalOffset + getVerticalSpace());  
  
        //滑出屏幕回收到缓存中  
        Rect childFrame = new Rect();  
        for (int i = 0; i < getChildCount(); i++) {  
            View view = getChildAt(i);  
            childFrame.left = getDecoratedLeft(view);  
            childFrame.top = getDecoratedTop(view);  
            childFrame.right = getDecoratedRight(view);  
            childFrame.bottom = getDecoratedBottom(view);  
            //判断是否在显示区域里面  
            if (!Rect.intersects(displayFrame, childFrame)) {  
                removeAndRecycleView(view, recycler);  
            }  
        }  
        //在屏幕上显示出  
        for (int i = 0; i < getItemCount(); i++) {  
            if (Rect.intersects(displayFrame, itemFrames.get(i))) {//判断是否在屏幕中  
                View view = recycler.getViewForPosition(i);  
                measureChildWithMargins(view, 0, 0);  
                addView(view);  
                Rect rect = itemFrames.get(i);  
                layoutDecorated(view, rect.left - horizontalOffset, rect.top - verticalOffset,  
                        rect.right - horizontalOffset, rect.bottom - verticalOffset);  
            }  
        }  
  
  
    }  
  
    @Override  
    public boolean canScrollVertically() {  
        return true;  
    }  
  
    @Override  
    public boolean canScrollHorizontally() {  
        return true;  
    }  
  
  
    @Override  
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {  
        detachAndScrapAttachedViews(recycler);  
        if (verticalOffset + dy < 0) {//滑动到最顶部  
            dy = -verticalOffset;  
        } else if (verticalOffset + dy > mTotalHeight - getVerticalSpace()) {//滑动到底部  
            dy = mTotalHeight - getVerticalSpace() - verticalOffset;  
        }  
  
        offsetChildrenVertical(-dy);  
        fill(recycler, state);  
        verticalOffset += dy;  
        return dy;  
    }  
  
    @Override  
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {  
        detachAndScrapAttachedViews(recycler);  
        if (horizontalOffset + dx < 0) {//滑动到最左边  
            dx = -horizontalOffset;  
        } else if (horizontalOffset + dx > mTotalWidth - getHorizontalSpace()) {//滑动到最右边  
            dx = mTotalWidth - getHorizontalSpace() - horizontalOffset;  
        }  
  
        offsetChildrenHorizontal(-dx);  
        fill(recycler, state);  
        horizontalOffset += dx;  
        return dx;  
    }  
  
    //获取控件的竖直高度  
    private int getVerticalSpace() {  
        return getHeight() - getPaddingBottom() - getPaddingTop();  
    }  
  
    //获取控件的水平宽度  
    private int getHorizontalSpace() {  
        return getWidth() - getPaddingLeft() - getPaddingRight();  
    }  
}  