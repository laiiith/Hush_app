package com.example.hush.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class CommentModel {
    private String comment, commentID, postID, uid;
    @ServerTimestamp
    private Date timeStamp;

    public CommentModel() {
    }

    public CommentModel(String comment, String commentID, String postID, String uid, Date timeStamp) {
        this.comment = comment;
        this.commentID = commentID;
        this.postID = postID;
        this.uid = uid;
        this.timeStamp = timeStamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
