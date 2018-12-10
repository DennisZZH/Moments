package edu.ucsb.cs184.moments.moments;

import android.content.Intent;
import android.support.v7.view.menu.MenuBuilder;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GroupAdapter extends CustomAdapter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_group, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    public static String TimeText(Long time){
        Calendar date = Calendar.getInstance(), now = Calendar.getInstance();
        date.setTimeInMillis(time);
        int delta_year = now.get(Calendar.YEAR) - date.get(Calendar.YEAR);
        if (delta_year != 0) return new SimpleDateFormat("yyyy-MM-dd").format(time);
        int delta_day = now.get(Calendar.DAY_OF_YEAR) - date.get(Calendar.DAY_OF_YEAR);
        if (delta_day == 0) return new SimpleDateFormat("HH:mm").format(time);
        else if (delta_day == 1) return "Yesterday";
        if (date.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)) return date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);;
        return new SimpleDateFormat("MM-dd").format(time);
    }

    public class ViewHolder extends CustomAdapter.CustomViewHolder {
        public ImageView group_icon;
        public TextView group_name;
        public TextView time;
        public TextView content;
        public ImageView quiet;
        private Group data;

        public ViewHolder(final View view) {
            super(view);
            group_icon = view.findViewById(R.id.group_icon);
            group_name = view.findViewById(R.id.group_name);
            time = view.findViewById(R.id.group_time);
            content = view.findViewById(R.id.group_content);
            quiet = view.findViewById(R.id.group_quiet);
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, GroupPostsActivity.class);
                    intent.putExtra(GroupsFragment.GROUP, data);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenuHelper helper = new PopupMenuHelper(R.menu.group_popup_menu, view.getContext(), view);
                    helper.setOnItemSelectedListener(new PopupMenuHelper.onItemSelectListener() {
                        @Override
                        public boolean onItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                            switch (menuItem.getItemId()){
                                case R.id.menuitem_test:
                                    return true;
                            }
                            return false;
                        }
                    });
                    helper.show();
                    return true;
                }
            });
        }

        @Override
        public void setData(Object object) {
            data = (Group) object;
            group_name.setText(data.getName());
            FirebaseHelper.setIcon(data.GetIcon(), activity, group_icon);
            Message message = data.latestActivity();
            time.setText(TimeText(message.getTime()));
            content.setText(message.getContent());
        }
        public void setQuiet(boolean isQuiet){
            quiet.setVisibility(isQuiet ? View.VISIBLE : View.GONE);
        }
    } 
}
