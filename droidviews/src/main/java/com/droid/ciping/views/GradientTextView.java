package com.droid.ciping.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Build;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GradientTextView extends TextView {
    private int[] gradient;
    private int shadowColor;
    private float shadowOffsetX = 1.1f;
    private float shadowOffsetY = 1.1f;
    private float shadowRadius = 5.f;
    private Shader shader;
    private Shader selectShader;
    private boolean useShadow;
    private boolean useGradient;
    private List<SpannableString> spannableStringList;
    private int lastSubStringIndex = 0;
    private boolean shouldSetSpan = false;
    private String prevString = "";
    private boolean isHighlighted;

    public GradientTextView(Context context) {
        super(context);

        if (!isInEditMode())
            init(context, null);
    }

    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode())
            init(context, attrs);
    }

    public GradientTextView(Context context, AttributeSet attrs, int position) {
        super(context, attrs, position);

        if (!isInEditMode())
            init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        int orange = getResources().getColor(android.R.color.holo_orange_dark);
        int startColor = Color.YELLOW;
        int endColor = getResources().getColor(android.R.color.holo_orange_dark);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GradientTextView);
            if (a != null) {
                try {
                    startColor = a.getColor(R.styleable.GradientTextView_gradientStartColor, startColor);
                    endColor = a.getColor(R.styleable.GradientTextView_gradientEndColor, endColor);
                } finally {
                    a.recycle();
                }
            }
        }

        gradient = new int[]{startColor, endColor};
        useShadow = true;
        useGradient = true;

        createSelectShader(gradient[0], gradient[1]);
        shader = new LinearGradient(0, 0, 0, getPaint().getTextSize(), gradient[0], gradient[1], TileMode.CLAMP);

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

    private void addSpannableString(String displayName, int index, boolean setSpan) {
        String stringPart = displayName.substring(lastSubStringIndex, index);
        SpannableString spanString = new SpannableString(stringPart);
        if (setSpan) {
            spanString.setSpan(new NoGradientSpan(), 0, spanString.length(), 0);
        }
        spannableStringList.add(spanString);
        shouldSetSpan = !setSpan;
        lastSubStringIndex = index;
    }

    private CharSequence containsIllegalCharacters(String displayName) {
        final int nameLength = displayName.length();
        spannableStringList = new ArrayList<SpannableString>();

        lastSubStringIndex = 0;
        shouldSetSpan = false;

        for (int i = 0; i < nameLength; i++) {
            final char hs = displayName.charAt(i);

            if (0xd800 <= hs && hs <= 0xdbff) {
                final char ls = displayName.charAt(i + 1);
                final int uc = ((hs - 0xd800) * 0x400) + (ls - 0xdc00) + 0x10000;

                if (0x1d000 <= uc && uc <= 0x1f77f) {

                    if (!shouldSetSpan) {
                        addSpannableString(displayName, i, false);
                    }
                    ++i;
                }
            } else if (Character.isHighSurrogate(hs)) {
                final char ls = displayName.charAt(i + 1);

                if (ls == 0x20e3) {
                    if (!shouldSetSpan) {
                        addSpannableString(displayName, i, false);
                    }
                    ++i;
                }
            } else {
                // non surrogate
                if (0x2100 <= hs && hs <= 0x27ff) {
                    if (!shouldSetSpan)
                        addSpannableString(displayName, i, false);
                } else if (0x2B05 <= hs && hs <= 0x2b07) {
                    if (!shouldSetSpan)
                        addSpannableString(displayName, i, false);
                } else if (0x2934 <= hs && hs <= 0x2935) {
                    if (!shouldSetSpan)
                        addSpannableString(displayName, i, false);
                } else if (0x3297 <= hs && hs <= 0x3299) {
                    if (!shouldSetSpan)
                        addSpannableString(displayName, i, false);
                } else if (hs == 0xa9 || hs == 0xae || hs == 0x303d || hs == 0x3030 || hs == 0x2b55 || hs == 0x2b1c || hs == 0x2b1b || hs == 0x2b50) {
                    if (!shouldSetSpan)
                        addSpannableString(displayName, i, false);
                } else {
                    if (shouldSetSpan) {
                        addSpannableString(displayName, i, true);
                    }
                }
            }
        }

        if (shouldSetSpan) {
            addSpannableString(displayName, nameLength, true);
        } else {
            addSpannableString(displayName, nameLength, false);
        }

        final CharSequence[] charSequenceItems = spannableStringList.toArray(new CharSequence[spannableStringList.size()]);

        return TextUtils.concat(charSequenceItems);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        setText(containsIllegalCharacters(getText().toString()));

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
        this.shader = new LinearGradient(0, 0, 0, getPaint().getTextSize(), gradient[0], gradient[1], TileMode.CLAMP);
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setUseShadow(boolean useShadow, int shadowColor, int shadowRadius) {
        this.useShadow = useShadow;
        this.shadowColor = shadowColor;
        this.shadowRadius = shadowRadius;

        if (useShadow) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                setLayerType(LAYER_TYPE_SOFTWARE, getPaint());
            }
        }
    }

    public void setIsHighlighted(boolean aBool) {
        isHighlighted = aBool;
        postInvalidate();
    }

    public boolean isUsingGradient() {
        return useGradient;
    }

    public void setUseGradient(boolean useGradient) {
        this.useGradient = useGradient;
        postInvalidate();
    }

    class NoGradientSpan extends CharacterStyle implements UpdateAppearance {

        public NoGradientSpan() {

        }

        @Override
        public void updateDrawState(TextPaint paint) {

            paint.setShader(null);
            paint.clearShadowLayer();
        }


    }
}
