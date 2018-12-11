package edu.ucsb.cs184.moments.moments;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

public class Group implements Parcelable {
    private String id;
    private String managerid;
    private String name;
    private int group_number;
    private String icon = "group_icon.png";
    private String intro = "";
    // private group?
    private ArrayList<String> members = new ArrayList<>();
    private ArrayList<Post> posts = new ArrayList<>();
    private ArrayList<Message> messages = new ArrayList<>();

    public Group(){}

    public Group(String name, User manager){
        this.name = name;
        this.managerid = manager.getId();
        this.members.add(managerid);
        messages.add(new Message(manager.getName() + " created this group.", Calendar.getInstance().getTimeInMillis()));
    }

    protected Group(Parcel in) {
        id = in.readString();
        managerid = in.readString();
        name = in.readString();
        group_number = in.readInt();
        icon = in.readString();
        intro = in.readString();
        members = in.createStringArrayList();
        posts = in.createTypedArrayList(Post.CREATOR);
        messages = in.createTypedArrayList(Message.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(managerid);
        dest.writeString(name);
        dest.writeInt(group_number);
        dest.writeString(icon);
        dest.writeString(intro);
        dest.writeStringList(members);
        dest.writeTypedList(posts);
        dest.writeTypedList(messages);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public void setId(String id) { if (id != null) this.id = id; }
    public String getId() { return id; }
    public String getManagerid() { return managerid; }
    public void setManagerid(String userid) { this.managerid = userid; }
    public void modifyManager(String userid){
        this.managerid = userid;
        upload("managerid", managerid);
    }
    public boolean IsManager(String userid) { return managerid.equals(userid); }
    public void setNumber(int number) { this.group_number = number; }
    public int getNumber() { return group_number;}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public void modifyName(String name){
        this.name = name;
        upload("name", name);
    }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public void modifyIntro(String intro) {
        this.intro = intro;
        upload("intro", intro);
    }
    public ArrayList<String> getMembers() { return members; }
    public int getMemberSize() { return members.size(); }
    public boolean hasMember(String userid) { return members.contains(userid); }
    public ArrayList<Post> getPosts() { return posts; }
    public void modifyIcon(Bitmap bitmap){
        this.icon = id + ".jpg";
        upload("icon", icon);
        FirebaseHelper.uploadIcon(bitmap, FirebaseHelper.GROUP_ICON, icon);
    }
    public StorageReference GetIcon(){
        return FirebaseHelper.getIcon(FirebaseHelper.GROUP_ICON, icon);
    }

    public void addMember(String userid){
        members.add(userid);
        upload("members", members);
        addMessage(new Message(User.findUser(userid).getName() + " joined this group." , Calendar.getInstance().getTimeInMillis()));
    }
    public void addPost(Post post){
        posts.add(0, post);
        upload("posts", posts);
    }
    public void removeMember(String userid){
        members.remove(userid);
        upload("members", members);
        if (members.size() == 0)
            FirebaseHelper.removeGroup(id);
    }
    public void removePost(Post post){
        posts.remove(post);
        upload("posts", posts);
    }
    public void addMessage(Message message){
        messages.add(0, message);
        upload("messages", messages);
    }
    public Message latestActivity(){
        if (messages.size() == 0){
            messages.add(new Message(User.findUser(managerid).getName() + " created this group.",
                    posts.size() > 0 ? posts.get(posts.size() - 1).getTime() - 1 : Calendar.getInstance().getTimeInMillis()));
            upload("messages", messages);
        }
        Message activity = messages.get(0);
        if (posts.size() > 0){
            Post post = posts.get(0);
            if (post.getTime() > activity.getTime())
                activity = new Message(User.findUser(post.getUserid()).getName() + ": " + post.getContent(), post.getTime());
        }
        return activity;
    }
    private void upload(String key, Object value){
        FirebaseHelper.updateGroup(id, key, value);
    }
    public boolean containsKeyword(String keyword) { return name.contains(keyword) || Integer.toString(group_number).contains(keyword); }
    public static Group findGroup(String id) { return FirebaseHelper.findGroup(id); }
    @Override
    public String toString(){
        return (new Gson()).toJson(this);
    }
    public static Group fromJson(String json){
        return (new Gson()).fromJson(json, Group.class);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Group)) return false;
        return id.equals(((Group) obj).id);
    }

    public static class GroupComparator implements Comparator<Group> {
        @Override
        public int compare(Group o1, Group o2) { return new Post.TimeComparator().compare(o1.posts.get(0).GetKey(), o2.posts.get(0).GetKey()); }
    }
}
