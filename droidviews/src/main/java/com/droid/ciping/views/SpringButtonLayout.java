package com.droid.ciping.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import java.util.HashMap;

/**
 * 点击弹跳效果
 */
public class SpringButtonLayout extends RelativeLayout {
    private boolean isSelected;
    private int normalTextColor;
    private int selectedTextColor;
    private Spring spring;
    private View mView;
    private HashMap<Integer, Integer> myDefaultTextColors = null;
    private HashMap<Integer, Integer> mySelectedTextColors = null;
    boolean showEnabledState = false;
    boolean hasFinishInflate = false;
    int textID = 0;

    int currentColor;

    public SpringButtonLayout(Context context) {
        super(context);
        init(context, null);
    }

    public SpringButtonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SpringButtonLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void disableScaleAnimation() {
        spring.removeAllListeners();

    }

    public void setShowEnabledState(boolean aBool) {
        showEnabledState = aBool;
    }

    private boolean checkIfEnabled() {
        if (showEnabledState)
            return isEnabled();

        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (hasFinishInflate) {
            setColorRecursive(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        hasFinishInflate = true;
        myDefaultTextColors = new HashMap<Integer, Integer>();
        mySelectedTextColors = new HashMap<Integer, Integer>();
        addStartColorRecursive(this);
        setColorRecursive(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    private void init(Context context, AttributeSet attrs) {
        currentColor = Color.argb(255, 255, 255, 255);
        isSelected = false;
        normalTextColor = Color.WHITE;
        selectedTextColor = 0x80000000 | (int) (0.7 * (normalTextColor & 0xFF))
                | ((int) (0.7 * ((normalTextColor & 0xFF00) >> 8))) << 8
                | ((int) (0.7 * ((normalTextColor & 0xFF0000) >> 16))) << 16;

        mView = this;

        if (!this.isInEditMode()) {
            SpringSystem springSystem = SpringSystem.create();
            spring = springSystem.createSpring();

            spring.setSpringConfig(new SpringConfig(40, 4));
            spring.addListener(springListener);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        if (isInEditMode()) return super.onTouchEvent(event);

        if (this.isEnabled()) {
            if (action == MotionEvent.ACTION_DOWN && !isSelected) {
                isSelected = true;
                currentColor = Color.argb(255, 155, 155, 155);

                setColorRecursive(this);
                spring.setEndValue(0.5);

            } else if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) && isSelected) {
                isSelected = false;

                currentColor = Color.argb(255, 255, 255, 255);
                setColorRecursive(this);

                spring.setEndValue(0);
            }
        } else {
            setColorRecursive(this);
        }
        return super.onTouchEvent(event);
    }

    private void setColorOnView(View aView) {
        if (aView instanceof ImageView) {
            Drawable d = ((ImageView) aView).getBackground();

            if (d == null) {
                d = ((ImageView) aView).getDrawable();
            }

            if (d != null) {
                if (isSelected || !checkIfEnabled())
                    d.setColorFilter(0x99000000, PorterDuff.Mode.SRC_ATOP);
                else
                    d.clearColorFilter();
            }
        } else if (aView instanceof TextView) {
            TextView t = (TextView) aView;
            Drawable d = t.getBackground();
            if (d != null) {
                if (isSelected || !checkIfEnabled())
                    d.setColorFilter(0x99000000, PorterDuff.Mode.SRC_ATOP);
                else
                    d.clearColorFilter();
            }

            Drawable[] tList = t.getCompoundDrawables();
            for (Drawable temp : tList) {
                if (temp != null) {
                    if (isSelected)
                        temp.setColorFilter(0x99000000, PorterDuff.Mode.SRC_ATOP);
                    else
                        temp.clearColorFilter();
                }
            }

            if (aView instanceof AutoResizeGradientTextView) {
                AutoResizeGradientTextView gradientView = (AutoResizeGradientTextView) aView;
                if (isSelected || !checkIfEnabled())
                    gradientView.setIsHighlighted(true);
                else
                    gradientView.setIsHighlighted(false);

            } else if (aView instanceof GradientTextView) {
                GradientTextView gradientView = (GradientTextView) aView;
                if (isSelected || !checkIfEnabled())
                    gradientView.setIsHighlighted(true);
                else
                    gradientView.setIsHighlighted(false);
            } else {
                if (isSelected || !checkIfEnabled())
                    t.setTextColor(mySelectedTextColors.get(t.getTag()));
                else
                    t.setTextColor(myDefaultTextColors.get(t.getTag()));
            }


        } else {
            Drawable d = aView.getBackground();
            if (d != null) {
                if (isSelected || !checkIfEnabled())
                    d.setColorFilter(0x99000000, PorterDuff.Mode.SRC_ATOP);
                else
                    d.clearColorFilter();
            }
        }
    }

    private boolean setColorRecursive(View aView) {
        if (aView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) aView;
            Drawable d = viewGroup.getBackground();
            if (d != null) {
                if (isSelected || !checkIfEnabled())
                    d.setColorFilter(0x99000000, PorterDuff.Mode.SRC_ATOP);
                else
                    d.clearColorFilter();
            }

            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (!setColorRecursive(child)) {
                    setColorOnView(child);
                }
            }
            return true;
        }
        return false;
    }

    private boolean addStartColorRecursive(View aView) {
        if (aView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) aView;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (!addStartColorRecursive(child)) {
                    addColorFromView(child);
                }
            }
            return true;
        }
        return false;
    }


    private void addColorFromView(View aView) {
        if (aView instanceof TextView) {
            TextView textView = (TextView) aView;
            Integer defaultColor = new Integer(textView.getCurrentTextColor());
            textView.setTag(Integer.valueOf(textID));
            textID++;

            Integer selectedTextColor = new Integer(0xFF000000 | (int) (0.6 * (defaultColor & 0xFF))
                    | ((int) (0.6 * ((defaultColor & 0xFF00) >> 8))) << 8
                    | ((int) (0.6 * ((defaultColor & 0xFF0000) >> 16))) << 16);

            myDefaultTextColors.put((Integer) textView.getTag(), defaultColor);
            mySelectedTextColors.put((Integer) textView.getTag(), selectedTextColor);

        }
    }


    private SpringListener springListener = new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
            // You can observe the updates in the spring
            // state by asking its current value in onSpringUpdate.
            float value = (float) spring.getCurrentValue();
            float scale = 1f - (value * 0.5f);

            mView.setScaleX(scale);
            mView.setScaleY(scale);


        }
    };

}
