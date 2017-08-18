package com.droid.ciping.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by dengciping on 2017/8/15.
 */

public class TagEditText extends EditText {
    public TagEditText(Context context) {
        super(context);
        init();
    }

    public TagEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TagEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLineSpacing(10, 1);

        addTextChangedListener(new TextWatcher() {
            private int length;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                length = charSequence.length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (length >= editable.length() || editable.length() == 0) {
                    return;
                }

                String content = editable.toString();
                if (content.endsWith(" ")) {
                    onSetSpan();
                }
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_DEL) return super.onKeyUp(keyCode, event);
        Editable text = getText();
        if (!(text instanceof SpannableString)) {
            return super.onKeyUp(keyCode, event);
        }

        SpannableString spannable = (SpannableString) text;
        String content = getText().toString();
        if (content.length() > 0) {
            String last = content.substring(content.length() - 1, content.length());
            if (!last.equals(" ")) {
                String[] m = content.split(" ");
                String lastTag = m[m.length - 1];
                content = content.substring(0, content.length() - lastTag.length());
                setText(content);
                return true;
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * 核心步骤
     * 主要通过SpannableString来实现标签分组
     **/
    private void onSetSpan() {
        String content = getText().toString();
        SpannableString spannable = new SpannableString(content);
        //通过空格来区分标签
        String[] m = content.split(" ");
        int start = 0;
        int end;
        for (String str : m) {
            end = start + str.length();

            RadiusSpan span = new RadiusSpan(Color.GRAY, Color.WHITE, 5);
            span.setPaddingLR(5);
            span.setPaddingTB(5);
            spannable.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

            start = end + 1;
        }
        setText(spannable);
        //设置完成后 需要把焦点移动到最后一位
        setSelection(spannable.length());
    }

    class RadiusSpan extends ReplacementSpan {
        /**
         * 需要设置span的宽度
         */
        private int mSize;
        /**
         * 背景颜色
         */
        private int mBackgroundColor;
        /**
         * 字体颜色
         */
        private int mTextColor;
        /**
         * 圆角
         */
        private int mRadius;
        /**
         * 设置左右内边距
         * 默认2
         */
        private int mPaddingLR = 2;
        /**
         * 设置上下边距
         */
        private int mPaddingTB = 2;

        /**
         * @param backgroundColor 背景颜色
         * @param radius          圆角半径
         */
        public RadiusSpan(int backgroundColor, int textColor, int radius) {
            this.mBackgroundColor = backgroundColor;
            this.mTextColor = textColor;
            this.mRadius = radius;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            //根据传入起始截至位置获得截取文字的宽度，最后加上左右两个圆角的半径得到span宽度
            mSize = (int) (paint.measureText(text, start, end) + mPaddingLR * mRadius);
            return mSize;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            paint.setColor(mBackgroundColor);//设置背景颜色
            paint.setAntiAlias(true);// 设置画笔的锯齿效果
            RectF oval = new RectF(x, y + paint.ascent(), x + mSize, y + paint.descent() + mPaddingTB);
            //设置文字背景矩形，
            // x为span其实左上角相对整个TextView的x值，
            // y为span左上角相对整个View的y值。
            // paint.ascent()获得文字上边缘，paint.descent()获得文字下边缘
            canvas.drawRoundRect(oval, mRadius, mRadius, paint);
            paint.setColor(mTextColor);
            //绘制文字
            canvas.drawText(text, start, end, x + mRadius * mPaddingLR / 2, y + mPaddingTB / 2, paint);
        }

        public int getPaddingLR() {
            return mPaddingLR;
        }

        public void setPaddingLR(int mPaddingLR) {
            this.mPaddingLR = mPaddingLR;
        }

        public int getPaddingTB() {
            return mPaddingTB;
        }

        public void setPaddingTB(int mPaddingTB) {
            this.mPaddingTB = mPaddingTB;
        }
    }

    class ClickSpan extends ClickableSpan {
        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);         //设置文本的颜色
//            ds.setColor(res.getColor(R.color.color));
            //超链接形式的下划线，false 表示不显示下划线，true表示显示下划线
//          ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View widget) {
        }
    }
}
