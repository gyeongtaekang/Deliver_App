package com.example.googlemaptest.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaptest.MyApplication;
import com.example.googlemaptest.R;
import com.example.googlemaptest.api.ChatData;
import com.example.googlemaptest.api.Party;
import com.example.googlemaptest.fragment.ChatAdapter;
import com.example.googlemaptest.fragment.ChatRoom;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.content.SharedPreferences;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<ChatData> chatList;
    private String nick;
    private EditText editTextChat;
    private ImageButton buttonSend;
    private DatabaseReference myRef;
    private TextView tvChatRoomName;
    private TextView tvParticipantCount;
    private Button btnJoin;
    private int currentParticipants = 1; // 현재 참가 인원
    private int maxParticipants = 10; // 최대 참가 인원
    private Party party;

    @Override
    public void onBackPressed() {
        super.onBackPressed();  // 기본 뒤로 가기 동작 수행
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_chat);

        // 뷰 초기화
        editTextChat = findViewById(R.id.ET_chat);
        buttonSend = findViewById(R.id.Button_Send);
        tvChatRoomName = findViewById(R.id.tvChatRoomName); // 이 부분을 setContentView 이후에 위치하도록 변경
        tvParticipantCount = findViewById(R.id.tvParticipantCount);

        if (tvParticipantCount == null) {
            Log.e("ChatActivity", "tvParticipantCount is null!");
            finish();
            return;
        }

        if (getIntent() != null && getIntent().hasExtra("partyId")) {
            nick = getIntent().getStringExtra("nickname"); // 닉네임 받아오기
            int partyId = getIntent().getIntExtra("partyId", -1);
            // 채팅방 이름 설정
            Party party = MyApplication.getPartyMap().get(partyId);
            if (party != null) {
                tvChatRoomName.setText(party.getName());
                updateParticipantCount();
            } else {
                Log.e("ChatActivity", "Party object is null.");
            }
        } else {
            Log.e("ChatActivity", "Intent data is null or missing");
            // Handle the error, perhaps close the activity or show an error message
        }


        // 뷰 초기화
        editTextChat = findViewById(R.id.ET_chat);
        buttonSend = findViewById(R.id.Button_Send);
        tvChatRoomName = findViewById(R.id.tvChatRoomName);
        mRecyclerView = findViewById(R.id.myRecyclerView);

        tvParticipantCount = findViewById(R.id.tvParticipantCount);
        btnJoin = findViewById(R.id.btnJoin);

        if (tvParticipantCount == null) {
            // tvParticipantCount가 null이면 오류 메시지를 로그에 출력하고 액티비티를 종료합니다.
            Log.e("ChatActivity", "tvParticipantCount is null!");
            finish();
            return;
        }
        updateParticipantCount();

        // RecyclerView 설정
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // 채팅 리스트와 어댑터 설정
        chatList = new ArrayList<>();
        mAdapter = new ChatAdapter(chatList, this, nick); // ChatAdapter는 채팅 데이터를 표시하는 어댑터입니다.
        mRecyclerView.setAdapter(mAdapter);

        // SharedPreferences에서 닉네임 불러오기
        SharedPreferences sharedPreferences = getSharedPreferences("ChatApp", MODE_PRIVATE);
        nick = sharedPreferences.getString("nickname", "defaultNick");

        int partyId = getIntent().getIntExtra("partyId", -1);
        DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("chatrooms").child(String.valueOf(partyId));

        // 엑스 버튼 클릭 이벤트 처리
        ImageButton btnCloseChat = findViewById(R.id.btnCloseChat);
        btnCloseChat.setOnClickListener(view -> {
            // 채팅 액티비티 종료
            finish();
        });

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                        if (chatRoom != null) {
                            int partyId = getIntent().getIntExtra("partyId", -1);
                            Party updatedParty = MyApplication.getPartyMap().get(partyId);
                            if (updatedParty != null && updatedParty.getCurrentRecruitNumber() < updatedParty.getRecruitNumber()) {
                                int newParticipantCount = updatedParty.getCurrentRecruitNumber() + 1;
                                updatedParty.setCurrentRecruitNumber(newParticipantCount);
                                MyApplication.getPartyMap().put(partyId, updatedParty);
                                chatRoomRef.setValue(updatedParty);
                                updateParticipantCount();
                            } else {
                                Toast.makeText(ChatActivity.this, "최대 참가 인원에 도달했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // 에러 처리...
                    }
                });
            }
        });


        // Firebase 데이터베이스 참조 설정
        if (partyId != -1) {
            myRef = FirebaseDatabase.getInstance().getReference("message_" + partyId);

            // Firebase 데이터베이스 이벤트 리스너 설정
            myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    // 데이터가 추가될 때 호출되는 메서드
                    ChatData chatData = dataSnapshot.getValue(ChatData.class);
                    chatList.add(chatData);
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.scrollToPosition(chatList.size() - 1); // 마지막으로 스크롤
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    // 데이터가 변경될 때 호출되는 메서드
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    // 데이터가 삭제될 때 호출되는 메서드
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    // 데이터가 이동될 때 호출되는 메서드
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // 데이터베이스 에러가 발생할 때 호출되는 메서드
                }
            });
        }

        // 메시지 전송 버튼 리스너
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editTextChat.getText().toString();
                if (!message.isEmpty()) {
                    ChatData chatData = new ChatData();
                    chatData.setNickname(nick); // SharedPreferences에서 가져온 닉네임
                    chatData.setMsg(message);
                    chatData.setTimestamp(getCurrentTime());
                    myRef.push().setValue(chatData);
                    editTextChat.setText("");
                }
            }
        });
    }
    private void updateParticipantCount() {
        int partyId = getIntent().getIntExtra("partyId", -1);
        if (partyId != -1) {
            Party updatedParty = MyApplication.getPartyMap().get(partyId);
            if (updatedParty != null) {
                party = updatedParty; // party 객체 갱신
                int recruitNumber = party.getRecruitNumber(); // 모집 인원
                int currentRecruitNumber = party.getCurrentRecruitNumber(); // 현재 참가 인원
                if (tvParticipantCount != null) {
                    tvParticipantCount.setText(currentRecruitNumber + "/" + recruitNumber);
                }
            }
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return simpleDateFormat.format(new Date());
    }
}
