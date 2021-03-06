package edu.ucsb.cs184.moments.moments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;


@SuppressLint("RestrictedApi")
public class PopupMenuHelper {

    private boolean showIcon = true;
    private MenuBuilder menuBuilder;
    private MenuInflater inflater;
    private MenuPopupHelper menuHelper;

    public PopupMenuHelper(int menuRes, Context context, View anchor){
        menuBuilder = new MenuBuilder(context);
        inflater = new MenuInflater(context);
        menuHelper = new MenuPopupHelper(context, menuBuilder, anchor);
        inflater.inflate(menuRes, menuBuilder);
        setShowIcon(showIcon);
    }

    public void addItem(int groupId, int itemId, int order, String title){
        menuBuilder.add(groupId, itemId, order, title);
    }

    public void modifyIcon(int itemId, String title, int drawable){
        MenuItem item = menuBuilder.findItem(itemId);
        item.setTitle(title);
        item.setIcon(drawable);
    }

    public void hideItem(int itemid){
        menuBuilder.findItem(itemid).setVisible(false);
    }


    public void removeItem(int itemid){
        menuBuilder.removeItem(itemid);
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
        menuHelper.setForceShowIcon(showIcon);
    }

    public void setOnItemSelectedListener(final onItemSelectListener listener){
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                return listener.onItemSelected(menuBuilder, menuItem);
            }

            @Override
            public void onMenuModeChange(MenuBuilder menuBuilder) {

            }
        });
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener){
        menuHelper.setOnDismissListener(listener);
    }

    public void show(){
        menuHelper.show();
    }

    public static interface onItemSelectListener{
        public boolean onItemSelected(MenuBuilder menuBuilder, MenuItem menuItem);
    }
}
