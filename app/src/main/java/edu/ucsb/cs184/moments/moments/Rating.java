package edu.ucsb.cs184.moments.moments;

import java.util.Date;

public class Rating {
    private int userid;
    private int rating;
    private Date date;
    public Rating(int userid, int rating, Date date){
        this.userid = userid;
        this.rating = rating;
        this.date = date;
    }
    public Boolean isAnonymous() { return userid == User.anonymous; }
    public int getUserid() { return userid; }
    public int getRating() { return rating; }
    public Date getDate() { return date; }
}