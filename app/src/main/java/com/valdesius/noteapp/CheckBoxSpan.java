package com.valdesius.noteapp;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

public class CheckBoxSpan extends ImageSpan {
    private Drawable drawable;
    private boolean isChecked;

    public CheckBoxSpan(Drawable drawable, boolean isChecked) {
        super(drawable, ImageSpan.ALIGN_BOTTOM);
        this.drawable = drawable;
        this.isChecked = isChecked;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        canvas.save();
        canvas.translate(x, bottom - drawable.getBounds().bottom);
        drawable.draw(canvas);
        canvas.restore();
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}

