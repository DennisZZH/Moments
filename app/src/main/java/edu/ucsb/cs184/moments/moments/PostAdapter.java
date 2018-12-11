package edu.ucsb.cs184.moments.moments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.view.menu.MenuBuilder;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PostAdapter extends CustomAdapter {

    public static final int FULL_POST = 1;
    private boolean usericonClickable = true;
    public void setUsericonClickable(boolean clickable) { usericonClickable = clickable; }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_post, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    public static String TimeText(Long time){
        Calendar date = Calendar.getInstance(), now = Calendar.getInstance();
        date.setTimeInMillis(time);
        long delta_sec = (now.getTimeInMillis() - time) / 1000;
        if (delta_sec == 0) return "Just now";
        else if (delta_sec < 60) return delta_sec + " second"+ ((delta_sec == 1) ? "" : "s") +" ago";
        long delta_min = delta_sec / 60;
        if (delta_min < 60) return delta_min + " minute"+ ((delta_min == 1) ? "" : "s") +" ago";
        long delta_hour = delta_min / 60;
        if (delta_hour < 24) return delta_hour + " hour"+ ((delta_hour == 1) ? "" : "s") +" ago";
        long delta_day = delta_hour / 24;
        if (delta_day < 7) return delta_day + " day"+ ((delta_day == 1) ? "" : "s") +" ago";
        return new SimpleDateFormat("yyyy-MM-dd").format(time);
    }

    public static void setContent(final Context context, TextView textView, final String content){
        int at, hashtag, start = 0, end, length = content.length();
        SpannableString spannableString = new SpannableString(content);
        while (true){
            at = content.substring(start).indexOf("@");
            hashtag = content.substring(start).indexOf("#");
            if (at < 0 && hashtag < 0) break;
            if (at != -1) at += start;
            if (hashtag != -1) hashtag += start;
            start = (at < 0) ? hashtag : ((at < hashtag || hashtag < 0) ? at : hashtag);
            final boolean isAt = start == at;
            // if last char, break
            if (start + 1 == length) break;
            if (isAt){
                end = content.substring(start + 1).indexOf(" ");
                if (end == -1) end = length;
                else end += start + 1;
            }else{
                end = content.substring(start + 1).indexOf("#");
                if (end == -1) break;
                else end += start + 1;
            }
            String word = content.substring(start + 1, end);
            if (!word.isEmpty()){
                final String target = isAt ? word : ("#" + word + "#");
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        if (isAt){
                            User user = User.findUserWithName(target);
                            if (user == null)
                                Toast.makeText(context, "User " + target + " not found!" , Toast.LENGTH_SHORT).show();
                            else{
                                Intent intent = new Intent(context, UserProfileActivity.class);
                                intent.putExtra(UserProfileActivity.USERID, user.getId());
                                context.startActivity(intent);
                                ((Activity) context).overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
                            }
                        }else{
                            Intent intent = new Intent(context, SearchActivity.class);
                            intent.putExtra(SearchActivity.TAB, SearchPair.POSTS);
                            intent.putExtra(SearchActivity.KEYWORD, target);
                            context.startActivity(intent);
                            ((Activity) context).overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(context.getColor(R.color.DeepBlue));
                        ds.setUnderlineText(false);
                    }
                };
                spannableString.setSpan(clickableSpan, start, end + (isAt ? 0 : 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (end + 1 >= length) break;
            start = end + 1;
        }
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public class ViewHolder extends CustomAdapter.CustomViewHolder {
        public ImageView usericon;
        public TextView username, time, content, comments_counter;
        public RatingBar ratingBar;
        public ImageButton comment, collect, share, dropdown;
        private Post data;

        public ViewHolder(final View view) {
            super(view);
            usericon = view.findViewById(R.id.post_usericon);
            username = view.findViewById(R.id.post_username);
            time = view.findViewById(R.id.post_time);
            content = view.findViewById(R.id.post_content);
            comments_counter = view.findViewById(R.id.post_comments_counter);
            ratingBar = view.findViewById(R.id.post_ratingBar);
            comment = view.findViewById(R.id.post_comment);
            collect = view.findViewById(R.id.post_collect);
            share = view.findViewById(R.id.post_share);
            dropdown = view.findViewById(R.id.post_dropdown);
        }

        public void setData(Object object) {
            data = (Post) object;
            final User user = User.findUser(data.getUserid());
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FullPostActivity.class);
                    intent.putExtra(FullPostActivity.POST, data);
                    // TODO: these two lines are problematic
                    activity.startActivityForResult(intent, FULL_POST);
                    activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
                }
            });
            usericon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!usericonClickable || data.IsAnonymous()) return;
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra(UserProfileActivity.USERID, data.getUserid());
                    // TODO: these two lines are problematic
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
                }
            });
            collect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collect(!User.user.hasCollected(data));
                }
            });
            int comments_count = data.comments_recv();
            comments_counter.setText(comments_count + "");
            comments_counter.setVisibility(comments_count == 0 ? View.GONE : View.VISIBLE);
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FullPostActivity.class);
                    intent.putExtra(FullPostActivity.POST, data);
                    intent.putExtra(FullPostActivity.ADD_COMMENT, true);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
                }
            });
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (!fromUser) return;
                    if (User.user.isUser(data.getUserid())){
                        Toast.makeText(context, "You can't rate your own post!", Toast.LENGTH_SHORT).show();
                        ratingBar.setRating(data.ratings_avg());
                    }else{
                        User.user.rate(data, new Rating(User.user.getId(), (int) rating, Calendar.getInstance().getTimeInMillis(), data.GetKey()));
                        if (rating == 0f) ratingBar.setRating(data.ratings_avg());
                    }
                }
            });
            dropdown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ObjectAnimator.ofFloat(v, "rotation", 0, 180).start();
                    final PopupMenuHelper helper = new PopupMenuHelper(R.menu.post_more_menu, context, dropdown);
                    if (data.getUserid().equals(User.user.getId())){
                        helper.hideItem(R.id.post_more_follow);
                    }else{
                        helper.hideItem(R.id.post_more_delete);
                        boolean isFollowing = User.user.isFollowing(data.getUserid());
                        helper.modifyIcon(R.id.post_more_follow, isFollowing ? "Unfollow" : "Follow",isFollowing ? R.drawable.ic_unfollow : R.drawable.ic_follow);
                    }
                    helper.setOnItemSelectedListener(new PopupMenuHelper.onItemSelectListener() {
                        @Override
                        public boolean onItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                            switch (menuItem.getItemId()){
                                case R.id.post_more_follow:
                                    User.user.follow(data.getUserid());
                                    return true;
                                case R.id.post_more_delete:
                                    User.user.removePost(data);
                                    removeElement(data);
                                    return true;
                                case R.id.post_more_report:
                                    return true;
                            }
                            return false;
                        }
                    });
                    helper.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            ObjectAnimator.ofFloat(v, "rotation", 180, 360).start();
                        }
                    });
                    helper.show();
                }
            });
            FirebaseHelper.setIcon(user.GetIcon(), activity, usericon);
            username.setText(user.getName());
            time.setText(TimeText(data.getTime()));
            setContent(context, content, data.getContent());
            Rating rating = data.hasRated();
            ratingBar.setRating((rating == null) ? data.ratings_avg() : rating.getRating());
            setCollect(User.user.hasCollected(data));
        }


        public void setCollect(boolean Collect){
            collect.setImageResource(Collect ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
        }

        public void collect(boolean Collect){
            if (Collect) User.user.collect(data);
            else User.user.uncollect(data);
            setCollect(Collect);
        }
    }
}
