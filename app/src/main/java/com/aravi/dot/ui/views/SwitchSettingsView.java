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
import com.bitvale.switcher.SwitcherX;


public class SwitchSettingsView extends RelativeLayout {
    private final Context context;
    private AttributeSet attrs;
    private int styleAttr;
    private TextView textTitle, textInfo;
    private ImageView imageIcon;
    private SwitcherX switchMaterial;

    public SwitchSettingsView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public SwitchSettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        initView();
    }

    public SwitchSettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.attrs = attrs;
        this.styleAttr = defStyleAttr;
        initView();
    }

    private void initView() {
        inflate(context, R.layout.item_setting_switch, this);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.SettingsView,
                styleAttr, 0);

        imageIcon = findViewById(R.id.image_icon);
        textTitle = findViewById(R.id.text_title);
        textInfo = findViewById(R.id.text_info);
        switchMaterial = findViewById(R.id.switch_state);

        setAttrsToView(arr);
        arr.recycle();
    }

    public void setAttrsToView(TypedArray arr) {
        textTitle.setText(arr.getText(R.styleable.SettingsView_title));
        textInfo.setText(arr.getText(R.styleable.SettingsView_message));
        imageIcon.setImageDrawable(arr.getDrawable(R.styleable.SettingsView_icon));
        imageIcon.setImageTintList(ColorStateList.valueOf(arr.getColor(R.styleable.SettingsView_icon_tint, ContextCompat.getColor(context, R.color.blue_grey_900))));
        imageIcon.setBackgroundTintList(ColorStateList.valueOf(arr.getColor(R.styleable.SettingsView_icon_tint, ContextCompat.getColor(context, R.color.blue_grey_900))).withAlpha(25));
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

    public void checked(Boolean checked) {
        this.switchMaterial.setChecked(checked, true);
    }

}
