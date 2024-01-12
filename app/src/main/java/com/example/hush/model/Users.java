package com.example.hush.model;

public class Users {
    private String email , uniID , profileImage ,faculty,uid;

    public Users() {
    }

    public Users(String email, String uniID, String profileImage, String faculty, String uid) {
        this.email = email;
        this.uniID = uniID;
        this.profileImage = profileImage;
        this.faculty = faculty;
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getUniID() {
        return uniID;
    }

    public void setUniID(String uniID) {
        this.uniID = uniID;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
