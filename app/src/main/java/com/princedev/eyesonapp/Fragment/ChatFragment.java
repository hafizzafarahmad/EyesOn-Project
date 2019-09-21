package com.princedev.eyesonapp.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.princedev.eyesonapp.ChatActivity;
import com.princedev.eyesonapp.FriendsActivity;
import com.princedev.eyesonapp.Models.Message;
import com.princedev.eyesonapp.Models.Request;
import com.princedev.eyesonapp.PersonProfileActivity;
import com.princedev.eyesonapp.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import libs.mjn.prettydialog.PrettyDialog;

/**
 * Created by Fizz on 20/08/2018.
 */

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    private RecyclerView chatList;
    private ImageView image;
    private TextView text;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, friendsRef, friendRequestRef, chatRef, chatlistRef;

    private String saveCurrentDate, senderUserId, receiverUserId, CURRENT_STATE;

    String currentUserId;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        try {
            mAuth = FirebaseAuth.getInstance();
            currentUserId = mAuth.getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("Users");
            chatRef = FirebaseDatabase.getInstance().getReference().child("Messages");
            chatlistRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserId);

            text = (TextView) view.findViewById(R.id.text);
            image = (ImageView) view.findViewById(R.id.image);

            chatList = (RecyclerView) view.findViewById(R.id.all_chat_friend_list);
            chatList.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            chatList.setLayoutManager(linearLayoutManager);

            isOnline();
            DisplayAllChatList();

        }catch (NullPointerException e){

        }
        return view;
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        final PrettyDialog logoutDialog = new PrettyDialog(getContext());

        if(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()){
            logoutDialog.setTitle("No Internet Connection")
                    .setMessage("Turn On Your Data")
                    .setIcon(R.drawable.icon_alert)
                    .setCanceledOnTouchOutside(false);
            logoutDialog.show();
            return false;
        }
        return true;
    }

    private void DisplayAllChatList() {
        FirebaseRecyclerAdapter<Message, ChatFragment.ChatViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Message, ChatFragment.ChatViewHolder>(
                        Message.class,
                        R.layout.all_users_display_layout,
                        ChatFragment.ChatViewHolder.class,
                        chatlistRef
                ) {
                    @Override
                    protected void populateViewHolder(final ChatFragment.ChatViewHolder viewHolder, Message model, int position) {
                        final String list_user_id = getRef(position).getKey();

                        userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    image.setVisibility(View.GONE);
                                    text.setVisibility(View.GONE);

                                    final String userName = dataSnapshot.child("fullname").getValue().toString();
                                    final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                                    final String status = dataSnapshot.child("status").getValue().toString();

                                    viewHolder.setFullname(userName);
                                    viewHolder.setProfileimage(getContext(), profileImage);

                                    TextView age = (TextView) viewHolder.mView.findViewById(R.id.all_users_age);
                                    age.setVisibility(View.GONE);

//                                    if (status.equals("online")){
//                                        viewHolder.imgOn.setVisibility(View.VISIBLE);
//                                        viewHolder.imgOff.setVisibility(View.GONE);
//                                    }else {
//                                        viewHolder.imgOn.setVisibility(View.GONE);
//                                        viewHolder.imgOff.setVisibility(View.VISIBLE);
//                                    }

                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", list_user_id);
                                            chatIntent.putExtra("userName", userName);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                };
        chatList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        View mView;
//        CircleImageView imgOn, imgOff;

        public ChatViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
//            imgOn = (CircleImageView) mView.findViewById(R.id.imgOn);
//            imgOff = (CircleImageView) mView.findViewById(R.id.imgOff);
        }

        public void setFullname(String fullname) {
            TextView fullNameDisplay = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            fullNameDisplay.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage) {
            CircleImageView requestImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(requestImage);
        }

    }
}
