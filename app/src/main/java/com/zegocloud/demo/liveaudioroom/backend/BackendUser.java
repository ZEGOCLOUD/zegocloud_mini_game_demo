package com.zegocloud.demo.liveaudioroom.backend;


import com.google.gson.annotations.SerializedName;

public class BackendUser {

    @SerializedName("uid")
    private String uid;
    @SerializedName("nick_name")
    private String nickName;
    @SerializedName("gender")
    private String gender;
    @SerializedName("avatar")
    private String avatar;
    @SerializedName("is_login")
    private Boolean isLogin;
    @SerializedName("coins")
    private Integer coins;
    @SerializedName("is_ai")
    private Integer isAi;


    public String getUid() {
        return uid;
    }

    public String getNickName() {
        return nickName;
    }

    public String getGender() {
        return gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public Boolean getLogin() {
        return isLogin;
    }

    public Integer getCoins() {
        return coins;
    }

    public Integer getIsAi() {
        return isAi;
    }

    @Override
    public String toString() {
        return "BackendUser{" +
            "uid='" + uid + '\'' +
            ", nickName='" + nickName + '\'' +
            ", gender='" + gender + '\'' +
            ", avatar='" + avatar + '\'' +
            ", isLogin=" + isLogin +
            ", coins=" + coins +
            ", isAi=" + isAi +
            '}';
    }
}
