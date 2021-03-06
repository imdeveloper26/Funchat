package com.example.amangoyal.funchat.Fragments;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.amangoyal.funchat.ChatActivity;
import com.example.amangoyal.funchat.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private List<ChatModelClass> chatList;
    private Context context;

    public ChatListAdapter(Context context,List<ChatModelClass> chatList){
        this.chatList = chatList;
        this.context = context;

    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_chat_list_layout,viewGroup,false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder chatViewHolder, int i) {
        final ChatModelClass chatModelClass = chatList.get(i);
        chatViewHolder.setName(chatModelClass.getName());
        chatViewHolder.setProfile(chatModelClass.getThumb_image());
        chatViewHolder.setLastMessage(chatModelClass.getLast_message());
        chatViewHolder.setTime(String.valueOf(chatModelClass.getTimestamp()));

        chatViewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userName",chatModelClass.getName());
                intent.putExtra("user_id",chatModelClass.getUid());
               context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView tname,tLastMessage,tTime;
        CircleImageView tprofileImage;
        ConstraintLayout root;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            tprofileImage = itemView.findViewById(R.id.single_user_image);
            tname =itemView.findViewById(R.id.list_title);
            root = itemView.findViewById(R.id.list_root);
            tLastMessage = itemView.findViewById(R.id.list_desc);
            tTime = itemView.findViewById(R.id.single_chat_list_time_layout);



        }

        public void setName(String name){
            tname.setText(name);
        }

        public void setProfile(String profile){

            Picasso.get().load(profile).placeholder(R.drawable.default_avatar).into(tprofileImage);
        }

        public void setLastMessage(String lastMessage){
            tLastMessage.setText(lastMessage);
        }

        public  void setTime(String timestamp){
            long timeStamp = Long.parseLong(timestamp);
            String time = DateUtils.formatDateTime(context, timeStamp, DateUtils.FORMAT_SHOW_TIME);
            tTime.setText(time);


        }

    }

}


