package edu.ucsb.cs184.moments.moments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jude.swipbackhelper.SwipeBackHelper;

public class GroupPostsActivity extends AppCompatActivity {

    public static final String GROUP = "Group";
    public static final int SETTINGS = 2;
    public static Group group;

    private Toolbar toolbar;
    private TextView title;
    private ImageView icon;
    private ImageButton back;
    private ImageButton more;
    private FloatingActionButton fab;
    private RecyclerViewFragment fragment;
    private Intent data;
//    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_posts);
        SwipeBackHelper.onCreate(this);
        data = getIntent();
//        group = data.getParcelableExtra(GROUP);

        toolbar = findViewById(R.id.gp_toolbar);
        fab = findViewById(R.id.gp_fab);
        back = findViewById(R.id.gp_back);
        more = findViewById(R.id.gp_more);
        title = findViewById(R.id.gp_title);
        icon = findViewById(R.id.gp_icon);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.gotoTop();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EditPostActivity.class);
                EditPostActivity.group = group;
//                intent.putExtra(EditPostActivity.GROUP, group);
                startActivityForResult(intent, EditPostActivity.EDIT_POST);
                overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupPostsActivity.this, GroupSettingsActivity.class);
                GroupSettingsActivity.group = group;
//                intent.putExtra(GroupSettingsActivity.GROUP, group);
                startActivityForResult(intent, SETTINGS);
                overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
            }
        });
        title.setText(group.getName());
        FirebaseHelper.setIcon(group.GetIcon(), this, icon);
        fragment = new RecyclerViewFragment();
        fragment.setAdapter(new PostAdapter());
        fragment.addHiddenView(fab);
        try {
            fragment.setData(group.getPosts());
        } catch (RecyclerViewFragment.UnsupportedDataException e) {
            e.printStackTrace();
        }
        fragment.show(getSupportFragmentManager(), R.id.gp_content);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's state here
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode){
            case EditPostActivity.EDIT_POST:
                try {
                    fragment.addElement(data.getParcelableExtra(EditPostActivity.POST));
                }catch (RecyclerViewFragment.UnsupportedDataException e) {
                    e.printStackTrace();
                }
                fragment.gotoTop();
                break;
            case PostAdapter.FULL_POST:
                try {
                    if (data.getStringExtra(FullPostActivity.DELETE_POST) != null)
                        fragment.removeElement(data.getParcelableExtra(FullPostActivity.POST));
                } catch (RecyclerViewFragment.UnsupportedDataException e) {
                    e.printStackTrace();
                }
                break;
            case SETTINGS:
                if (data.getStringExtra(GroupSettingsActivity.QUIT) != null){
                    close();
                }
                break;

        }
    }

    private void close(){
        finish();
        overridePendingTransition(R.anim.push_left_in,R.anim.push_right_out);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SwipeBackHelper.onPostCreate(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SwipeBackHelper.onDestroy(this);
    }
}
