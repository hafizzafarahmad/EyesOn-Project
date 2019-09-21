package com.princedev.eyesonapp.Models;

/**
 * Created by Fizz on 30/08/2018.
 */

public class Request {

    private String fullname, age, profileimage, request_type;

    public Request() {
    }

    public Request(String fullname, String age, String profileimage, String request_type) {
        this.fullname = fullname;
        this.age = age;
        this.profileimage = profileimage;
        this.request_type = request_type;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
