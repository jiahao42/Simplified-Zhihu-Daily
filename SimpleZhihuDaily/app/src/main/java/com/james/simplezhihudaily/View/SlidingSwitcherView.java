package com.james.simplezhihudaily.View;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.james.simplezhihudaily.R;

public class SlidingSwitcherView extends RelativeLayout implements View.OnTouchListener {

    /**
     * 让菜单滚动，手指滑动需要达到的速度。
     */
    public static final int SNAP_VELOCITY = 200;

    /**
     * SlidingSwitcherView的宽度。
     */
    private int switcherViewWidth;

    /**
     * 当前显示的元素的下标。
     */
    private int currentItemIndex;

    /**
     * 菜单中包含的元素总数。
     */
    private int itemsCount;

    /**
     * 各个元素的偏移边界值。
     */
    private int[] borders;

    /**
     * 最多可以滑动到的左边缘。值由菜单中包含的元素总数来定，marginLeft到达此值之后，不能再减少。
     *
     */
    private int leftEdge = 0;

    /**
     * 最多可以滑动到的右边缘。值恒为0，marginLeft到达此值之后，不能再增加。
     */
    private int rightEdge = 0;

    /**
     * 记录手指按下时的横坐标。
     */
    private float xDown;

    /**
     * 记录手指移动时的横坐标。
     */
    private float xMove;

    /**
     * 记录手机抬起时的横坐标。
     */
    private float xUp;

    /**
     * 菜单布局。
     */
    private LinearLayout itemsLayout;

    /**
     * 标签布局。
     */
    private LinearLayout dotsLayout;

    /**
     * 菜单中的第一个元素。
     */
    private View firstItem;

    /**
     * 菜单中第一个元素的布局，用于改变leftMargin的值，来决定当前显示的哪一个元素。
     */
    private MarginLayoutParams firstItemParams;

    /**
     * 用于计算手指滑动的速度。
     */
    private VelocityTracker mVelocityTracker;

    /**
     * 重写SlidingSwitcherView的构造函数，用于允许在XML中引用当前的自定义布局。
     *
     * @param context
     * @param attrs
     */
    public SlidingSwitcherView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 滚动到下一个元素。
     */
    public void scrollToNext() {
        new ScrollTask().execute(-20);
    }

    /**
     * 滚动到上一个元素。
     */
    public void scrollToPrevious() {
        new ScrollTask().execute(20);
    }

    /**
     * 在onLayout中重新设定菜单元素和标签元素的参数。
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            initializeItems();
            initializeDots();
        }
    }

    /**
     * 初始化菜单元素，为每一个子元素增加监听事件，并且改变所有子元素的宽度，让它们等于父元素的宽度。
     */
    private void initializeItems() {
        switcherViewWidth = getWidth();
        itemsLayout = (LinearLayout) getChildAt(0);
        itemsCount = itemsLayout.getChildCount();
        borders = new int[itemsCount];
        for (int i = 0; i < itemsCount; i++) {
            borders[i] = -i * switcherViewWidth;
            View item = itemsLayout.getChildAt(i);
            MarginLayoutParams params = (MarginLayoutParams) item.getLayoutParams();
            params.width = switcherViewWidth;
            item.setLayoutParams(params);
            item.setOnTouchListener(this);
        }
        leftEdge = borders[itemsCount - 1];
        firstItem = itemsLayout.getChildAt(0);
        firstItemParams = (MarginLayoutParams) firstItem.getLayoutParams();
    }

    /**
     * 初始化标签元素。
     */
    private void initializeDots() {
        dotsLayout = (LinearLayout) getChildAt(1);
        refreshDotsLayout();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        createVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时，记录按下时的横坐标
                xDown = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                // 手指移动时，对比按下时的横坐标，计算出移动的距离，来调整左侧布局的leftMargin值，从而显示和隐藏左侧布局
                xMove = event.getRawX();
                int distanceX = (int) (xMove - xDown) - (currentItemIndex * switcherViewWidth);
                firstItemParams.leftMargin = distanceX;
                if (beAbleToScroll()) {
                    firstItem.setLayoutParams(firstItemParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                // 手指抬起时，进行判断当前手势的意图，从而决定是滚动到左侧布局，还是滚动到右侧布局
                xUp = event.getRawX();
                if (beAbleToScroll()) {
                    if (wantScrollToPrevious()) {
                        if (shouldScrollToPrevious()) {
                            currentItemIndex--;
                            scrollToPrevious();
                            refreshDotsLayout();
                        } else {
                            scrollToNext();
                        }
                    } else if (wantScrollToNext()) {
                        if (shouldScrollToNext()) {
                            currentItemIndex++;
                            scrollToNext();
                            refreshDotsLayout();
                        } else {
                            scrollToPrevious();
                        }
                    }
                }
                recycleVelocityTracker();
                break;
        }
        return false;
    }

    /**
     * 当前是否能够滚动，滚动到第一个或最后一个元素时将不能再滚动。
     *
     * @return 当前leftMargin的值在leftEdge和rightEdge之间返回true,否则返回false。
     */
    private boolean beAbleToScroll() {
        return firstItemParams.leftMargin < rightEdge && firstItemParams.leftMargin > leftEdge;
    }

    /**
     * 判断当前手势的意图是不是想滚动到上一个菜单元素。如果手指移动的距离是正数，则认为当前手势是想要滚动到上一个菜单元素。
     *
     * @return 当前手势想滚动到上一个菜单元素返回true，否则返回false。
     */
    private boolean wantScrollToPrevious() {
        return xUp - xDown > 0;
    }

    /**
     * 判断当前手势的意图是不是想滚动到下一个菜单元素。如果手指移动的距离是负数，则认为当前手势是想要滚动到下一个菜单元素。
     *
     * @return 当前手势想滚动到下一个菜单元素返回true，否则返回false。
     */
    private boolean wantScrollToNext() {
        return xUp - xDown < 0;
    }

    /**
     * 判断是否应该滚动到下一个菜单元素。如果手指移动距离大于屏幕的1/2，或者手指移动速度大于SNAP_VELOCITY，
     * 就认为应该滚动到下一个菜单元素。
     *
     * @return 如果应该滚动到下一个菜单元素返回true，否则返回false。
     */
    private boolean shouldScrollToNext() {
        return xDown - xUp > switcherViewWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    /**
     * 判断是否应该滚动到上一个菜单元素。如果手指移动距离大于屏幕的1/2，或者手指移动速度大于SNAP_VELOCITY，
     * 就认为应该滚动到上一个菜单元素。
     *
     * @return 如果应该滚动到上一个菜单元素返回true，否则返回false。
     */
    private boolean shouldScrollToPrevious() {
        return xUp - xDown > switcherViewWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    /**
     * 刷新标签元素布局，每次currentItemIndex值改变的时候都应该进行刷新。
     */
    private void refreshDotsLayout() {
        dotsLayout.removeAllViews();
        for (int i = 0; i < itemsCount; i++) {
            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(0,
                    LayoutParams.MATCH_PARENT);
            linearParams.weight = 1;
            RelativeLayout relativeLayout = new RelativeLayout(getContext());
            ImageView image = new ImageView(getContext());
            if (i == currentItemIndex) {
                image.setBackgroundResource(R.drawable.dot_selected);
            } else {
                image.setBackgroundResource(R.drawable.dot_unselected);
            }
            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            relativeLayout.addView(image, relativeParams);
            dotsLayout.addView(relativeLayout, linearParams);
        }
    }

    /**
     * 创建VelocityTracker对象，并将触摸事件加入到VelocityTracker当中。
     *
     * @param event
     *            右侧布局监听控件的滑动事件
     */
    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 获取手指在右侧布局的监听View上的滑动速度。
     *
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。
     */
    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

    /**
     * 回收VelocityTracker对象。
     */
    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    /**
     * 检测菜单滚动时，是否有穿越border，border的值都存储在{@link #borders}中。
     *
     * @param leftMargin
     *            第一个元素的左偏移值
     * @param speed
     *            滚动的速度，正数说明向右滚动，负数说明向左滚动。
     * @return 穿越任何一个border了返回true，否则返回false。
     */
    private boolean isCrossBorder(int leftMargin, int speed) {
        for (int border : borders) {
            if (speed > 0) {
                if (leftMargin >= border && leftMargin - speed < border) {
                    return true;
                }
            } else {
                if (leftMargin <= border && leftMargin - speed > border) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 找到离当前的leftMargin最近的一个border值。
     *
     * @param leftMargin
     *            第一个元素的左偏移值
     * @return 离当前的leftMargin最近的一个border值。
     */
    private int findClosestBorder(int leftMargin) {
        int absLeftMargin = Math.abs(leftMargin);
        int closestBorder = borders[0];
        int closestMargin = Math.abs(Math.abs(closestBorder) - absLeftMargin);
        for (int border : borders) {
            int margin = Math.abs(Math.abs(border) - absLeftMargin);
            if (margin < closestMargin) {
                closestBorder = border;
                closestMargin = margin;
            }
        }
        return closestBorder;
    }

    class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... speed) {
            int leftMargin = firstItemParams.leftMargin;
            // 根据传入的速度来滚动界面，当滚动穿越border时，跳出循环。
            while (true) {
                leftMargin = leftMargin + speed[0];
                if (isCrossBorder(leftMargin, speed[0])) {
                    leftMargin = findClosestBorder(leftMargin);
                    break;
                }
                publishProgress(leftMargin);
                // 为了要有滚动效果产生，每次循环使线程睡眠10毫秒，这样肉眼才能够看到滚动动画。
                sleep(10);
            }
            return leftMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... leftMargin) {
            firstItemParams.leftMargin = leftMargin[0];
            firstItem.setLayoutParams(firstItemParams);
        }

        @Override
        protected void onPostExecute(Integer leftMargin) {
            firstItemParams.leftMargin = leftMargin;
            firstItem.setLayoutParams(firstItemParams);
        }
    }

    /**
     * 使当前线程睡眠指定的毫秒数。
     *
     * @param millis
     *            指定当前线程睡眠多久，以毫秒为单位
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}