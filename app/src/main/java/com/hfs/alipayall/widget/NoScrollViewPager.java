package com.hfs.alipayall.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author HuangFusheng
 * @date 2019-11-12
 * description NoScrollViewPager
 */
public class NoScrollViewPager extends ViewPager {

    private boolean noScroll = false;
    //拦截viewpager下所有事件,禁止任何屏幕操作
    private boolean allEnable = true;

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollViewPager(Context context) {
        super(context);
    }

    public void setNoScroll(boolean noScroll) {
        this.noScroll = noScroll;
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        /* return false;//super.onTouchEvent(arg0); */
        if (noScroll)
            return false;
        else
            return super.onTouchEvent(arg0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (noScroll)
            return false;
        else
            return super.onInterceptTouchEvent(arg0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!allEnable)
            return true;

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
// ViewPager嵌套NoScrollViewPager时会调用canScroll()进而调用此方法判断child是否能横向滑动，不重写该方法会导致NoScrollViewPager不能翻页的同时、父ViewPager也不能翻页
        if (noScroll) {
            return false;
        }
        return super.canScrollHorizontally(direction);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
    }


    public boolean isAllEnable() {
        return allEnable;
    }

    public void setAllEnable(boolean allEnable) {
        this.allEnable = allEnable;
    }
}
