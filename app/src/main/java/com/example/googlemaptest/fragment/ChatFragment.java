package com.example.googlemaptest.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaptest.MyApplication;
import com.example.googlemaptest.R;
import com.example.googlemaptest.api.ChatData;
import com.example.googlemaptest.api.Party;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ChatFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<ChatData> chatList;
    private String nick;
    private EditText editTextChat; // 메시지 입력창
    private ImageButton buttonSend; // 메시지 전송 버튼
    private DatabaseReference myRef;
    private TextView tvChatRoomName;
    private RecyclerView myRecyclerView;

    private TextView tvParticipantCount;
    private Button btnJoin;
    private int currentParticipants = 1; // 현재 참가 인원
    private int maxParticipants = 10; // 최대 참가 인원

    private int markerCreateCount; // 마커 생성 횟수
    private String userGrade; // 사용자 등급

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SharedPreferences에서 닉네임 불러오기
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ChatApp", Context.MODE_PRIVATE);
        nick = sharedPreferences.getString("nickname", "defaultNick");

        // getArguments()가 null이 아닌지 확인하고 partyId를 가져옵니다.
        Bundle args = getArguments();
        if (args != null && args.containsKey("partyId")) {
            int partyId = args.getInt("partyId", -1);
            if (partyId != -1) {
                loadChatRoom(partyId);
                myRef = FirebaseDatabase.getInstance().getReference("message_" + partyId);
            }
        }
        loadUserGrade();
        resetMarkerCreateCount(); // 로그인 시 마커 생성 횟수 초기화
    }

    private void loadUserGrade() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userGrade = sharedPreferences.getString("userGrade", "Bronze"); // 기본값은 Bronze
    }

    private void resetMarkerCreateCount() {
        markerCreateCount = 0;
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("markerCreateCount", markerCreateCount);
        editor.apply();
    }
    private void loadChatRoom(int partyId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference chatRoomRef = database.getReference("chatrooms").child(String.valueOf(partyId));

        chatRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                if (chatRoom != null) {
                    tvChatRoomName.setText(chatRoom.getName());
                    // 채팅 데이터 로드 및 설정
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // 뷰 바인딩
        editTextChat = view.findViewById(R.id.ET_chat);
        buttonSend = (ImageButton) view.findViewById(R.id.Button_Send);
        tvChatRoomName = view.findViewById(R.id.tvChatRoomName);
        myRecyclerView = view.findViewById(R.id.myRecyclerView);
        tvChatRoomName = view.findViewById(R.id.tvChatRoomName);


        updateParticipantCount();

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentParticipants < maxParticipants) {
                    currentParticipants++;
                    updateParticipantCount();
                    // Firebase 데이터베이스에 참가 인원 업데이트 로직 추가
                } else {
                    Toast.makeText(getActivity(), "최대 참가 인원에 도달했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        int partyId = getArguments() != null ? getArguments().getInt("partyId", -1) : -1;

        if (partyId != -1) {
            // 파티 정보를 로드하고 채팅방 이름 설정
            loadPartyInfo(partyId);
            // Firebase 데이터베이스 참조 설정
            myRef = FirebaseDatabase.getInstance().getReference("message_" + partyId);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            myRef = database.getReference("message_" + partyId);

            // Firebase 데이터베이스 이벤트 리스너 설정
            myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                    ChatData chat = snapshot.getValue(ChatData.class);
                    ((ChatAdapter) mAdapter).addChat(chat);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    // 데이터가 변경됐을 때 필요한 작업을 여기에 작성합니다.
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    // 데이터가 제거됐을 때 필요한 작업을 여기에 작성합니다.
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    // 데이터가 이동했을 때 필요한 작업을 여기에 작성합니다.
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // 데이터베이스 에러가 발생했을 때 필요한 작업을 여기에 작성합니다.
                    Log.w("ChatFragment", "loadChat:onCancelled", databaseError.toException());
                }
            });
        }
        // 닉네임 입력 대화 상자 표시
        showNicknameDialog();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editTextChat.getText().toString();
                if (!message.isEmpty() && myRef != null) {
                    ChatData chatData = new ChatData();
                    chatData.setNickname(nick);
                    chatData.setMsg(message);
                    chatData.setTimestamp(getCurrentTime());
                    myRef.push().setValue(chatData);
                    editTextChat.setText("");
                } else {
                    Toast.makeText(getActivity(), "메시지를 보낼 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // RecyclerView 설정
        mRecyclerView = view.findViewById(R.id.myRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(requireActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // 채팅 리스트와 어댑터 설정
        chatList = new ArrayList<>();
        mAdapter = new ChatAdapter(chatList, requireActivity(), nick);
        mRecyclerView.setAdapter(mAdapter);


        return view;
    }
    private void updateParticipantCount() {
        tvParticipantCount.setText(currentParticipants + "/" + maxParticipants);
    }

    private void loadPartyInfo(int partyId) {
        Party party = MyApplication.getPartyMap().get(partyId);
        if (party != null) {
            tvChatRoomName.setText(party.getName());
            // 채팅방 데이터 로드 및 설정 (예: Firebase Database 연결 등)
        }
    }
    private String getCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return simpleDateFormat.format(new Date());
    }
    // ChatData를 생성하는 메서드
    private ChatData createChatData(String nickname, String message) {
        ChatData chat = new ChatData();
        chat.setNickname(nickname);
        chat.setMsg(message);
        chat.setTimestampToNow(); // 현재 시간을 문자열로 설정
        return chat;
    }
    //안녕닉네임 입력 시작부분
    private void showNicknameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_nickname, null);
        builder.setView(dialogView);

        final EditText editTextDialogNickname = dialogView.findViewById(R.id.editTextDialogNickname);
        Button buttonDialogOK = dialogView.findViewById(R.id.buttonDialogOK);

        final AlertDialog dialog = builder.create();

        buttonDialogOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = editTextDialogNickname.getText().toString();
                if (!nickname.isEmpty()) {
                    confirmNicknameDialog(nickname);
                    dialog.dismiss();
                } else {
                    Toast.makeText(getActivity(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void confirmNicknameDialog(final String nickname) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("닉네임 확인");
        builder.setMessage("닉네임을 '" + nickname + "'(으)로 하시겠습니까?");

        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                saveNickname(nickname);
                nick = nickname;
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                showNicknameDialog(); // 사용자가 '아니오'를 선택하면 다시 닉네임 입력 대화 상자를 표시
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //여기까지 닉네임 입력
    private void saveNickname(String nickname) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ChatApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nickname", nickname);
        editor.apply();
    }

    public class ChatAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private ArrayList<ChatData> mDataset;
        private String myNickname;
        private Context context;

        public ChatAdapter(ArrayList<ChatData> myDataset, Context context, String myNickname) {
            mDataset = myDataset;
            this.context = context;
            this.myNickname = myNickname;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            ChatData chat = mDataset.get(position);

            holder.TV_nickname.setText(chat.getNickname());
            holder.TV_msg.setText(chat.getMsg());
            holder.TV_date.setText(getDisplayDate(chat.getTimestamp()));

            RelativeLayout.LayoutParams paramsNick = (RelativeLayout.LayoutParams) holder.TV_nickname.getLayoutParams();
            RelativeLayout.LayoutParams paramsMsg = (RelativeLayout.LayoutParams) holder.TV_msg.getLayoutParams();
            RelativeLayout.LayoutParams paramsDate = (RelativeLayout.LayoutParams) holder.TV_date.getLayoutParams();

            if (chat.getNickname().equals(this.myNickname)) {
                // 사용자 자신의 메시지인 경우
                paramsNick.addRule(RelativeLayout.ALIGN_PARENT_END);
                paramsMsg.addRule(RelativeLayout.ALIGN_PARENT_END);
                paramsDate.addRule(RelativeLayout.ALIGN_PARENT_END);

                holder.TV_msg.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_user_message));
                holder.TV_nickname.setVisibility(View.INVISIBLE);
            } else {
                // 다른 사람의 메시지인 경우
                paramsNick.addRule(RelativeLayout.ALIGN_PARENT_START);
                paramsMsg.addRule(RelativeLayout.ALIGN_PARENT_START);
                paramsDate.addRule(RelativeLayout.ALIGN_PARENT_START);

                holder.TV_msg.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_other_message));
                holder.TV_nickname.setVisibility(View.VISIBLE);
            }

            holder.TV_nickname.setLayoutParams(paramsNick);
            holder.TV_msg.setLayoutParams(paramsMsg);
            holder.TV_date.setLayoutParams(paramsDate);
        }

        private String getDisplayDate(String timestamp) {
            try {
                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = originalFormat.parse(timestamp);

                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                Calendar today = Calendar.getInstance();
                SimpleDateFormat dateFormat;

                if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    dateFormat = new SimpleDateFormat("HH:mm");
                    return "오늘 " + dateFormat.format(cal.getTime());
                } else {
                    dateFormat = new SimpleDateFormat("MM월 dd일");
                    return dateFormat.format(cal.getTime());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }


        @Override
        public int getItemCount() {
            return mDataset == null ? 0 : mDataset.size();
        }

        public void addChat(ChatData chat) {
            mDataset.add(chat);
            notifyItemInserted(mDataset.size() - 1);
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView TV_nickname;
        public TextView TV_msg;
        public TextView TV_date; // 날짜 표시 TextView

        public MyViewHolder(View v) {
            super(v);
            TV_nickname = v.findViewById(R.id.TV_nickname);
            TV_msg = v.findViewById(R.id.TV_msg);
            TV_date = v.findViewById(R.id.TV_date); // 날짜 표시 TextView 초기화
        }
    }
}
