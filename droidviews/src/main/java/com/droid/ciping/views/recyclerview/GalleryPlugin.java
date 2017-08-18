package com.droid.ciping.views.recyclerview;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by dengciping on 2017/8/18.
 */

public class GalleryPlugin implements IPlugin {
    private RecyclerView mRecyclerView;

    //缩放效果
    private boolean needScale = true;

    //翻转效果
    private boolean needRotation = true;

    //翻转角度
    private int mMaxRotationAngle = 30;

    //缩放比例
    private float minScale = 0.8f;
    private float maxScale = 1.2f;

    private RecyclerView.OnScrollListener onScrollListener;
    private Rect rvRect;

    private GalleryPlugin() {
    }

    public static GalleryPlugin newBuild(RecyclerView recyclerView) {
        GalleryPlugin galleryPlugin = new GalleryPlugin();
        galleryPlugin.mRecyclerView = recyclerView;
        galleryPlugin.setDefaltMode();
        return galleryPlugin;
    }

    public GalleryPlugin NeedScale(boolean needScale) {
        this.needScale = needScale;
        return this;
    }

    public GalleryPlugin NeedRotation(boolean needRotation) {
        this.needRotation = needRotation;
        return this;
    }

    public GalleryPlugin setScale(float minScale, float maxScale) {
        this.minScale = minScale;
        this.maxScale = maxScale;
        return this;
    }

    public GalleryPlugin setmMaxRotationAngle(int mMaxRotationAngle) {
        this.mMaxRotationAngle = mMaxRotationAngle;
        return this;
    }

    public GalleryPlugin setStartMode() {
        StartSnapHelper linearSnapHelper = new StartSnapHelper();
        linearSnapHelper.attachToRecyclerView(mRecyclerView);
        return this;
    }

    public GalleryPlugin setDefaltMode() {
        GallerySnapHlper linearSnapHelper = new GallerySnapHlper();
        linearSnapHelper.attachToRecyclerView(mRecyclerView);
        return this;
    }

    //viewpager 模式 一次翻一页
    public GalleryPlugin setViewPagerMode() {
        PagerSnapHelper linearSnapHelper = new PagerSnapHelper();
        linearSnapHelper.attachToRecyclerView(mRecyclerView);
        return this;
    }

    public GalleryPlugin build() {
        attach(mRecyclerView);
        return this;
    }


    @Override
    public void attach(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;

        LinearLayoutManager layout = new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layout);

        onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    initSize();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                computeChild();
            }
        };

        recyclerView.addOnScrollListener(onScrollListener);

        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() / 2);
            }
        });
    }

    public void initSize() {
        rvRect = new Rect();
        mRecyclerView.getGlobalVisibleRect(rvRect);
    }

    private void computeChild() {
        if (rvRect == null) return;
        if (!needScale && !needRotation) return;

        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int childCount = layoutManager.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View view = layoutManager.getChildAt(i);

            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);

            int centerX = rvRect.centerX();
            int viewX = rect.centerX();

            float i3 = Math.abs(centerX - viewX);//view 离中心的距离

            if (needScale) {
                float p = 1 - i3 / centerX; //离中心位置的百分比 0->1
                scaleChild(view, p);
            }

            if (needRotation) {
                //开始还原翻转的距离
                int distance = rect.width();

                if (i3 > distance) {
                    //远离中心区域固定角度
                    rotationChild(view, mMaxRotationAngle, centerX > viewX);
                } else {
                    //靠近中心区域计算百分比
                    float p = i3 / distance;
                    rotationChild(view, mMaxRotationAngle * p, centerX > viewX);
                }
            }
        }
    }

    private void scaleChild(View view, float p) {
        float scale = minScale + (maxScale - minScale) * p;
        view.setScaleX(scale);
        view.setScaleY(scale);
    }

    private void rotationChild(View view, float angle, boolean isLeft) {
        view.setRotationY(isLeft ? angle : -angle);
    }

    /**
     * 滑动速度加快的LinearSnapHelper
     */
    public class GallerySnapHlper extends LinearSnapHelper {
        protected OrientationHelper mHorizontalHelper, mVerticalHelper;

        //SnapHelper中该值为100，这里改为40
        private static final float MILLISECONDS_PER_INCH = 40f;

        @Nullable
        protected LinearSmoothScroller createSnapScroller(final RecyclerView.LayoutManager layoutManager) {
            if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
                return null;
            }
            return new LinearSmoothScroller(mRecyclerView.getContext()) {
                @Override
                protected void onTargetFound(View targetView, RecyclerView.State state, RecyclerView.SmoothScroller.Action action) {
                    int[] snapDistances = calculateDistanceToFinalSnap(mRecyclerView.getLayoutManager(), targetView);
                    final int dx = snapDistances[0];
                    final int dy = snapDistances[1];
                    final int time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                    if (time > 0) {
                        action.update(dx, dy, time, mDecelerateInterpolator);
                    }
                }

                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                }
            };
        }

        protected OrientationHelper getHorizontalHelper(
                @NonNull RecyclerView.LayoutManager layoutManager) {
            if (mHorizontalHelper == null) {
                mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
            }
            return mHorizontalHelper;
        }

        protected OrientationHelper getVerticalHelper(RecyclerView.LayoutManager layoutManager) {
            if (mVerticalHelper == null) {
                mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
            }
            return mVerticalHelper;

        }

    }

    /**
     * 居左显示
     */
    public class StartSnapHelper extends GallerySnapHlper {

        @Nullable
        @Override
        public int[] calculateDistanceToFinalSnap(RecyclerView.LayoutManager layoutManager, View targetView) {
            int[] out = new int[2];
            if (layoutManager.canScrollHorizontally()) {
                out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager));
            } else {
                out[0] = 0;
            }
            if (layoutManager.canScrollVertically()) {
                out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager));
            } else {
                out[1] = 0;
            }
            return out;
        }

        private int distanceToStart(View targetView, OrientationHelper helper) {
            return helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
        }

        @Nullable
        @Override
        public View findSnapView(RecyclerView.LayoutManager layoutManager) {
            if (layoutManager instanceof LinearLayoutManager) {

                if (layoutManager.canScrollHorizontally()) {
                    return findStartView(layoutManager, getHorizontalHelper(layoutManager));
                } else {
                    return findStartView(layoutManager, getVerticalHelper(layoutManager));
                }
            }

            return super.findSnapView(layoutManager);
        }


        private View findStartView(RecyclerView.LayoutManager layoutManager,
                                   OrientationHelper helper) {
            if (layoutManager instanceof LinearLayoutManager) {
                int firstChild = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                //需要判断是否是最后一个Item，如果是最后一个则不让对齐，以免出现最后一个显示不完全。
                boolean isLastItem = ((LinearLayoutManager) layoutManager)
                        .findLastCompletelyVisibleItemPosition()
                        == layoutManager.getItemCount() - 1;

                if (firstChild == RecyclerView.NO_POSITION || isLastItem) {
                    return null;
                }

                View child = layoutManager.findViewByPosition(firstChild);

                if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2
                        && helper.getDecoratedEnd(child) > 0) {
                    return child;
                } else {
                    return layoutManager.findViewByPosition(firstChild + 1);
                }
            }

            return super.findSnapView(layoutManager);
        }

    }
}
