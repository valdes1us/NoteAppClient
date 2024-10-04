package com.valdesius.noteapp.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private int textColor;
    private int backgroundColor;

    public CustomSpinnerAdapter(Context context, List<String> items, int textColor, int backgroundColor) {
        super(context, android.R.layout.simple_spinner_item, items);
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setTextColor(textColor);
        textView.setBackgroundColor(backgroundColor);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setTextColor(textColor);
        textView.setBackgroundColor(backgroundColor);
        return view;
    }
}
