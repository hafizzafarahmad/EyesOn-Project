package com.princedev.eyesonapp.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.princedev.eyesonapp.FriendsActivity;
import com.princedev.eyesonapp.Models.Request;
import com.princedev.eyesonapp.PersonProfileActivity;
import com.princedev.eyesonapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import libs.mjn.prettydialog.PrettyDialog;

/**
 * Created by Fizz on 20/08/2018.
 */

public class RequestFragment extends Fragment {

    private static final String TAG = "RequestFragment";

    private RecyclerView requsetList;
    private ImageView image;
    private TextView text;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, friendsRef, friendRequestRef, requestRef;

    private String saveCurrentDate, senderUserId, receiverUserId, CURRENT_STATE;

    String currentUserId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);

        try {
            mAuth = FirebaseAuth.getInstance();
            currentUserId = mAuth.getCurrentUser().getUid();
            senderUserId = mAuth.getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("Users");
            friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
            requestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(currentUserId);
            friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

            text = (TextView) view.findViewById(R.id.text);
            image = (ImageView) view.findViewById(R.id.image);

            requsetList = (RecyclerView) view.findViewById(R.id.all_request_friend_list);
            requsetList.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            requsetList.setLayoutManager(linearLayoutManager);

            isOnline();
            DisplayAllRequestList();

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

    private void DisplayAllRequestList() {
        FirebaseRecyclerAdapter<Request, RequestViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Request, RequestViewHolder>(
                        Request.class,
                        R.layout.all_request_display_layout,
                        RequestFragment.RequestViewHolder.class,
                        requestRef
                ) {
                    @Override
                    protected void populateViewHolder(final RequestViewHolder viewHolder, Request model, int position) {
                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference get_type_ref = getRef(position).child("request_type").getRef();

                        get_type_ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String request_type = dataSnapshot.getValue().toString();

                                    image.setVisibility(View.GONE);
                                    text.setVisibility(View.GONE);

                                    if (request_type.equals("received")) {
                                        userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()){
                                                    final String fullName = dataSnapshot.child("fullname").getValue().toString();
                                                    final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                                                    final String age = dataSnapshot.child("age").getValue().toString();

                                                    viewHolder.setFullname(fullName);
                                                    viewHolder.setProfileimage(getContext(), profileImage);
                                                    viewHolder.setAge(age);

                                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            Intent profileIntent = new Intent(getContext(), PersonProfileActivity.class);
                                                            profileIntent.putExtra("visit_user_id", list_user_id);
                                                            startActivity(profileIntent);
                                                        }
                                                    });

                                                    ImageButton acceptFriend = (ImageButton) viewHolder.mView.findViewById(R.id.accept_friend);
                                                    acceptFriend.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            Calendar calForDate = Calendar.getInstance();
                                                            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                                            saveCurrentDate = currentDate.format(calForDate.getTime());

                                                            friendsRef.child(senderUserId)
                                                                    .child(list_user_id)
                                                                    .child("date")
                                                                    .setValue(saveCurrentDate);

                                                            friendsRef.child(senderUserId)
                                                                    .child(list_user_id)
                                                                    .child("uid")
                                                                    .setValue(list_user_id);

                                                            friendsRef.child(list_user_id)
                                                                    .child(senderUserId)
                                                                    .child("date")
                                                                    .setValue(saveCurrentDate);

                                                            friendsRef.child(list_user_id)
                                                                    .child(senderUserId)
                                                                    .child("uid")
                                                                    .setValue(senderUserId);

                                                            friendRequestRef.child(senderUserId)
                                                                    .child(list_user_id)
                                                                    .removeValue();

                                                            friendRequestRef.child(list_user_id)
                                                                    .child(senderUserId)
                                                                    .removeValue();
                                                            CURRENT_STATE = "friends";

                                                        }
                                                    });

                                                    ImageButton declineFriend = (ImageButton) viewHolder.mView.findViewById(R.id.decline_friend);
                                                    declineFriend.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            friendRequestRef.child(currentUserId).child(list_user_id)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        friendRequestRef.child(list_user_id).child(currentUserId)
                                                                                .removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            CURRENT_STATE = "not_friends";
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }if (request_type.equals("sent")){
                                        userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()){
                                                    final String fullName = dataSnapshot.child("fullname").getValue().toString();
                                                    final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                                                    final String age = dataSnapshot.child("age").getValue().toString();

                                                    viewHolder.setFullname(fullName);
                                                    viewHolder.setProfileimage(getContext(), profileImage);
                                                    viewHolder.setAge(age);

                                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            Intent profileIntent = new Intent(getContext(), PersonProfileActivity.class);
                                                            profileIntent.putExtra("visit_user_id", list_user_id);
                                                            startActivity(profileIntent);
                                                        }
                                                    });

                                                    TextView textWaiting = (TextView) viewHolder.mView.findViewById(R.id.text_not_accepted);
                                                    textWaiting.setVisibility(View.VISIBLE);

                                                    ImageButton acceptFriend = (ImageButton) viewHolder.mView.findViewById(R.id.accept_friend);
                                                    acceptFriend.setVisibility(View.GONE);

                                                    ImageButton declineFriend = (ImageButton) viewHolder.mView.findViewById(R.id.decline_friend);
                                                    declineFriend.setVisibility(View.GONE);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                };
        requsetList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setFullname(String fullname) {
            TextView fullNameDisplay = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            fullNameDisplay.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage) {
            CircleImageView requestImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(requestImage);
        }

        public void setAge(String age) {
            TextView requestAge = (TextView) mView.findViewById(R.id.all_users_age);
            requestAge.setText(age + " Years Old");
        }
    }
}
