package edu.ucsb.cs184.moments.moments;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Comparator;

public class Comment implements Parcelable {
    private String userid; // Commentator
    private String content;
    private Long time;
    private Post.Key postKey;
//    private Comment.Key parent;

    private ArrayList<Comment> replies = new ArrayList<>();
    private ArrayList<Rating> ratings = new ArrayList<>();

    public Comment(){}
    public Comment(String userid, String content, Long time, Post.Key postKey){
        this.userid = userid;
        this.content = content;
        this.time = time;
        this.postKey = postKey;
    }
    public Comment(String userid, String content, Long time, Comment.Key parent){
        this.userid = userid;
        this.content = content;
        this.time = time;
        this.postKey = null;
//        this.parent = parent;
    }

    protected Comment(Parcel in) {
        userid = in.readString();
        content = in.readString();
        if (in.readByte() == 0) {
            time = null;
        } else {
            time = in.readLong();
        }
        postKey = in.readParcelable(Post.Key.class.getClassLoader());
        replies = in.createTypedArrayList(Comment.CREATOR);
        ratings = in.createTypedArrayList(Rating.CREATOR);
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
        dest.writeParcelable(postKey, flags);
        dest.writeTypedList(replies);
        dest.writeTypedList(ratings);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public int replies_received() { return replies.size(); }
    public ArrayList<Comment> getComments() { return  replies; }
    public int ratings_received() { return ratings.size(); }
    public float ratings_avg(){
        float sum = 0;
        int count = ratings.size();
        if (count == 0) return 0f;
        for (int i = 0; i < count; i++)
            sum += ratings.get(i).getRating();
        return sum / count;
    }
    public void addReply(Comment reply){
        replies.add(reply);
    }
    public void removeReply(Comment reply){
        replies.remove(reply);
    }
    public String getUserid() { return userid; }
    public String GetPosterId() { return postKey.userid; }
    public String getContent() { return content; }
    public Long getTime() { return time; }
    public Key GetKey() { return new Key(userid, time, postKey); }
    public Post.Key getPostKey() { return postKey; }
//    public Comment.Key getParent() { return parent; }
//    public Boolean hasParent() { return parent == null; }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Comment)) return false;
        return GetKey().equals(((Comment) obj).GetKey());
    }
    @Override
    public String toString(){
        return (new Gson()).toJson(this);
    }
    public static Comment fromJson(String json){
        return (new Gson()).fromJson(json, Comment.class);
    }
    public static Comment findComment(Key key) { return FirebaseHelper.findComment(key); }

    public static class Key implements Parcelable{
        public String userid;
        public Long time;
        public Post.Key postKey;
        public Key() {}
        public Key(String userid, Long time, Post.Key postKey){
            this.userid = userid;
            this.time = time;
            this.postKey = postKey;
        }

        protected Key(Parcel in) {
            userid = in.readString();
            time = in.readLong();
            postKey = in.readParcelable(Post.Key.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(userid);
            dest.writeLong(time);
            dest.writeParcelable(postKey, flags);
        }

        @Override
        public int describeContents() {
            return 0;
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
            return postKey.equals(k.postKey) && userid.equals(k.userid);
        }
    }
    public static class CommentComparator implements Comparator<Comment> {
        @Override
        public int compare(Comment o1, Comment o2) {
            return new TimeComparator().compare(o1.GetKey(), o2.GetKey());
        }
    }
    public static class TimeComparator implements Comparator<Key> {
        @Override
        public int compare(Key o1, Key o2) {
            return Long.compare(o2.time, o1.time) ;
        }
    }
}
