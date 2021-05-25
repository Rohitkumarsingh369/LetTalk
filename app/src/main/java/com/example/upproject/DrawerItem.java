package com.example.upproject;

import android.view.ViewGroup;

/**
 * Created by yarolegovich on 25.03.2017.
 */

public abstract class DrawerItem<T extends com.example.upproject.DrawerAdapter.ViewHolder> {

    protected boolean isChecked;

    public abstract T createViewHolder(ViewGroup parent);

    public abstract void bindViewHolder(T holder);

    public com.example.upproject.DrawerItem<T> setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        return this;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public boolean isSelectable() {
        return true;
    }

}
