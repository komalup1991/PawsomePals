package edu.northeastern.pawsomepals.ui.feed.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class TagLocationLayout extends LinearLayout {

    private TextView textView;

    public TagLocationLayout(Context context) {
        super(context);
        init(context);
    }

    public TagLocationLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TagLocationLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TagLocationLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        textView = new TextView(context);
        textView.setText("HELLOOO");
        setOrientation(LinearLayout.VERTICAL);
        addView(textView);
    }
}
