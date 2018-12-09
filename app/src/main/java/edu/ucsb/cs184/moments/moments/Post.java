package edu.ucsb.cs184.moments.moments;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Comparator;

public class Post implements Parcelable {
    private String userid;
    private String content;
    private Long time;
    private String groupid;
    private ArrayList<Comment> comments = new ArrayList<>();
    private ArrayList<Rating> ratings = new ArrayList<>();

    public Post() {}
    public Post(String userid, String content, Long time){
        this.userid = userid;
        this.content = content;
        this.time = time;
    }

    public Post(String userid, String content, Long time, String groupid){
        this.userid = userid;
        this.content = content;
        this.time = time;
        this.groupid = groupid;
    }

    protected Post(Parcel in) {
        userid = in.readString();
        content = in.readString();
        if (in.readByte() == 0) {
            time = null;
        } else {
            time = in.readLong();
        }
        comments = in.createTypedArrayList(Comment.CREATOR);
        ratings = in.createTypedArrayList(Rating.CREATOR);
        groupid = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public Key GetKey() { return new Key(userid, time); }
    public String getUserid() { return userid; }
    public String getGroupid() { return groupid; }
    public String getContent() { return content; }
    public Long getTime() { return time; }
    public int comments_count() { return comments.size(); }
    public ArrayList<Comment> getComments() { return comments; }
    public int ratings_received() { return ratings.size(); }
    public Rating hasRated() {
        for (Rating rating: ratings)
            if (rating.getRaterId().equals(User.user.getId()))
                return rating;
        return null;
    }
    public float ratings_avg(){
        float sum = 0;
        int count = ratings.size();
        if (count == 0) return 0f;
        for (int i = 0; i < count; i++)
            sum += ratings.get(i).getRating();
        return sum / count;
    }
    public float ratings_avg(int digit){
        return Float.parseFloat(String.format("%." + digit + "f", ratings_avg()));
    }
    public int counting_star(int stars){
        int count = 0;
        for (Rating rating: ratings)
            if (rating.getRating() == stars)
                count++;
        return count;
    }
    public boolean IsAnonymous() { return userid.equals(User.ANONYMOUS); }
    public boolean postedInGroup() { return groupid != null; }
    public boolean containsKeyword(String keyword) { return content.contains(keyword); }
    public void addComment(Comment comment){
        comments.add(comment);
        upload("comments", comments);
    }
    public void addRating(Rating rating){
        ratings.add(0, rating);
        upload("ratings", ratings);
    }
    public boolean removeComment(Comment comment){
        boolean result = comments.remove(comment);
        if (result) upload("comments", comments);
        return result;
    }
    public boolean removeRating(Rating rating){
        boolean result = ratings.remove(rating);
        if (result) upload("ratings", ratings);
        return result;
    }
    private void upload(String key, Object value){
        FirebaseHelper.updatePost(this, key, value);
    }

    @Override
    public String toString(){
        return (new Gson()).toJson(this);
    }
    public static Post fromJson(String json){
        return (new Gson()).fromJson(json, Post.class);
    }
    public static Post refresh(Post post){
        post = post.postedInGroup() ? findPost(post.GetKey(), post.groupid) : findPost(post.GetKey());
        return post;
    }
    public static Post findPost(Key key) { return FirebaseHelper.findPost(key); }
    public static Post findPost(Key key, String groupid) { return FirebaseHelper.findPostInGroup(key, groupid); }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Post)) return false;
        return GetKey().equals(((Post) obj).GetKey());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userid);
        dest.writeString(content);
        if (time == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(time);
        }
        dest.writeTypedList(comments);
        dest.writeTypedList(ratings);
        dest.writeString(groupid);
    }

    public static class Key implements Parcelable {
        String userid;
        Long time;
        public Key() {}
        public Key(String userid, Long time){
            this.userid = userid;
            this.time = time;
        }

        protected Key(Parcel in) {
            userid = in.readString();
            if (in.readByte() == 0) {
                time = null;
            } else {
                time = in.readLong();
            }
        }

        public static final Creator<Key> CREATOR = new Creator<Key>() {
            @Override
            public Key createFromParcel(Parcel in) {
                return new Key(in);
            }

            @Override
            public Key[] newArray(int size) {
                return new Key[size];
            }
        };

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof Key))
                return false;
            Key k = (Key) obj;
            return time.equals(k.time) && userid.equals(k.userid);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(userid);
            dest.writeLong(time);
        }
    }

    public static class PostComparator implements Comparator<Post> {
        @Override
        public int compare(Post o1, Post o2) { return new TimeComparator().compare(o1.GetKey(), o2.GetKey()); }
    }

    public static class RatingComparator implements Comparator<Post> {
        @Override
        public int compare(Post o1, Post o2) {
            int result = Double.compare(o1.ratings_avg(), o2.ratings_avg());
            for (int i = 1; i <= 5; i++){
                result = o1.counting_star(i) - o2.counting_star(i);
                if (result != 0)
                    return result;
            }
            return new TimeComparator().compare(o1.GetKey(), o2.GetKey());
        }
    }

    public static class PopularityComparator implements Comparator<Post>{
        @Override
        public int compare(Post o1, Post o2) {
            return new RatingComparator().compare(o1, o2);
        }
    }

    public static class TimeComparator implements Comparator<Key> {
        @Override
        public int compare(Key o1, Key o2) {
            return Long.compare(o2.time, o1.time) ;
        }
    }
}
