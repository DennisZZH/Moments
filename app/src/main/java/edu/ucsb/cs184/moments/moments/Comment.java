package edu.ucsb.cs184.moments.moments;

import java.util.ArrayList;
import java.util.Date;

public class Comment {
    private int userid;
    private String content;
    private Date date;
    private Comment parent = null;

    private ArrayList<Comment> comments = new ArrayList<>();
    private ArrayList<Rating> ratings = new ArrayList<>();

    public Comment(int userid, String content, Date date){
        this.userid = userid;
        this.content = content;
        this.date = date;
    }
    public Comment(int userid, String content, Date date, Comment parent){
        this.userid = userid;
        this.content = content;
        this.date = date;
        this.parent = parent;
    }

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
    public int getUserid() { return userid; }
    public String getContent() { return content; }
    public Date getDate() { return date; }
    public Comment getParent() { return parent; }
    public Boolean hasParent() { return parent == null; }

    public static Comment TestComment(){ return new Comment(0, new Date().toString(), new Date());}
}