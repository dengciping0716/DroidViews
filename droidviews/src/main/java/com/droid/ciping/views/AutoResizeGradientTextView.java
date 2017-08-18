package com.droid.ciping.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Build;
import android.util.AttributeSet;

public class AutoResizeGradientTextView extends AutoResizeTextView {
    private int[] gradient;
    private int shadowColor;
    private float shadowOffsetX = 1.1f;
    private float shadowOffsetY = 1.1f;
    private float shadowRadius = 5.f;
    private Shader shader;
    private Shader selectShader;

    private boolean useShadow;
    private boolean useGradient;
    private boolean isHighlighted;

    public AutoResizeGradientTextView(Context context) {
        super(context);

        if (!isInEditMode())
            init();
    }

    public AutoResizeGradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode())
            init();
    }

    public AutoResizeGradientTextView(Context context, AttributeSet attrs, int position) {
        super(context, attrs, position);

        if (!isInEditMode())
            init();
    }

    public void init() {
        int yellow = Color.YELLOW;
        int orange = getResources().getColor(android.R.color.holo_orange_dark);

        gradient = new int[]{yellow, orange};

        useShadow = true;
        useGradient = true;

        createSelectShader(gradient[0], gradient[1]);
        shader = new LinearGradient(0, 0, 0, getPaint().getTextSize(), new int[]{gradient[0], gradient[1]}, new float[]{0, 1}, TileMode.CLAMP);
        shadowColor = Color.argb(140, 0, 0, 0);

        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    private void createSelectShader(int color1, int color2) {
        Integer selectedTextColor1 = new Integer(0xFF000000 | (int) (0.6 * (color1 & 0xFF))
                | ((int) (0.6 * ((color1 & 0xFF00) >> 8))) << 8
                | ((int) (0.6 * ((color1 & 0xFF0000) >> 16))) << 16);

        Integer selectedTextColor2 = new Integer(0xFF000000 | (int) (0.6 * (color2 & 0xFF))
                | ((int) (0.6 * ((color2 & 0xFF00) >> 8))) << 8
                | ((int) (0.6 * ((color2 & 0xFF0000) >> 16))) << 16);

        selectShader = new LinearGradient(0, 0, 0, getPaint().getTextSize(), new int[]{selectedTextColor1, selectedTextColor2}, new float[]{0, 1}, TileMode.CLAMP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (useShadow) {
            getPaint().setShadowLayer(shadowRadius, shadowOffsetX, shadowOffsetY, shadowColor);
        } else {
            getPaint().setShadowLayer(0.0f, 0.0f, 0.0f, shadowColor);
        }
        getPaint().setShader(null);

        super.onDraw(canvas);

        getPaint().clearShadowLayer();

        if (useGradient) {
            if (!isHighlighted)
                getPaint().setShader(shader);
            else
                getPaint().setShader(selectShader);

        }

        super.onDraw(canvas);
    }

    public int[] getGradient() {
        return gradient;
    }

    public void setGradient(int[] gradient) {
        this.gradient = gradient;
        shader = new LinearGradient(0, 0, 0, getPaint().getTextSize(), new int[]{gradient[0], gradient[1]}, new float[]{0, 1}, TileMode.CLAMP);
        createSelectShader(gradient[0], gradient[1]);
        postInvalidate();
    }

    public int getShadowColor() {
        return shadowColor;
    }

    public void setShadowColor(int color) {
        this.shadowColor = color;
    }

    public void setShadowOffset(float x, float y) {
        this.shadowOffsetX = x;
        this.shadowOffsetY = y;
    }

    public void setShadowRadius(float radius) {
        this.shadowRadius = radius;
    }

    public boolean isUsingShadow() {
        return useShadow;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setUseShadow(boolean useShadow) {
        this.useShadow = useShadow;

        if (useShadow) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                setLayerType(LAYER_TYPE_SOFTWARE, getPaint());
            }
        }
    }

    public boolean isUsingGradient() {
        return useGradient;
    }

    public void setIsHighlighted(boolean aBool) {
        isHighlighted = aBool;
        postInvalidate();
    }

    public void setUseGradient(boolean useGradient) {
        this.useGradient = useGradient;
        postInvalidate();
    }
}
