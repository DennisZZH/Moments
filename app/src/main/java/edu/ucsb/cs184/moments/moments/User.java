package edu.ucsb.cs184.moments.moments;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
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
    private String icon;
    private String gender = "Unknown";
    private ArrayList<Post> posts = new ArrayList<>();
    private ArrayList<Post> drafts = new ArrayList<>();
    private ArrayList<Post.Key> collections = new ArrayList<>();
    private ArrayList<Comment.Key> commentsRecv = new ArrayList<>();
    private ArrayList<Rating.Key> ratingsRecv = new ArrayList<>();
    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayList<String> groups = new ArrayList<>();  // id
    private ArrayList<String> followers = new ArrayList<>();  // id
    private ArrayList<String> following = new ArrayList<>();  // id
    private ArrayList<Post.Key> atMe = new ArrayList<>();
    // TODO: at me comments
    private ArrayList<Comment.Key> commentsNotification = new ArrayList<>();
    private ArrayList<Post.Key> postsNotification = new ArrayList<>();
    private ArrayList<Rating.Key> ratingsNotification = new ArrayList<>();
    private ArrayList<Post.Key> timeline = new ArrayList<>();
    private ArrayList<SearchPair> searchHistory = new ArrayList<>();

    public User(){}

    public User(String id, String name){
        this.name = name;
        this.id = id;
        this.icon = "user_icon.jpg";
    }

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        intro = in.readString();
        icon = in.readString();
        gender = in.readString();
        posts = in.createTypedArrayList(Post.CREATOR);
        drafts = in.createTypedArrayList(Post.CREATOR);
        collections = in.createTypedArrayList(Post.Key.CREATOR);
        commentsRecv = in.createTypedArrayList(Comment.Key.CREATOR);
        ratingsRecv = in.createTypedArrayList(Rating.Key.CREATOR);
        messages = in.createTypedArrayList(Message.CREATOR);
        groups = in.createStringArrayList();
        followers = in.createStringArrayList();
        following = in.createStringArrayList();
        atMe = in.createTypedArrayList(Post.Key.CREATOR);
        commentsNotification = in.createTypedArrayList(Comment.Key.CREATOR);
        postsNotification = in.createTypedArrayList(Post.Key.CREATOR);
        ratingsNotification = in.createTypedArrayList(Rating.Key.CREATOR);
        timeline = in.createTypedArrayList(Post.Key.CREATOR);
        searchHistory = in.createTypedArrayList(SearchPair.CREATOR);
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

    public StorageReference GetIcon() {
        if (icon == null) icon = "user_icon.jpg";
        return FirebaseHelper.getIcon(FirebaseHelper.USER_ICON, icon);
    }
    public void modifyIcon(Bitmap bitmap){
        this.icon = id + ".jpg";
        upload("icon", icon);
        FirebaseHelper.uploadIcon(bitmap, FirebaseHelper.USER_ICON, icon);
    }
    public String getName() { return name; }
    public void setName(String name){ this.name = name; }
    public String getId() { return id; }
    public String getIntro() { return intro; }
    public void setIntro(String intro){ this.intro = intro; }
    public String getGender() { return gender; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setGender(String gender){ this.gender = gender; }
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
    public ArrayList<String> getFollowers() { return followers; }
    public ArrayList<String> getFollowing() { return following; }
    public ArrayList<SearchPair> getSearchHistory() { return searchHistory; }
    public ArrayList<Comment> getCommentsRecv() {
        ArrayList<Comment> data = new ArrayList<>();
        for (Comment.Key key: (ArrayList<Comment.Key>) commentsRecv.clone()){
            Comment comment = Comment.findComment(key);
            if (comment != null) data.add(comment);
            else commentsRecv.remove(key);
        }
        return data;
    }
    public ArrayList<Rating> getRatingsRecv() {
        ArrayList<Rating> data = new ArrayList<>();
        for (Rating.Key key: (ArrayList<Rating.Key>) ratingsRecv.clone()){
            Rating rating = Rating.findRating(key);
            if (rating != null) data.add(rating);
            else ratingsRecv.remove(rating);
        }
        return data;
    }
    public ArrayList<Post> getAtMe() {
        ArrayList<Post> data = new ArrayList<>();
        for (Post.Key key: (ArrayList<Post.Key>) atMe.clone()){
            Post post = Post.powerfulFindPost(key);
            if (post == null) atMe.remove(key);
            else data.add(post);
        }
        return data;
    }
    public ArrayList<Post.Key> getPostsNotification() { return postsNotification; }
    public ArrayList<Comment.Key> getCommentsNotification() { return commentsNotification; }
    public ArrayList<Rating.Key> getRatingsNotification() { return ratingsNotification; }
    public boolean IsAnonymous() { return id.equals(ANONYMOUS); }
    public boolean mutualFollow(String userid) { return !id.equals(userid) && following.contains(userid) && findUser(userid).following.contains(id); }
    public boolean inGroup(String groupid) { return groups.contains(groupid); }
    public boolean isFollowing(String userid) { return !id.equals(userid) && following.contains(userid); }
    public boolean isUser(String userid) { return id.equals(userid); }
    public boolean hasCollected(Post post) { return collections.contains(post.GetKey()); }
    public boolean hasNewPost() { return postsNotification.size() > 0; }
    public boolean hasNewComment() { return commentsNotification.size() > 0; }
    public boolean hasNewRating() { return ratingsNotification.size() > 0; }
    public boolean hasDrafts() { return drafts.size() > 0; }
    public boolean hasPosted(Post post) { return id.equals(post.getUserid()); }
    public boolean containsKeyword(String keyword) { return name.contains(keyword); }
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
    public void AtMeNotification(Post post, boolean remove){
        if (remove) atMe.remove(post.GetKey());
        else atMe.add(0, post.GetKey());
        upload("atMe", atMe);
    }
    public void PostNotification(Post post, boolean remove){
        if (remove) postsNotification.remove(post.GetKey());
        else postsNotification.add(0, post.GetKey());
        upload("postsNotification", postsNotification);
    }
    public void CommentNotification(Comment comment, boolean remove){
        if (remove) commentsNotification.remove(comment.GetKey());
        else commentsNotification.add(0, comment.GetKey());
        upload("commentsNotification", commentsNotification);
    }
    public boolean RatingNotification(Rating rating, boolean remove){
        boolean result = ratingsNotification.remove(rating.GetKey());
        if (!remove) ratingsNotification.add(0, rating.GetKey());
        upload("ratingsNotification", ratingsNotification);
        return result;
    }
    public void FollowerNotification(String followerid, boolean remove){
        if (remove) followers.remove(followerid);
        else followers.add(0, followerid);
        upload("followers", followers);
    }
    public int refreshTimeline(){
        int count = postsNotification.size();
        timeline.addAll(0, postsNotification);
        Collections.sort(timeline, new Post.TimeComparator());
        upload("timeline", timeline);
        postsNotification.clear();
        upload("postsNotification", postsNotification);
        return count;
    }
    public void refreshGroups(){

    }
    public void refreshCommentsRecv(){
        for (Comment.Key key: (ArrayList<Comment.Key>) commentsNotification.clone())
            if (!key.postKey.userid.equals(id))
                commentsNotification.remove(key);
        commentsRecv.addAll(0, commentsNotification);
        Collections.sort(commentsRecv, new Comment.TimeComparator());
        upload("commentsRecv", commentsRecv);
        commentsNotification.clear();
        upload("commentsNotification", commentsNotification);
    }
    public void refreshRatingsRecv(){
        ratingsRecv.addAll(0, ratingsNotification);
        Collections.sort(ratingsRecv, new Rating.TimeComparator());
        upload("ratingsRecv", ratingsRecv);
        ratingsNotification.clear();
        upload("ratingsNotification", ratingsNotification);
    }
    public void modifyName(String name){
        this.name = name;
        upload("name", name);
    }
    public void modifyIntro(String intro){
        this.intro = intro;
        upload("intro", intro);
    }
    public void modifyGender(String gender){
        this.gender = gender;
        upload("gender", gender);
    }
    public boolean addHistory(SearchPair pair){
        boolean remove = searchHistory.remove(pair);
        searchHistory.add(0, pair);
        upload("searchHistory", searchHistory);
        return remove;
    }
    public void removeHistory(String history){
        for (int i = 0; i < searchHistory.size(); i++)
            if (searchHistory.get(i).keyword.equals(history))
                searchHistory.remove(i);
        upload("searchHistory", searchHistory);
    }
    public void clearDrafts(){
        drafts.clear();
        upload("drafts", drafts);
    }
    public void clearHistory(){
        searchHistory.clear();
        upload("searchHistory", searchHistory);
    }
    public void addPost(Post post){
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
    public void rate(Post post, Rating rating){
        boolean remove = rating.getRating() == 0;
        if (!user.isUser(post.getUserid()))
            findUser(post.getUserid()).RatingNotification(rating, remove);
        post.removeRating(rating);
        if (!remove) post.addRating(rating);
    }
    public void addDraft(Post post){
        drafts.add(0, post);
        upload("drafts", drafts);
    }
    public void removeDraft(Post post){
        drafts.remove(post);
        upload("drafts", drafts);
    }
    public void atMe(Post post){
        atMe.add(0, post.GetKey());
        upload("atMe", atMe);
    }
    private void upload(String key, Object value){
        FirebaseHelper.updateUser(id, key, value);
    }

    @Override
    public String toString(){
        return (new Gson()).toJson(this);
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof User)) return false;
        return id.equals(((User) obj).id);
    }

    public static User fromJson(String json){
        return (new Gson()).fromJson(json, User.class);
    }
    public static User findUser(String id){
        if (User.user != null && User.user.getId().equals(id)) return User.user;
        return FirebaseHelper.findUser(id);
    }
    public static User findUserWithName(String name){
        if (User.user != null && User.user.getName().equals(name)) return User.user;
        return FirebaseHelper.findUserWithName(name);
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
        dest.writeString(icon);
        dest.writeString(gender);
        dest.writeTypedList(posts);
        dest.writeTypedList(drafts);
        dest.writeTypedList(collections);
        dest.writeTypedList(commentsRecv);
        dest.writeTypedList(ratingsRecv);
        dest.writeTypedList(messages);
        dest.writeStringList(groups);
        dest.writeStringList(followers);
        dest.writeStringList(following);
        dest.writeTypedList(atMe);
        dest.writeTypedList(commentsNotification);
        dest.writeTypedList(postsNotification);
        dest.writeTypedList(ratingsNotification);
        dest.writeTypedList(timeline);
        dest.writeTypedList(searchHistory);
    }
}
