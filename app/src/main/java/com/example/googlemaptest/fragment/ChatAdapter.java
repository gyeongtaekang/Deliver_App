package com.example.googlemaptest.fragment;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaptest.R;
import com.example.googlemaptest.api.ChatData;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private ArrayList<ChatData> chatList;
    private Context context;
    private String nick;

    public ChatAdapter(ArrayList<ChatData> chatList, Context context, String nick) {
        this.chatList = chatList;
        this.context = context;
        this.nick = nick;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatData chat = chatList.get(position);

        // nickname이 null인 경우를 체크합니다.
        if (chat == null || chat.getNickname() == null) {
            Log.e("ChatAdapter", "Nickname is null at position " + position);
            holder.tvNickname.setText("알 수 없음");
        } else {
            holder.tvNickname.setText(chat.getNickname().equals(nick) ? "나" : chat.getNickname());
        }

        holder.tvMessage.setText(chat.getMsg());
        holder.tvTimestamp.setText(chat.getTimestamp());

        customizeMessageView(holder, chat.getNickname() != null && chat.getNickname().equals(nick));
    }


    private void customizeMessageView(ChatViewHolder holder, boolean isOwnMessage) {
        if (isOwnMessage) {
            // 사용자 자신의 메시지
            holder.tvMessage.setBackgroundResource(R.drawable.bg_user_message);
            holder.tvNickname.setVisibility(View.GONE);
            holder.messageContainer.setGravity(Gravity.END); // 오른쪽 정렬
        } else {
            // 다른 사용자의 메시지
            holder.tvMessage.setBackgroundResource(R.drawable.bg_other_message);
            holder.tvNickname.setVisibility(View.VISIBLE);
            holder.messageContainer.setGravity(Gravity.START); // 왼쪽 정렬
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNickname, tvMessage, tvTimestamp;
        public RelativeLayout messageContainer;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNickname = itemView.findViewById(R.id.TV_nickname);
            tvMessage = itemView.findViewById(R.id.TV_msg);
            tvTimestamp = itemView.findViewById(R.id.TV_date);
            messageContainer = itemView.findViewById(R.id.message_container);
        }
    }
}
