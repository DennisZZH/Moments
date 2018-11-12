package edu.ucsb.cs184.moments.moments;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;

public class Post {
    private int userid;
    private String content;
    private Date date;

    private ArrayList<Rating> ratings = new ArrayList<>();
    private ArrayList<Comment> comments = new ArrayList<>();

    public Post(int userid, String content, Date date){
        this.userid = userid;
        this.content = content;
        this.date = date;
    }

    public int getUserid() { return userid; }
    public String getContent() { return content; }
    public Date getDate() { return date; }
    public int comments_received() { return comments.size(); }
    public ArrayList<Comment> getComments() { return  comments; }
    public int ratings_received() { return ratings.size(); }
    public float ratings_avg(){
        float sum = 0;
        int count = ratings.size();
        if (count == 0) return 0f;
        for (int i = 0; i < count; i++)
            sum += ratings.get(i).getRating();
        return sum / count;
    }
    public Boolean isAnonymous() { return userid == User.anonymous; }
    public Boolean isCollected(int userid) {
        User user = User.findUser(userid);
        ArrayList<Post> posts = user.getCollections();
        for (int i = 0; i < posts.size(); i++) if (this.equals(posts.get(i))) return true;
        return false;
    }
    public void addComment(Comment comment) { comments.add(comment); }
    public void addRating(Rating rating) { ratings.add(rating); }

    @Override
    public String toString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Post toPost(String json){
        Gson gson = new Gson();
        return gson.fromJson(json, Post.class);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Post))
            return false;
        Post p = (Post) obj;
        return date == p.date && userid == p.userid;
    }

    public static Post TestPost(){ return new Post(0, new Date().toString(), new Date());}
}