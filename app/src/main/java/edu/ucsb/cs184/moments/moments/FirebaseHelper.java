package edu.ucsb.cs184.moments.moments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class FirebaseHelper {
    public static final String GROUP_ICON = "GroupIcon", USER_ICON = "UserIcon";

    private static FirebaseDatabase firebase;
    private static FirebaseStorage storage;
    private static StorageReference storageRef;
    private static StorageReference usr, gsr;
    private static DatabaseReference db;
    private static DatabaseReference udb, gdb, uc, gc, uudb;
    private static DataSnapshot uds, gds, gcds;
    private static OnUDBReceivedListener uReceivedListener;
    private static OnGDBReceivedListener gReceivedListener;
    private static ArrayList<OnDataUpdatesListener> updateListeners = new ArrayList<>();
    private static AfterUserInsertedListener uInsertionListener;
    private static AfterGroupInsertedListener gInsertionListener;
    public static void init(){
        firebase = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        usr = storageRef.child("user_icon");
        gsr = storageRef.child("group_icon");
        db = firebase.getReference();
        db.keepSynced(true);
        udb = db.child("users");
        udb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uds = dataSnapshot;
                if (User.firebaseUser != null){
                    uudb = udb.child(User.firebaseUser.getUid());
                    User.user = uds.child(User.firebaseUser.getUid()).getValue(User.class);
                    uudb.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User.user = dataSnapshot.getValue(User.class);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                if (uReceivedListener != null) uReceivedListener.onUDBReceived();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        udb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uds = dataSnapshot;
                for (OnDataUpdatesListener listener: updateListeners)
                    listener.onUDBUpdates();
                // This updates user
                // So there is a TODO: gives a red dot on the bottom navigation when there is a new post
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        gdb = db.child("groups");
        gdb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gds = dataSnapshot;
                if (gReceivedListener != null) gReceivedListener.onGDBReceived();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        gdb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // TODO: Since we update user, updating group is also necessary but has lower priority.
                for (OnDataUpdatesListener listener: updateListeners)
                    listener.onGDBUpdates();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        gc = db.child("group_count");
        gc.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gcds = dataSnapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // I don't think these three should be used
    public static FirebaseDatabase getFirebase(){ return firebase; }
    public static DatabaseReference getUdb() { return udb; }
    public static DatabaseReference getGdb() { return gdb; }

    public static void insertUser(final User user){
        new Thread(new Runnable() {
            @Override
            public void run() {
                udb.child(user.getId()).setValue(user);
                if (uInsertionListener != null) uInsertionListener.afterUserInserted(user);
            }
        }).start();
    }

    public static void insertGroup(final Group group){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String id = gdb.push().getKey();
                group.setId(id);
                int number = gcds.getValue(Integer.class);
                group.setNumber(number);
                gc.setValue(++number);
                gdb.child(id).setValue(group);
                if (gInsertionListener != null) gInsertionListener.afterGroupInserted(group);
            }
        }).start();
    }

    public static void removeGroup(final String id){
        gdb.child(id).removeValue();
    }

    public static User findUser(String id){
        return uds.child(id).getValue(User.class);
    }
    public static Group findGroup(String id){
        return gds.child(id).getValue(Group.class);
    }

    public static ArrayList<Post> searchPosts(String keyword){
        ArrayList<Post> data = new ArrayList<>();
        for (DataSnapshot ds: uds.getChildren()){
            for (DataSnapshot pds: ds.child("posts").getChildren()){
                Post value = pds.getValue(Post.class);
                if (value.containsKeyword(keyword))
                    data.add(value);
            }
        }
        return data;
    }
    public static ArrayList<User> searchUsers(String keyword){
        ArrayList<User> data = new ArrayList<>();
        for (DataSnapshot ds: uds.getChildren()){
            User user = ds.getValue(User.class);
            if (user.containsKeyword(keyword))
                data.add(user);
        }
        return data;
    }
    public static ArrayList<Group> searchGroups(String keyword){
        ArrayList<Group> data = new ArrayList<>();
        for (DataSnapshot ds: gds.getChildren()){
            Group group = ds.getValue(Group.class);
            if (group.containsKeyword(keyword))
                data.add(group);
        }
        return data;
    }

    public static Post findPost(Post.Key key){
        for (DataSnapshot ds: uds.child(key.userid).child("posts").getChildren()){
            Post data = ds.getValue(Post.class);
            if (data.GetKey().equals(key))
                return data;
        }
        return null;
    }
    public static Post findPostInGroup(Post.Key key, String id){
        for (DataSnapshot ds: gds.child(id).child("posts").getChildren()){
            Post data = ds.getValue(Post.class);
            if (data.GetKey().equals(key))
                return data;
        }
        return null;
    }
    public static Comment findComment(Comment.Key key){
        Post post = Post.powerfulFindPost(key.postKey);
        if (post != null)
            for (Comment data: post.getComments())
                if (data.GetKey().equals(key))
                    return data;
        return null;
    }
    public static Rating findRating(Rating.Key key){
        Post post = Post.powerfulFindPost(key.postKey);
        if (post != null)
            for (Rating data: post.getRatings())
                if (data.GetKey().equals(key))
                    return data;
        return null;
    }

    public static void updateUser(final String id, final String key, final Object data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(udb != null)
                    udb.child(id).child(key).setValue(data);
            }
        }).start();
    }

    public static void updateGroup(final String id, final String key, final Object data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(gdb != null)
                    gdb.child(id).child(key).setValue(data);
            }
        }).start();
    }

    public static void updatePost(final Post post, final String key, final Object value){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                if (post.postedInGroup()){
                    if (gds == null || gdb == null) return;
                    for (DataSnapshot ds: gds.child(post.getGroupid() + "/posts/").getChildren()){
                        if (ds.getValue(Post.class).equals(post)){
                            gdb.child(post.getGroupid() + "/posts/" + index + "/" + key).setValue(value);
                            return;
                        }
                        index++;
                    }
                }else{
                    if (uds == null || udb == null) return;
                    for (DataSnapshot ds: uds.child(post.getUserid() + "/posts/").getChildren()){
                        if (ds.getValue(Post.class).equals(post)){
                            udb.child(post.getUserid() + "/posts/" + index + "/" + key).setValue(value);
                            return;
                        }
                        index++;
                    }
                }
            }
        }).start();
    }

    public static User findUserWithName(String name){
        for (DataSnapshot ds: uds.getChildren()){
            User user = ds.getValue(User.class);
            if (user.getName().equals(name))
                return user;
        }
        return null;
    }

    public static void setOnUDBReceivedListener(OnUDBReceivedListener listener){
        uReceivedListener = listener;
    }
    public static void setOnGDBReceivedListener(OnGDBReceivedListener listener){
        gReceivedListener = listener;
    }
    public static void addOnDateUpdatesListener(OnDataUpdatesListener listener){
        updateListeners.add(listener);
    }
    public static void setAfterUserInsertionListener(AfterUserInsertedListener listener){
        uInsertionListener = listener;
    }
    public static void setAfterGroupInsertionListener(AfterGroupInsertedListener listener){
        gInsertionListener = listener;
    }

    public static boolean initFinished() { return uds != null && gds != null && gcds != null; }

    public static void uploadIcon(final Bitmap bitmap, final String type, final String id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                UploadIcon(bitmap, type, id);
            }
        }).start();
    }

    private static void UploadIcon(Bitmap bitmap, String type, String url) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference imagesRef;
        switch (type){
            case USER_ICON:
                imagesRef = usr.child(url);
                break;
            case GROUP_ICON:
                 imagesRef = gsr.child(url);
                 break;
            default:
                return;
        }

        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
    }

    public static StorageReference getIcon(final String type, final String url){
        switch (type){
            case USER_ICON:
                return usr.child(url);
            case GROUP_ICON:
                return gsr.child(url);
            default:
                return null;
        }
    }

    public static void setIcon(StorageReference reference, final Activity activity, final ImageView imageView)  {
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(activity).load(uri).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("setImageWithGlide", e.toString());
            }
        });
    }

    interface OnUDBReceivedListener{
        void onUDBReceived();
    }
    interface OnGDBReceivedListener{
        void onGDBReceived();
    }
    interface AfterUserInsertedListener {
        void afterUserInserted(User user);
    }
    interface AfterGroupInsertedListener {
        void afterGroupInserted(Group group);
    }

    interface OnDataUpdatesListener {
        void onUDBUpdates();
        void onGDBUpdates();
    }
}
