package com.example.googlemaptest.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaptest.R;
import com.example.googlemaptest.fragment.ChatRoom;

import java.util.List;

public class ChatRoomsAdapter extends RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder> {

    private List<ChatRoom> chatRooms;
    private ChatRoomsFragment.OnChatRoomClickListener listener;

    public ChatRoomsAdapter(List<ChatRoom> chatRooms, ChatRoomsFragment.OnChatRoomClickListener listener) {
        this.chatRooms = chatRooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatRoomsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomsAdapter.ViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);
        holder.bind(chatRoom, listener);
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewName;

        ViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textview_chatroom_name);
        }

        void bind(final ChatRoom chatRoom, final ChatRoomsFragment.OnChatRoomClickListener listener) {
            textViewName.setText(chatRoom.getName());
            itemView.setOnClickListener(v -> listener.onChatRoomClicked(chatRoom));
        }
    }
}
