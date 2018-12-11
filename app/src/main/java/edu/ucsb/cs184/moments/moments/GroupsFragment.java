package edu.ucsb.cs184.moments.moments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collections;

import static android.app.Activity.RESULT_OK;

public class GroupsFragment extends Fragment {

    private static final int CREATE_GROUP = 0;

    private View view;
    private Context context;
    private Toolbar toolbar;
    private ImageButton menu;
    private ImageButton add;
    private DrawerLayout drawer;
    private BottomNavigationView nav;
    private RecyclerViewFragment fragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_groups, container, false);
        context = getContext();
        toolbar = view.findViewById(R.id.groups_toolbar);
        menu = view.findViewById(R.id.groups_menu);
        add = view.findViewById(R.id.groups_add);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.gotoTop();
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer != null) drawer.openDrawer(Gravity.START);
            }
        });
        final PopupMenuHelper helper = new PopupMenuHelper(R.menu.groups_adder_menu, context, add);
        helper.setOnItemSelectedListener(new PopupMenuHelper.onItemSelectListener() {
            @Override
            public boolean onItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                Intent intent;
                switch (menuItem.getItemId()){
                    case R.id.create_a_group:
                        intent = new Intent(context, CreateGroupActivity.class);
                        startActivityForResult(intent, CREATE_GROUP);
                        return true;
                    case R.id.join_a_group:
                        intent = new Intent(context, SearchActivity.class);
                        intent.putExtra(SearchActivity.TAB, SearchPair.GROUPS);
                        startActivity(intent);
                        return true;
                }
                return false;
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                helper.show();
            }
        });
        fragment = new RecyclerViewFragment();
        fragment.setShowDivider(true);
        fragment.setAdapter(new GroupAdapter());
        try {
            fragment.setData(User.user.getGroups());
        } catch (RecyclerViewFragment.UnsupportedDataException e) {
            e.printStackTrace();
        }
        fragment.addOnRefreshListener(new RecyclerViewFragment.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        fragment.addHiddenView(nav);
        fragment.show(getFragmentManager(), R.id.groups_content);
        return view;
    }

    public void refresh(){
        fragment.gotoTop();
        new Thread(new Runnable() {
            @Override
            public void run() {
                User.user.refreshGroups();
                ArrayList<Group> data = User.user.getGroups();
                Collections.sort(data, new Group.GroupComparator());
                try {
                    fragment.setData(data);
                } catch (RecyclerViewFragment.UnsupportedDataException e) {
                    e.printStackTrace();
                }
                fragment.gotoTop();
            }
        }).start();
    }

    public void setWidgets(DrawerLayout drawer, BottomNavigationView nav){
        this.drawer = drawer;
        this.nav = nav;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == CREATE_GROUP){
            try {
                fragment.addElement(data.getParcelableExtra(GroupPostsActivity.GROUP));
            } catch (RecyclerViewFragment.UnsupportedDataException e) {
                e.printStackTrace();
            }
        }
    }
}
