package com.example.googlemaptest.fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatRoom {
    private String name;
    private int participantCount; // 참가 인원 수를 위한 필드 추가

    // 기본 생성자 추가
    public ChatRoom() {
        // Firebase를 위한 기본 생성자
    }
    // 생성자
    public ChatRoom(String name) {
        this.name = name;
    }

    // 필요한 getter와 setter 메서드
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // participantCount 필드의 getter와 setter
    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

}
