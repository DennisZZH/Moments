package edu.ucsb.cs184.moments.moments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jude.swipbackhelper.SwipeBackHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FullPostActivity extends AppCompatActivity {

    public static final String POST = "post";
    public static final String ADD_COMMENT = "Add Comment";
    public static final String DELETE_POST = "Delete Post";

    private SwipeRefreshLayout swipeRefreshLayout;
    private TabViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabPagerAdapter pagerAdapter;
    private ImageButton more;
    private ImageButton backButton;
    private View include;
    private ImageView poster_icon;
    private TextView username, time, content, comments_counter;
    private RatingBar ratingBar;
    private ImageButton comment, collect, share, dropdown;
    private ImageView comment_usericon;
    private EditText edit_comment;
    private Post post;
    private FullPostCommentsFragment commentsFragment;
    private FullPostRatingsFragment ratingsFragment;
    private AddCommentDialog addCommentDialog;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_post);
        SwipeBackHelper.onCreate(this);

        intent = getIntent();
        post = intent.getParcelableExtra(POST);

        commentsFragment = new FullPostCommentsFragment();
        ratingsFragment = new FullPostRatingsFragment();
        addCommentDialog = new AddCommentDialog();
        include = findViewById(R.id.fullpost_post);
        poster_icon = include.findViewById(R.id.post_usericon);
        username = include.findViewById(R.id.post_username);
        time = include.findViewById(R.id.post_time);
        comments_counter = include.findViewById(R.id.post_comments_counter);
        content = include.findViewById(R.id.post_content);
        ratingBar = include.findViewById(R.id.post_ratingBar);
        comment = include.findViewById(R.id.post_comment);
        collect = include.findViewById(R.id.post_collect);
        share = include.findViewById(R.id.post_share);
        dropdown = include.findViewById(R.id.post_dropdown);
        swipeRefreshLayout = findViewById(R.id.fullpost_swipe_container);
        backButton = findViewById(R.id.fullpost_back);
        more = findViewById(R.id.fullpost_more);
        mViewPager = findViewById(R.id.fullpost_viewpager);
        mTabLayout = findViewById(R.id.fullpost_tablayout);
        comment_usericon = findViewById(R.id.fpac_user_icon);
        edit_comment = findViewById(R.id.fpac_editcomment);

        addCommentDialog.setCaller(edit_comment, commentsFragment);
        addCommentDialog.setPost(post);
        addCommentDialog.setOnSendListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_comment.setText("");
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                post = Post.powerfulFindPost(post);
                setPost(post);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenuHelper helper = new PopupMenuHelper(R.menu.fullpost_more_menu, FullPostActivity.this, more);
                if (User.user.hasPosted(post)){
                    helper.hideItem(R.id.fullpost_more_follow);
                }else{
                    helper.hideItem(R.id.fullpost_more_delete);
                    boolean isFollowing = User.user.isFollowing(post.getUserid());
                    helper.modifyIcon(R.id.fullpost_more_follow, isFollowing ? "Unfollow" : "Follow",isFollowing ? R.drawable.ic_unfollow : R.drawable.ic_follow);
                }
                helper.setOnItemSelectedListener(new PopupMenuHelper.onItemSelectListener() {
                    @Override
                    public boolean onItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.fullpost_more_follow:
                                User.user.follow(post.getUserid());
                                return true;
                            case R.id.fullpost_more_copy:
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Copy", content.getText());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(FullPostActivity.this,"Copied!",Toast. LENGTH_SHORT).show();
                                return true;
                            case R.id.fullpost_more_delete:
                                if (post.postedInGroup()) Group.findGroup(post.getGroupid()).removePost(post);
                                else User.user.removePost(post);
                                Intent intent = new Intent();
                                intent.putExtra(POST, post);
                                intent.putExtra(DELETE_POST, true);
                                setResult(RESULT_OK, intent);
                                finish();
                                overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
                                return true;
                            case R.id.fullpost_more_report:
                                return true;
                        }
                        return false;
                    }
                });
                helper.show();
            }
        });
        include.setClickable(false);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                mViewPager.setCurrentItem(position, true);
                tab.select();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPager.setSwipeable(false);
        poster_icon.setClickable(true);
        poster_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FullPostActivity.this, UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.USERID, post.getUserid());
                startActivity(intent);
                overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
            }
        });
        FirebaseHelper.setIcon(User.user.GetIcon(), this, comment_usericon);
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddComment();
            }
        });
        dropdown.setVisibility(View.GONE);
        edit_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddComment();
            }
        });
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (post.getUserid().equals(User.user.getId())){
                    Toast.makeText(FullPostActivity.this, "You can't rate your own post!", Toast.LENGTH_SHORT).show();
                    ratingBar.setRating(0);
                }else{
                    if (rating == 0)
                        ratingBar.setRating(post.ratings_avg());
                    else
                        User.user.rate(post, new Rating(User.user.getId(), (int) rating, Calendar.getInstance().getTimeInMillis(), post.GetKey()));
                }
            }
        });

        pagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(commentsFragment, "Comments");
        pagerAdapter.addFragment(ratingsFragment, "Ratings");
        mViewPager.setAdapter(pagerAdapter);
        setPost(post);
        if (intent.getBooleanExtra(ADD_COMMENT, false)) showAddComment();
    }

    private void setPost(final Post post){
        User user = User.findUser(post.getUserid());
        username.setText(user.getName());
        FirebaseHelper.setIcon(user.GetIcon(), this, poster_icon);
        time.setText(TimeText(post.getTime()));
        PostAdapter.setContent(this, content, post.getContent());
        Rating rating = post.hasRated();
        if (rating != null) ratingBar.setRating(rating.getRating());
        int comments_count = post.comments_recv();
        comments_counter.setText(comments_count + "");
        comments_counter.setVisibility(comments_count == 0 ? View.GONE : View.VISIBLE);
        commentsFragment.setData(post.getComments());
        ratingsFragment.setRating(post);
        setCollect(User.user.hasCollected(post));
        collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collect(post);
            }
        });
    }

    public static String TimeText(Long time){
        Calendar date = Calendar.getInstance(), now = Calendar.getInstance();
        date.setTimeInMillis(time);
        if (now.get(Calendar.YEAR) != date.get(Calendar.YEAR)) return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(time);
        else if (now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)) return new SimpleDateFormat("HH:mm").format(time);
        return new SimpleDateFormat("MM-dd HH:mm").format(time);
    }

    public void collect(Post post){
        boolean collected = User.user.hasCollected(post);
        if (collected) User.user.uncollect(post);
        else User.user.collect(post);
        setCollect(!collected);
    }
    public void setCollect(boolean collected){
        collect.setImageResource(collected ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
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

    private void showAddComment(){
        if (!addCommentDialog.isAdded())
            addCommentDialog.showNow(getSupportFragmentManager(), ADD_COMMENT);
    }
}
