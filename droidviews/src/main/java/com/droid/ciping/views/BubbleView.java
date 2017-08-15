package com.droid.ciping.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * 贝塞尔曲线 冒泡效果
 */
public class BubbleView extends FrameLayout {
    private List<Drawable> drawableList = new ArrayList<>();

    private int viewWidth = 16, viewHeight = 16;
    private int rankWidth = 100, rankHeight = 100;

    private int maxHeartNum = 8;
    private int minHeartNum = 2;

    private int riseDuration = 2000;

    private float maxScale = 1.2f;
    private float minScale = 0.8f;

    private int innerDelay = 200;

    private Rect outRect;

    public BubbleView(Context context) {
        super(context);
    }

    public BubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BubbleView setDrawableList(List<Drawable> drawableList) {
        this.drawableList = drawableList;
        return this;
    }

    public BubbleView setRiseDuration(int riseDuration) {
        this.riseDuration = riseDuration;
        return this;
    }

    public BubbleView setScaleAnimation(float maxScale, float minScale) {
        this.maxScale = maxScale;
        this.minScale = minScale;
        return this;
    }

    public BubbleView setAnimationDelay(int delay) {
        this.innerDelay = delay;
        return this;
    }

    public void setMaxHeartNum(int maxHeartNum) {
        this.maxHeartNum = maxHeartNum;
    }

    public void setMinHeartNum(int minHeartNum) {
        this.minHeartNum = minHeartNum;
    }

    public BubbleView setItemViewWH(int viewWidth, int viewHeight) {
        this.viewHeight = viewHeight;
        this.viewWidth = viewWidth;
        return this;
    }

    public BubbleView setRankWH(int viewWidth, int viewHeight) {
        this.rankHeight = viewHeight;
        this.rankWidth = viewWidth;
        return this;
    }

    public BubbleView setGiftBoxImaeg(Drawable drawable, int positionX, int positionY) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageDrawable(drawable);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageView.getWidth(), imageView.getHeight());
        this.addView(imageView, layoutParams);
        imageView.setX(positionX);
        imageView.setY(positionY);
        return this;
    }

    public void startAnimation() {
        startAnimation(innerDelay, 1);
    }

    public void startAnimation(int count) {
        startAnimation(innerDelay, count);
    }

    public void startAnimation(int delay, final int count) {

        Observable.timer(delay, TimeUnit.MILLISECONDS)
                .repeat(count)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        bubbleAnimation();
                    }
                });
    }

    private void bubbleAnimation() {
        if (drawableList.size() == 0) return;

        int heartDrawableIndex = (int) (drawableList.size() * Math.random());
        Drawable drawable = drawableList.get(heartDrawableIndex);
        //change color
        Random random = new Random();
        drawable.setColorFilter(Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255)), PorterDuff.Mode.SRC_IN);

        // add views
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(viewWidth, viewHeight);
        ImageView tempImageView = new ImageView(getContext());
        tempImageView.setImageDrawable(drawable);
        addView(tempImageView, layoutParams);

        //start animation
        ObjectAnimator riseAlphaAnimator = ObjectAnimator.ofFloat(tempImageView, "alpha", 1.0f, 0.0f);
        riseAlphaAnimator.setDuration(riseDuration);

        ObjectAnimator riseScaleXAnimator = ObjectAnimator.ofFloat(tempImageView, "scaleX", minScale, maxScale);
        riseScaleXAnimator.setDuration(riseDuration);

        ObjectAnimator riseScaleYAnimator = ObjectAnimator.ofFloat(tempImageView, "scaleY", minScale, maxScale);
        riseScaleYAnimator.setDuration(riseDuration);

        outRect = new Rect();
        getDrawingRect(outRect);

        ValueAnimator valueAnimator = getBezierAnimator(tempImageView, outRect);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(valueAnimator).with(riseAlphaAnimator).with(riseScaleXAnimator).with(riseScaleYAnimator);
        animatorSet.start();
    }

    private ValueAnimator getBezierAnimator(final ImageView imageView, Rect outRect) {
        int width = outRect.width();
        int height = outRect.height();

        Random random = new Random();
        int randomW = outRect.left + random.nextInt(width);
        int randomW2 = outRect.left + random.nextInt(width);
        int randomW3 = outRect.left + random.nextInt(width);

        float point0[] = new float[2];
        point0[0] = outRect.centerX();
        point0[1] = outRect.bottom;

        float point1[] = new float[2];
        point1[0] = randomW;
        point1[1] = outRect.bottom - height / 3;

        float point2[] = new float[2];
        point2[0] = randomW2;
        point2[1] = outRect.bottom - height / 3 * 2;

        float point3[] = new float[2];
        point3[0] = randomW3;
        point3[1] = outRect.top;

        BezierEvaluator bezierEvaluator = new BezierEvaluator(point1, point2);
        ValueAnimator valueAnimator = ValueAnimator.ofObject(bezierEvaluator, point0, point3);
        valueAnimator.setDuration(riseDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float[] currentPosition = new float[2];
                currentPosition = (float[]) animation.getAnimatedValue();
                imageView.setTranslationX(currentPosition[0]);
                imageView.setTranslationY(currentPosition[1]);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                removeView(imageView);
                imageView.setImageDrawable(null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return valueAnimator;
    }

    public class BezierEvaluator implements TypeEvaluator<float[]> {
        private float point1[] = new float[2], point2[] = new float[2];

        public BezierEvaluator(float[] point1, float[] point2) {
            this.point1 = point1;
            this.point2 = point2;
        }

        @Override
        public float[] evaluate(float fraction, float[] point0, float[] point3) {
            float[] currentPosition = new float[2];
            currentPosition[0] = point0[0] * (1 - fraction) * (1 - fraction) * (1 - fraction)
                    + point1[0] * 3 * fraction * (1 - fraction) * (1 - fraction)
                    + point2[0] * 3 * (1 - fraction) * fraction * fraction
                    + point3[0] * fraction * fraction * fraction;
            currentPosition[1] = point0[1] * (1 - fraction) * (1 - fraction) * (1 - fraction)
                    + point1[1] * 3 * fraction * (1 - fraction) * (1 - fraction)
                    + point2[1] * 3 * (1 - fraction) * fraction * fraction
                    + point3[1] * fraction * fraction * fraction;
            return currentPosition;
        }
    }

}
