package edu.ucsb.cs184.moments.moments;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Post> posts = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.view_post, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    private void add_element(Post post){
        posts.add(0, post);
    }
    public void addElements(ArrayList<Post> data){
        for (int i = 0; i < data.size(); i++) add_element(data.get(i));
        notifyDataSetChanged();
    }
    public void addElement(Post post){
        add_element(post);
        notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Post post = posts.get(position);
        holder.setPost(post);
    }

    public static String TimeText(Date date){
        Date now = new Date();
        long delta_sec = (now.getTime() - date.getTime()) / 1000;
        if (delta_sec == 0) return "Just now";
        else if (delta_sec < 60) return delta_sec + " second"+ ((delta_sec == 1) ? "" : "s") +" ago";
        long delta_min = delta_sec / 60;
        if (delta_min < 60) return delta_min + " minute"+ ((delta_min == 1) ? "" : "s") +" ago";
        long delta_hour = delta_min / 60;
        if (delta_hour < 24) return delta_hour + " hour"+ ((delta_hour == 1) ? "" : "s") +" ago";
        long delta_day = delta_hour / 24;
        if (delta_day < 7) return delta_day + " day"+ ((delta_hour == 1) ? "" : "s") +" ago";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
//        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView usericon;
        public TextView username;
        public TextView time;
        public TextView content;
        public RatingBar ratingBar;
        public ImageButton comment;
        public ImageButton collect;
        public ImageButton share;
        private Post post;

        public ViewHolder(final View view) {
            super(view);
            usericon = view.findViewById(R.id.post_usericon);
            username = view.findViewById(R.id.post_username);
            time = view.findViewById(R.id.post_time);
            content = view.findViewById(R.id.post_content);
            ratingBar = view.findViewById(R.id.post_ratingBar);
            comment = view.findViewById(R.id.post_comment);
            collect = view.findViewById(R.id.post_collect);
            share = view.findViewById(R.id.post_share);
            usericon.setClickable(true);
            usericon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            collect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCollect(!post.isCollected(User.user.getUserid()));
                }
            });
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, FullPostActivity.class);
                    intent.putExtra(FullPostActivity.POST, post);
                    context.startActivity(intent);
                }
            });
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, FullPostActivity.class);
                    intent.putExtra(FullPostActivity.POST, post);
                    intent.putExtra(FullPostActivity.ADD_COMMENT, FullPostActivity.ADD_COMMENT);
                    context.startActivity(intent);
                }
            });
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (!fromUser) return;
                    if (post.getUserid() == User.user.getUserid()){
                        Toast.makeText(view.getContext(), "You can't rate your own post!", Toast.LENGTH_SHORT).show();
                        ratingBar.setRating(post.ratings_avg());
                    }else{
                        User.user.rate(new Rating(User.user.getUserid(), (int) rating, new Date(), post.getKey()));
                        if (rating == 0) ratingBar.setRating(post.ratings_avg());
                    }
                }
            });
        }

        public void setPost(Post post) {
            this.post = post;
            User user = User.findUser(post.getUserid());
            usericon.setImageBitmap(user.getIcon());
            username.setText(user.getUsername());
            time.setText(TimeText(post.getTime()));
            content.setText(post.getContent());
            ratingBar.setRating(post.ratings_avg());
            setCollect(post.isCollected(User.user.getUserid()));
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        public void setCollect(boolean Collect){
            if (Collect){
                User.user.collect(post);
            }else{
                User.user.uncollect(post);
            }
            collect.setImageResource(Collect ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
        }
    }
}
