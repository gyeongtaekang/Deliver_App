package com.example.googlemaptest.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaptest.R;
import com.example.googlemaptest.fragment.ChatRoom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatRoomsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatRoomsAdapter adapter;
    private ArrayList<ChatRoom> chatRoomsList;

    public ChatRoomsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_rooms_fragment, container, false);

        recyclerView = view.findViewById(R.id.chat_rooms_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRoomsList = new ArrayList<>();
        adapter = new ChatRoomsAdapter(chatRoomsList, room -> {
            // 여기에 채팅방 입장 로직 구현
        });
        recyclerView.setAdapter(adapter);

        loadChatRooms();

        return view;
    }

    private void loadChatRooms() {
        DatabaseReference chatRoomsRef = FirebaseDatabase.getInstance().getReference("chatrooms");
        chatRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("ChatRoomsFragment", "Total chat rooms: " + dataSnapshot.getChildrenCount());
                chatRoomsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom room = snapshot.getValue(ChatRoom.class);
                    if (room != null) {
                        Log.d("ChatRoomsFragment", "Chat room found: " + room.getName());
                        DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                                .getReference("message_" + snapshot.getKey());
                        messagesRef.limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Log.d("ChatRoomsFragment", "Messages exist in: " + room.getName());
                                    chatRoomsList.add(room);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Log.d("ChatRoomsFragment", "No messages in: " + room.getName());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("ChatRoomsFragment", "Error loading messages: " + databaseError.getMessage());
                            }
                        });
                    } else {
                        Log.d("ChatRoomsFragment", "Chat room data is null");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatRoomsFragment", "Error loading chat rooms: " + databaseError.getMessage());
            }
        });
    }




    // 새로운 hasMessages 메소드 정의
    private void hasMessages(String chatRoomId, OnHasMessagesListener listener) {
        DatabaseReference chatMessagesRef = FirebaseDatabase.getInstance().getReference("message_" + chatRoomId);
        chatMessagesRef.limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onResult(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onResult(false);
            }
        });
    }

    // hasMessages 메소드의 리스너 인터페이스
    public interface OnHasMessagesListener {
        void onResult(boolean hasMessages);
    }


    // 채팅방 클릭 리스너 인터페이스
    public interface OnChatRoomClickListener {
        void onChatRoomClicked(ChatRoom chatRoom);
    }
}
