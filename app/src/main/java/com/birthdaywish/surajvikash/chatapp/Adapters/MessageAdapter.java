package com.birthdaywish.surajvikash.chatapp.Adapters;

import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.birthdaywish.surajvikash.chatapp.DataModels.Messages;
import com.birthdaywish.surajvikash.chatapp.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by surajvikash on 31/05/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> messagesList;
    private FirebaseAuth mAuth;
    private String thumbImage;

    public MessageAdapter(List<Messages> list, String uri){
        messagesList = list;
        thumbImage = uri;
        //Log.e("uri", "e"+thumbImage);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Fresco.initialize(parent.getContext());
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_msg_element_view, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        Messages messages = messagesList.get(position);
        String curr_id = mAuth.getCurrentUser().getUid();
        String from_id = messages.getFrom();
        //Uri thumbUri = Uri.parse(thumbImage);

        String t;
        SimpleDateFormat normal = new SimpleDateFormat("hh:mm a dd/MM", Locale.ENGLISH);
        SimpleDateFormat today_format = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        SimpleDateFormat today = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

        Date today1 = new Date(System.currentTimeMillis());
        Date yesterday = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24));
        Date today2 = new Date(messages.getTime());
        String t1 = today.format(today1);
        String t2 = today.format(today2);
        String t3 = today.format(yesterday);
        if (t1.equals(t2))
            t = today_format.format(messages.getTime());
        else if (t2.equals(t3))
            t = "Yesterday " + today_format.format(messages.getTime());
        else
            t = normal.format(messages.getTime());

        if(curr_id.equals(from_id)){
            holder.receiveLayout.setVisibility(View.INVISIBLE);
            holder.sendLayout.setVisibility(View.VISIBLE);
            holder.mSendMessageText.setBackgroundResource(R.drawable.message_text_background);
            holder.mSendMessageText.setTextColor(Color.WHITE);
            holder.mSendMessageText.setText(messages.getMessage());
            holder.mSendTimeText.setText(t);
        }
        else {
            holder.sendLayout.setVisibility(View.INVISIBLE);
            holder.receiveLayout.setVisibility(View.VISIBLE);
            holder.mReceiveMessageText.setBackgroundResource(R.drawable.chat_background);
            holder.mReceiveMessageText.setTextColor(Color.BLACK);
            holder.mReceiveMessageText.setText(messages.getMessage());
            holder.profileImage.setImageURI(thumbImage);
            holder.mReceiveTimeText.setText(t);
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{

        LinearLayout sendLayout, receiveLayout;
        SimpleDraweeView profileImage;
        TextView mReceiveMessageText, mReceiveTimeText, mSendMessageText, mSendTimeText;

        public MessageViewHolder(View itemView) {
            super(itemView);
            sendLayout = (LinearLayout)itemView.findViewById(R.id.send_layout);
            receiveLayout = (LinearLayout)itemView.findViewById(R.id.receive_layout);
            profileImage = (SimpleDraweeView) itemView.findViewById(R.id.single_msg_element_profile_image);
            mReceiveMessageText = (TextView)itemView.findViewById(R.id.single_msg_element_receive_message);
            mReceiveTimeText = (TextView)itemView.findViewById(R.id.receive_msg_time_textview);
            mSendMessageText = (TextView)itemView.findViewById(R.id.single_msg_element_sent_msg);
            mSendTimeText = (TextView)itemView.findViewById(R.id.send_msg_time_textview);
        }
    }
}
