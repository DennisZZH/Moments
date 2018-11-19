package edu.ucsb.cs184.moments.moments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.jude.swipbackhelper.SwipeBackHelper;

public class UserProfileActivity extends AppCompatActivity {

    private Button sortBy;
    private ImageButton back;
    private View include;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    private ImageView icon;
    private Intent intent;
    private RecycleViewFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        SwipeBackHelper.onCreate(this);
        intent = getIntent();

        back = findViewById(R.id.up_back);
        include = findViewById(R.id.content_user_profile);
        sortBy = include.findViewById(R.id.up_sortby_button);
        icon = include.findViewById(R.id.up_usericon);
        toolbar = findViewById(R.id.up_toolbar);
        collapsingToolbarLayout = findViewById(R.id.up_ctoolbar);
        fragment = new RecycleViewFragment();
        fragment.show(getSupportFragmentManager(), R.id.up_timeline);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uu = new Intent(UserProfileActivity.this, UploadIconActivity.class);
                uu.putExtra(UploadIconActivity.upload_icon, ((BitmapDrawable) icon.getDrawable()).getBitmap());
                startActivityForResult(uu, UploadIconActivity.iconCode);
            }
        });
        sortBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenuHelper helper = new PopupMenuHelper(R.menu.sort_by_menu, UserProfileActivity.this, sortBy);
                helper.setOnItemSelectedListener(new PopupMenuHelper.onItemSelectListener() {
                    @Override
                    public boolean onItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.sb_highest_rating:
                                return true;
                            case R.id.sb_lowest_rating:
                                return true;
                            case R.id.sb_lastest:
                                return true;
                            case R.id.sb_oldest:
                                return true;
                        }
                        return false;
                    }
                });
                helper.show();
            }
        });
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(getColor(android.R.color.transparent));
        setSupportActionBar(toolbar);
        setUserProfile(intent.getIntExtra("userid", 0));
    }

    private void setUserProfile(int userid){
        collapsingToolbarLayout.setTitle("Username");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == UploadIconActivity.iconCode){
            icon.setImageBitmap((Bitmap) data.getParcelableExtra(UploadIconActivity.upload_icon));
        }
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
