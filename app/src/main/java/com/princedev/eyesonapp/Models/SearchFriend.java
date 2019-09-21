package com.princedev.eyesonapp.Models;

/**
 * Created by Fizz on 27/08/2018.
 */

public class SearchFriend {

    public String profileimage, fullname, age;

    public SearchFriend() {
    }

    public SearchFriend(String profileimage, String fullname, String age) {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.age = age;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
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
}
