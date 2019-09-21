package com.princedev.eyesonapp.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.princedev.eyesonapp.Models.Message;
import com.princedev.eyesonapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Fizz on 12/08/2018.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder>{

    private List<Message> userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;
    private Message messages, messageTranslate;

    public MessagesAdapter(List<Message> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText , receiverMessageText, translateMessage;
        public RelativeLayout receiverLayout;
        public ImageButton translateButton;

        public MessagesViewHolder(final View itemView) {
            super(itemView);

            receiverLayout = (RelativeLayout) itemView.findViewById(R.id.layour_receiver);
            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);

            try {
                //Default variables for translation
                final String textToBeTranslated = messageTranslate.getMessage();
                final String languagePair = "en-id"; //("<source_language>-<target_language>")

                translateMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Process Translate
                        YandexTranslateAPI yandexTranslateAPI= new YandexTranslateAPI(itemView.getContext());
                        String translationResult = null;
                        try {
                            translationResult = String.valueOf(yandexTranslateAPI.execute(textToBeTranslated, languagePair).get());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        Log.d("clicked", "onClick: translate");
                        receiverMessageText.setText(translationResult);
                    }
                });
            }catch (NullPointerException e){

            }

        }
    }

//    private void Translate(String textToBeTranslated,String languagePair){
//        YandexTranslateAPI translatorBackgroundTask= new YandexTranslateAPI();
//        String translationResult = String.valueOf(translatorBackgroundTask.execute(textToBeTranslated,languagePair)); // Returns the translated text as a String
//        Log.d("Translation Result",translationResult); // Logs the result in Android Monitor
//    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_users, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessagesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesViewHolder holder, int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        try {
            messageTranslate = userMessagesList.get(position - 1);
        }catch (IndexOutOfBoundsException e){

        }
        messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String image = dataSnapshot.child("profileimage").getValue().toString();
//                    Picasso.with(holder.receiverProfileImage.getContext()).load(image)
//                            .placeholder(R.drawable.profile).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text")){
            holder.receiverLayout.setVisibility(View.INVISIBLE);
            holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.senderMessageText.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messageSenderID)){
                holder.senderMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.senderMessageText.setTextColor(Color.WHITE);
                holder.senderMessageText.setGravity(Gravity.LEFT);
                holder.senderMessageText.setText(messages.getMessage());
                holder.senderMessageText.setVisibility(View.VISIBLE);
            }else {
//                if (position == getItemCount() - 1){
//                    holder.translateMessage.setVisibility(View.VISIBLE);
//                }else {
//                    holder.translateMessage.setVisibility(View.INVISIBLE);
//                }
                holder.receiverLayout.setVisibility(View.VISIBLE);
                holder.senderMessageText.setVisibility(View.INVISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.receiverMessageText.setTextColor(Color.WHITE);
                holder.receiverMessageText.setGravity(Gravity.LEFT);
                holder.receiverMessageText.setText(messages.getMessage());

            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}
