/*
 * Copyright (c) 2022.  Aravind Chowdary (@kamaravichow)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.aravi.dot.ui.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.aravi.dot.R;


public class SettingsView extends RelativeLayout {
    private final Context context;
    private AttributeSet attrs;
    private int styleAttr;
    private TextView textTitle, textInfo;
    private ImageView imageIcon, imageOpen;

    public SettingsView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public SettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        initView();
    }

    public SettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.attrs = attrs;
        this.styleAttr = defStyleAttr;
        initView();
    }

    private void initView() {
        inflate(context, R.layout.item_setting, this);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.SettingsView,
                styleAttr, 0);

        imageIcon = findViewById(R.id.image_icon);
        textTitle = findViewById(R.id.text_title);
        textInfo = findViewById(R.id.text_info);
        imageOpen = findViewById(R.id.image_open);

        setAttrsToView(arr);
        arr.recycle();
    }

    public void setAttrsToView(TypedArray arr) {
        textTitle.setText(arr.getText(R.styleable.SettingsView_title));
        textInfo.setText(arr.getText(R.styleable.SettingsView_message));
        imageIcon.setImageDrawable(arr.getDrawable(R.styleable.SettingsView_icon));
        imageIcon.setImageTintList(ColorStateList.valueOf(arr.getColor(R.styleable.SettingsView_icon_tint, ContextCompat.getColor(context, R.color.blue_grey_900))));
        imageIcon.setBackgroundTintList(ColorStateList.valueOf(arr.getColor(R.styleable.SettingsView_icon_tint, ContextCompat.getColor(context, R.color.blue_grey_900))).withAlpha(25));
        boolean visibility = arr.getBoolean(R.styleable.SettingsView_iconVisibility, true);
        if (visibility) {
            imageOpen.setVisibility(VISIBLE);
        } else {
            imageOpen.setVisibility(INVISIBLE);
        }
    }

    public void setIconTint(int color) {
        this.imageIcon.setImageTintList(ColorStateList.valueOf(color));
    }

    public void setTitle(String info) {
        this.textTitle.setText(info);
    }

    public void setMessage(String info) {
        this.textInfo.setText(info);
    }

}
