package com.valdesius.noteapp.helpers;


import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.List;

public class SpannableStringBuilderConverter {

    @TypeConverter
    public static String fromSpannableStringBuilder(SpannableStringBuilder spannableStringBuilder) {
        if (spannableStringBuilder == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(spannableStringBuilder.toString());

        Object[] spans = spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), Object.class);
        for (Object span : spans) {
            int start = spannableStringBuilder.getSpanStart(span);
            int end = spannableStringBuilder.getSpanEnd(span);
            int flags = spannableStringBuilder.getSpanFlags(span);

            if (span instanceof AbsoluteSizeSpan) {
                stringBuilder.append("|SIZE:")
                        .append(((AbsoluteSizeSpan) span).getSize())
                        .append(":")
                        .append(start)
                        .append(":")
                        .append(end)
                        .append(":")
                        .append(flags)
                        .append("|");
            } else if (span instanceof StyleSpan) {
                stringBuilder.append("|STYLE:")
                        .append(((StyleSpan) span).getStyle())
                        .append(":")
                        .append(start)
                        .append(":")
                        .append(end)
                        .append(":")
                        .append(flags)
                        .append("|");
            }
        }

        return stringBuilder.toString();
    }

    @TypeConverter
    public static SpannableStringBuilder toSpannableStringBuilder(String value) {
        if (value == null) {
            return null;
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(value);
        String[] parts = value.split("\\|");

        for (String part : parts) {
            if (part.startsWith("SIZE:")) {
                String[] sizeParts = part.substring(5).split(":");
                int size = Integer.parseInt(sizeParts[0]);
                int start = Integer.parseInt(sizeParts[1]);
                int end = Integer.parseInt(sizeParts[2]);
                int flags = Integer.parseInt(sizeParts[3]);
                spannableStringBuilder.setSpan(new AbsoluteSizeSpan(size, true), start, end, flags);
            } else if (part.startsWith("STYLE:")) {
                String[] styleParts = part.substring(6).split(":");
                int style = Integer.parseInt(styleParts[0]);
                int start = Integer.parseInt(styleParts[1]);
                int end = Integer.parseInt(styleParts[2]);
                int flags = Integer.parseInt(styleParts[3]);
                spannableStringBuilder.setSpan(new StyleSpan(style), start, end, flags);
            }
        }

        return spannableStringBuilder;
    }
}
