package edu.ucsb.cs184.moments.moments;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

public class User implements Parcelable {
    public static final String ANONYMOUS = "ANONYMOUS", UNKNOWN = "Unknown", MALE = "Male", FEMALE = "Female";
    public static User user;
    public static FirebaseUser firebaseUser;
    private String id;
    private String name;
    private String intro = "";
    private Bitmap icon;
    private int user_number = 0;
    private String gender = "Unknown";
    private ArrayList<Post> posts = new ArrayList<>();
    private ArrayList<Post> drafts = new ArrayList<>();
    private ArrayList<Post.Key> collections = new ArrayList<>();
    private ArrayList<Comment> commentsMade = new ArrayList<>();
    private ArrayList<Comment.Key> commentsRecv = new ArrayList<>();
    private ArrayList<Rating> ratingsMade = new ArrayList<>();
    private ArrayList<Rating.Key> ratingsRecv = new ArrayList<>();
    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayList<String> groups = new ArrayList<>();  // id
    private ArrayList<String> followers = new ArrayList<>();  // id
    private ArrayList<String> following = new ArrayList<>();  // id
    private ArrayList<Comment.Key> commentsNotification = new ArrayList<>();
    private ArrayList<Post.Key> postsNotification = new ArrayList<>();
    private ArrayList<Rating.Key> ratingsNotification = new ArrayList<>();
    private ArrayList<Post.Key> timeline = new ArrayList<>();
    private ArrayList<String> searchHistory = new ArrayList<>();

    public User(){}

    public User(String id, String name){
        this.name = name;
        this.id = id;
    }

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        intro = in.readString();
        icon = in.readParcelable(Bitmap.class.getClassLoader());
        user_number = in.readInt();
        gender = in.readString();
        posts = in.createTypedArrayList(Post.CREATOR);
        drafts = in.createTypedArrayList(Post.CREATOR);
        collections = in.createTypedArrayList(Post.Key.CREATOR);
        commentsMade = in.createTypedArrayList(Comment.CREATOR);
        commentsRecv = in.createTypedArrayList(Comment.Key.CREATOR);
        ratingsMade = in.createTypedArrayList(Rating.CREATOR);
        ratingsRecv = in.createTypedArrayList(Rating.Key.CREATOR);
        groups = in.createStringArrayList();
        followers = in.createStringArrayList();
        following = in.createStringArrayList();
        commentsNotification = in.createTypedArrayList(Comment.Key.CREATOR);
        postsNotification = in.createTypedArrayList(Post.Key.CREATOR);
        ratingsNotification = in.createTypedArrayList(Rating.Key.CREATOR);
        timeline = in.createTypedArrayList(Post.Key.CREATOR);
        searchHistory = in.createStringArrayList();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public void setNumber(int number){ this.user_number = number; }
    public int getNumber() { return user_number; }
    public Bitmap getIcon() { return icon; }
    public String getName() { return name; }
    public String getId() { return id; }
    public String getIntro() { return intro; }
    public String getGender() { return gender; }
    public ArrayList<Message> getMessages() { return messages; }
    public ArrayList<Group> getGroups() {
        ArrayList<Group> data = new ArrayList<>();
        for (String gid: groups) data.add(Group.findGroup(gid));
        return data;
    }
    public ArrayList<Post> getPosts() { return posts; }
    public ArrayList<Post> getDrafts() { return drafts; }
    public ArrayList<Post> getCollections() {
        ArrayList<Post> data = new ArrayList<>();
        boolean update = false;
        for (Post.Key key: (ArrayList<Post.Key>) collections.clone()){
            Post post = Post.findPost(key);
            // Should let user know it's not found/deleted
            if (post == null){
                collections.remove(key);
                update = true;
            }
            else data.add(post);
        }
        if (update) upload("collections", collections);
        return data;
    }
    public ArrayList<Comment> getCommentsMade() { return commentsMade; }
    public ArrayList<String> getFollowers() { return followers; }
    public ArrayList<String> getFollowing() { return following; }
    public ArrayList<String> getSearchHistory() { return searchHistory; }
    public ArrayList<Comment> getCommentsRecv() {
        ArrayList<Comment> data = new ArrayList<>();
        for (Comment.Key key: commentsRecv){
            Comment comment = Comment.findComment(key);
            if (comment != null)
                data.add(comment);
        }
        return data;
    }
    public ArrayList<Post.Key> getPostsNotification() { return postsNotification; }
    public ArrayList<Comment.Key> getCommentsNotification() { return commentsNotification; }
    public ArrayList<Rating.Key> getRatingssNotification() { return ratingsNotification; }
    public boolean IsAnonymous() { return id.equals(ANONYMOUS); }
    public boolean inGroup(String groupid) { return groups.contains(groupid); }
    public boolean isFollowing(String userid) { return following.contains(userid); }
    public boolean hasCollected(Post post) { return collections.contains(post.GetKey()); }
    public boolean hasNewPost() { return postsNotification.size() != 0; }
    public boolean hasNewComment() { return commentsNotification.size() != 0; }
    public boolean hasNewRating() { return ratingsNotification.size() != 0; }
    public ArrayList<Post.Key> getPostKeys() {
        ArrayList<Post.Key> data = new ArrayList<>();
        for (Post post: posts) data.add(post.GetKey());
        return data;
    }
    public ArrayList<Post> getTimeline(){
        ArrayList<Post> data = new ArrayList<>();
        boolean remove = false;
        for (Post.Key key: (ArrayList<Post.Key>) timeline.clone()){
            Post post = Post.findPost(key);
            if (post == null){
                timeline.remove(key);
                remove = true;
            }
            else data.add(post);
        }
        boolean fix = false;
        for (Post post: posts){
            if (!data.contains(post)){
                data.add(post);
                timeline.add(post.GetKey());
                fix = true;
            }
        }
        if (fix){
            Collections.sort(data, new Post.PostComparator());
            Collections.sort(timeline, new Post.TimeComparator());
        }
        if (remove || fix) upload("timeline", timeline);
        return data;
    }
    public void PostNotification(Post post, boolean remove){
        if (remove && postsNotification.contains(post.GetKey())) postsNotification.remove(post.GetKey());
        else postsNotification.add(0, post.GetKey());
        upload("postsNotification", postsNotification);
    }
    public void CommentNotification(Comment comment, boolean remove){
        if (remove && commentsNotification.contains(comment.GetKey())) commentsNotification.remove(comment.GetKey());
        else commentsNotification.add(0, comment.GetKey());
        upload("commentsNotification", commentsNotification);
    }
    public void RatingNotification(Rating rating){
        boolean update = false;
        for (Rating.Key key: (ArrayList<Rating.Key>) ratingsNotification.clone()){
            if (rating.GetKey().equals(key)){
                if (rating.getRating() == 0) ratingsNotification.remove(key);
                else ratingsNotification.set(ratingsNotification.indexOf(key), rating.GetKey());
                update = true;
                break;
            }
        }
        if(!update){
            ratingsNotification.add(0, rating.GetKey());
        }
        upload("ratingsNotification", commentsNotification);
    }
    public void FollowerNotification(String followerid, boolean remove){
        if (remove) followers.remove(followerid);
        else followers.add(0, followerid);
        upload("followers", followers);
    }
    public void refreshTimeline(){
        timeline.addAll(postsNotification);
        Collections.sort(timeline, new Post.TimeComparator());
        upload("timeline", timeline);
        postsNotification.clear();
        upload("postsNotification", postsNotification);
    }
    public void refreshGroups(){

    }
    public void refreshCommentsRecv(){
        commentsRecv.addAll(commentsNotification);
        Collections.sort(commentsRecv, new Comment.TimeComparator());
        upload("commentsRecv", commentsRecv);
        commentsNotification.clear();
        upload("commentsNotification", commentsNotification);
    }
    public void refreshRatingsRecv(){
        ratingsRecv.addAll(ratingsNotification);
        Collections.sort(ratingsRecv, new Rating.TimeComparator());
        upload("ratingsRecv", ratingsRecv);
        ratingsNotification.clear();
        upload("ratingsNotification", ratingsNotification);
    }
    public void setName(String name){
        this.name = name;
        upload("name", name);
    }
    public void setIcon(Bitmap icon){
        this.icon = icon;
        upload("icon", icon);
    }
    public void setIntro(String intro){
        this.intro = intro;
        upload("intro", intro);
    }
    public void setGender(String gender){
        this.gender = gender;
        upload("gender", gender);
    }
    public void addHistory(String history){
        searchHistory.remove(history);
        searchHistory.add(0, history);
        upload("searchHistory", searchHistory);
    }
    public void removeHistory(String content){
        searchHistory.remove(content);
        upload("searchHistory", searchHistory);
    }
    public void makePost(Post post){
        posts.add(0, post);
        upload("posts", posts);
        timeline.add(0, post.GetKey());
        upload("timeline", timeline);
        for (String id: followers){
            findUser(id).PostNotification(post, false);
        }
    }
    public void removePost(Post post){
        posts.remove(post);
        upload("posts", posts);
        timeline.remove(post.GetKey());
        upload("timeline", timeline);
        for (String id: followers){
            findUser(id).PostNotification(post, true);
        }
        if (hasCollected(post)){
            collections.remove(post.GetKey());
            upload("collections", collections);
        }
    }
    public void makeComment(Comment comment) {
        commentsMade.add(0, comment);
        Post.findPost(comment.getPostKey()).addComment(comment);
        upload("commentsMade", commentsMade);
        findUser(comment.getUserid()).CommentNotification(comment, false);
    }
    public void removeComment(Comment comment, boolean made) {
        commentsMade.remove(comment);
        Post.findPost(comment.getPostKey()).removeComment(comment);
        upload("commentsMade", commentsMade);
        findUser(comment.getUserid()).CommentNotification(comment, true);
    }
    public void follow(String userid) {
        following.add(0, userid);
        upload("following", following);
        User user = findUser(userid);
        user.FollowerNotification(User.user.id, false);
        timeline.addAll(0, user.getPostKeys());
        Collections.sort(timeline, new Post.TimeComparator());
        upload("timeline", timeline);
    }
    public void unfollow(String userid) {
        following.remove(userid);
        upload("following", following);
        for (Post.Key postKey: (ArrayList<Post.Key>) timeline.clone()){
            if (postKey.userid.equals(userid)) timeline.remove(postKey);
        }
        upload("timeline", timeline);
        User user = findUser(userid);
        user.FollowerNotification(User.user.id, true);
    }
    public void createGroup(String groupid){
        groups.add(0, groupid);
        upload("groups", groups);
    }
    public void joinGroup(String groupid){
        groups.add(0, groupid);
        upload("groups", groups);
        FirebaseHelper.findGroup(groupid).addMember(user.id);
    }
    public void quitGroup(String groupid){
        groups.remove(groupid);
        upload("groups", groups);
        FirebaseHelper.findGroup(groupid).removeMember(user.id);
    }
    public void collect(Post post){
        collections.add(0, post.GetKey());
        upload("collections", collections);
    }
    public void uncollect(Post post){
        collections.remove(post.GetKey());
        upload("collections", collections);
    }
    public void rate(Rating rating){
        boolean remove = rating.getRating() == 0;
        findUser(rating.getUserid()).RatingNotification(rating);
        if (remove) Post.findPost(rating.GetPostKey()).removeRating(rating);
        else Post.findPost(rating.GetPostKey()).addRating(rating);
    }
    public void saveAsDraft(Post post){
        drafts.add(0, post);
    }
    private void upload(String key, Object value){
        if (User.user != null && User.user.id.equals(id))
            FirebaseHelper.updateUser(key, value);
    }

    @Override
    public String toString(){
        return (new Gson()).toJson(this);
    }
    public static User fromJson(String json){
        return (new Gson()).fromJson(json, User.class);
    }
    public static User findUser(String id){
        if (User.user != null && User.user.getId().equals(id)) return User.user;
        return FirebaseHelper.findUser(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(intro);
        dest.writeParcelable(icon, flags);
        dest.writeInt(user_number);
        dest.writeString(gender);
        dest.writeTypedList(posts);
        dest.writeTypedList(drafts);
        dest.writeTypedList(collections);
        dest.writeTypedList(commentsMade);
        dest.writeTypedList(commentsRecv);
        dest.writeTypedList(ratingsMade);
        dest.writeTypedList(ratingsRecv);
        dest.writeStringList(groups);
        dest.writeStringList(followers);
        dest.writeStringList(following);
        dest.writeTypedList(commentsNotification);
        dest.writeTypedList(postsNotification);
        dest.writeTypedList(ratingsNotification);
        dest.writeTypedList(timeline);
        dest.writeStringList(searchHistory);
    }
}
