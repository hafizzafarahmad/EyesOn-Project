package com.princedev.eyesonapp.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.princedev.eyesonapp.ClickPostActivity;
import com.princedev.eyesonapp.CommentActivity;
import com.princedev.eyesonapp.HomeActivity;
import com.princedev.eyesonapp.LoginActivity;
import com.princedev.eyesonapp.Models.Posts;
import com.princedev.eyesonapp.PersonProfileActivity;
import com.princedev.eyesonapp.R;
import com.princedev.eyesonapp.Utils.allFont;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import libs.mjn.prettydialog.PrettyDialog;

/**
 * Created by Fizz on 20/08/2018.
 */

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView postList;
    private ImageView image;
    private TextView text;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, postsRef, likesRef, friendRef;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public ArrayList<String> mFollowing;
    private int mResults;

    private String currentUserId, senderUserId, userId, id;
    Boolean likeChecker = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        //senderUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        text = (TextView) view.findViewById(R.id.text);
        image = (ImageView) view.findViewById(R.id.image);

        allFont.setfont(getContext(), "SERIF", "fonts/Yrsa-Light.ttf");

        postList = (RecyclerView) view.findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);
        //mFollowing = new ArrayList<>();


        DisplayAllUsersPosts();
        isOnline();


        return view;
    }

    private void getFollowing() {
        try {
            currentUserId = mAuth.getCurrentUser().getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child("Friends")
                    .child(currentUserId);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                            id = singleSnapshot.child("uid").getValue().toString();
                            DisplayAllUsersPosts();
                            Log.d(TAG, "onDataChange: found user: " + id);
                        }
                        //mFollowing.add(currentUserId);
                        //get the photos
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }catch (NullPointerException e){

        }
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

    private void DisplayAllUsersPosts() {
        final Query sortPostsInDecendingOrder = postsRef.orderByChild("counter");
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(
                        Posts.class,
                        R.layout.all_posts_layout,
                        PostsViewHolder.class,
                        sortPostsInDecendingOrder
                )

                {
                    @Override
                    protected void populateViewHolder(final PostsViewHolder viewHolder, final Posts model, int position) {

                        final String postKey = getRef(position).getKey();
                        sortPostsInDecendingOrder.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){

                                    image.setVisibility(View.GONE);
                                    text.setVisibility(View.GONE);

                                    viewHolder.setFullname(model.getFullname());
                                    viewHolder.setDate(model.getDate());
                                    viewHolder.setDescription(model.getDescription());
                                    //viewHolder.setProfileimage(getActivity().getApplicationContext(), model.getProfileimage());
                                    viewHolder.setPostimage(getActivity(), model.getPostimage());

                                    final String key = postKey.substring(0, 28);
                                    Log.d(TAG, "keyId: " + key);

                                    usersRef.child(key).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                final String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                                                viewHolder.setProfileimage(getContext(), profileImage);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    viewHolder.setLikeButtonStatus(postKey);

                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent clickPostIntent = new Intent(getContext(), ClickPostActivity.class);
                                            clickPostIntent.putExtra("PostKey", postKey);
                                            startActivity(clickPostIntent);
                                        }
                                    });

                                    viewHolder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent commentIntent = new Intent(getContext(), CommentActivity.class);
                                            commentIntent.putExtra("PostKey", postKey);
                                            startActivity(commentIntent);
                                        }
                                    });

                                    viewHolder.profileImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent profileIntent = new Intent(getContext(), PersonProfileActivity.class);
                                            profileIntent.putExtra("visit_user_id", key);
                                            startActivity(profileIntent);
                                        }
                                    });

                                    viewHolder.userName.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent userIntent = new Intent(getContext(), PersonProfileActivity.class);
                                            userIntent.putExtra("visit_user_id", key);
                                            startActivity(userIntent);
                                        }
                                    });

                                    try {
                                        currentUserId = mAuth.getCurrentUser().getUid();
                                        viewHolder.likePostButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                likeChecker = true;

                                                likesRef.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (likeChecker.equals(true)) {
                                                            if (dataSnapshot.child(postKey).hasChild(currentUserId)) {
                                                                likesRef.child(postKey).child(currentUserId).removeValue();
                                                                likeChecker = false;
                                                            } else {
                                                                likesRef.child(postKey).child(currentUserId).setValue(true);
                                                                likeChecker = false;
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        });
                                    } catch (NullPointerException e) {

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                };
            postList.setAdapter(firebaseRecyclerAdapter);
        }

    }

    class PostsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageView likePostButton, commentPostButton;
        TextView displayNoOfLikes, userName;
        int countLikes;
        String currentUserId;
        DatabaseReference likesRef;
        CircleImageView profileImage;

        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            likePostButton = (ImageView) mView.findViewById(R.id.likes_button);
            commentPostButton = (ImageView) mView.findViewById(R.id.comment_button);
            displayNoOfLikes = (TextView) mView.findViewById(R.id.display_number_likes);
            userName = (TextView) mView.findViewById(R.id.post_user_name);
            profileImage = (CircleImageView)  mView.findViewById(R.id.post_profile_image);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        }

        public void setLikeButtonStatus(final String postKey){
            try {
                currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(postKey).hasChild(currentUserId)){
                            countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                            likePostButton.setImageResource(R.drawable.like);
                            displayNoOfLikes.setText((Integer.toString(countLikes) + (" Likes")));
                        }else {
                            countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                            likePostButton.setImageResource(R.drawable.dislike);
                            displayNoOfLikes.setText((Integer.toString(countLikes) + (" Likes")));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }catch (NullPointerException e){

            }
        }

        public void setFullname(String fullname){
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(image);
        }

        public void setDate(String date){
            TextView postDate = (TextView) mView.findViewById(R.id.post_date);
            postDate.setText("  " + date);
        }

        public void setDescription(String description){
            TextView postDescription = (TextView) mView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }

        public void setPostimage(Context ctx1, String postimage){
            ImageView postImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx1).load(postimage).into(postImage);
        }
    }
