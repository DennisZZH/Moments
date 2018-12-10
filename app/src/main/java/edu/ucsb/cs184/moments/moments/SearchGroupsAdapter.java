package edu.ucsb.cs184.moments.moments;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchGroupsAdapter extends CustomAdapter{
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_search_group, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    public class ViewHolder extends CustomAdapter.CustomViewHolder {
        TextView number, name, intro, members;
        ImageView icon;
        ImageButton join;
        Group data;
        public ViewHolder(@NonNull View view) {
            super(view);
            number = view.findViewById(R.id.sg_number);
            icon = view.findViewById(R.id.sg_icon);
            name = view.findViewById(R.id.sg_name);
            intro = view.findViewById(R.id.sg_intro);
            members = view.findViewById(R.id.sg_followers);
            join = view.findViewById(R.id.sg_join);
        }

        @Override
        public void setData(Object object){
            data = (Group) object;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (User.user.inGroup(data.getId())){
                        Intent intent = new Intent(context, GroupPostsActivity.class);
                        intent.putExtra(GroupsFragment.GROUP, data);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
                    }
                }
            });
            name.setText(data.getName());
            number.setText("#" + data.getNumber());
            if (User.user.inGroup(data.getId())) join.setVisibility(View.GONE);
            else join.setVisibility(View.VISIBLE);
            join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User.user.joinGroup(data.getId());
                    join.setVisibility(View.GONE);
                }
            });
            FirebaseHelper.setIcon(data.GetIcon(), activity, icon);
            name.setText(data.getName());
            intro.setText(data.getIntro());
            members.setText("Members: " + data.getMemberSize());
        }
    }
}
