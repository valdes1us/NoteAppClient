package com.valdesius.noteapp.helpers;

import android.content.Context;
import android.text.style.ImageSpan;
import android.view.View;

public class ClickableImageSpan extends ImageSpan {
    private final View.OnClickListener onClickListener;

    public ClickableImageSpan(Context context, int resourceId, View.OnClickListener onClickListener) {
        super(context, resourceId);
        this.onClickListener = onClickListener;
    }


}
