package com.princedev.eyesonapp.Models;

/**
 * Created by Fizz on 23/08/2018.
 */

public class User {
    private String username;
    private String fullname;
    private String age;
    private String hobbies;
    private String aboutme;
    private String profileimage;
    private String status;
    private String nationality;
    private String native_speaker;
    private String learning;



    public User() {
    }

    public User(String username, String fullname, String age, String hobbies, String aboutme,
                String profileimage, String status, String nationality, String native_speaker,
                String learning) {
        this.username = username;
        this.fullname = fullname;
        this.age = age;
        this.hobbies = hobbies;
        this.aboutme = aboutme;
        this.profileimage = profileimage;
        this.status = status;
        this.nationality = nationality;
        this.native_speaker = native_speaker;
        this.learning = learning;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getHobbies() {
        return hobbies;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public String getAboutme() {
        return aboutme;
    }

    public void setAboutme(String aboutme) {
        this.aboutme = aboutme;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getNative_speaker() {
        return native_speaker;
    }

    public void setNative_speaker(String native_speaker) {
        this.native_speaker = native_speaker;
    }

    public String getLearning() {
        return learning;
    }

    public void setLearning(String learning) {
        this.learning = learning;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", fullname='" + fullname + '\'' +
                ", age='" + age + '\'' +
                ", hobbies='" + hobbies + '\'' +
                ", aboutme='" + aboutme + '\'' +
                ", profileimage='" + profileimage + '\'' +
                ", status='" + status + '\'' +
                ", nationality='" + nationality + '\'' +
                ", native_speaker='" + native_speaker + '\'' +
                ", learning='" + learning + '\'' +
                '}';
    }
}
