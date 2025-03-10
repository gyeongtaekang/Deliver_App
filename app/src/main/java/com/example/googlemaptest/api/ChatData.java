package com.example.googlemaptest.api;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatData implements Serializable {
    private String msg;
    private String nickname;
    private String timestamp; // 문자열 타입으로 변경
    private String profileImageUrl; // 프로필 이미지 URL 추가

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // 현재 시간을 문자열로 설정하는 메소드
    public void setTimestampToNow() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.timestamp = sdf.format(new Date());
    }


}
