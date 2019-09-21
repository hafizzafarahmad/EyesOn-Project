package com.princedev.eyesonapp.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.princedev.eyesonapp.ClickPostActivity;
import com.princedev.eyesonapp.CommentActivity;
import com.princedev.eyesonapp.PersonProfileActivity;
import com.princedev.eyesonapp.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Fizz on 26/12/2018.
 */

public class OpenGridActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, postsRef, friendRef;
    private FirebaseAuth.AuthStateListener mAuthListener;


    private String  postKey;
    Boolean likeChecker = false;

    private ImageView likePostButton, commentPostButton, postImage;
    private TextView displayNoOfLikes, userName, postDate, postDescription;
    int countLikes;
    private String currentUserId;
    private DatabaseReference likesRef;
    private CircleImageView profileImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_posts_layout);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        postKey = getIntent().getExtras().get("PostKey").toString();

        likePostButton = (ImageView) findViewById(R.id.likes_button);
        commentPostButton = (ImageView) findViewById(R.id.comment_button);
        displayNoOfLikes = (TextView) findViewById(R.id.display_number_likes);
        userName = (TextView) findViewById(R.id.post_user_name);
        profileImage = (CircleImageView)  findViewById(R.id.post_profile_image);
        postImage = (ImageView) findViewById(R.id.post_image);
        postDate = (TextView) findViewById(R.id.post_date);
        postDescription = (TextView) findViewById(R.id.post_description);

        DisplayPost();
    }

    private void DisplayPost(){
        postsRef.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String imageProfile = dataSnapshot.child("profileimage").getValue().toString();
                String name = dataSnapshot.child("fullname").getValue().toString();
                String image = dataSnapshot.child("postimage").getValue().toString();
                String date = dataSnapshot.child("date").getValue().toString();
                String desc = dataSnapshot.child("description").getValue().toString();

                Picasso.with(OpenGridActivity.this)
                        .load(imageProfile).placeholder(R.drawable.profile).into(profileImage);
                Picasso.with(OpenGridActivity.this)
                        .load(image).placeholder( R.drawable.progress_animation ).into(postImage);
                userName.setText(name);
                postDate.setText("  " + date);
                postDescription.setText(desc);

                setLikeButtonStatus(postKey);
                final String key = postKey.substring(0, 28);

                postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent clickPostIntent = new Intent(getApplication(), ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", postKey);
                        startActivity(clickPostIntent);
                    }
                });

                commentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent commentIntent = new Intent(getApplication(), CommentActivity.class);
                        commentIntent.putExtra("PostKey", postKey);
                        startActivity(commentIntent);
                    }
                });

                profileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent = new Intent(getApplication(), PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", key);
                        startActivity(profileIntent);
                    }
                });

                userName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent userIntent = new Intent(getApplication(), PersonProfileActivity.class);
                        userIntent.putExtra("visit_user_id", key);
                        startActivity(userIntent);
                    }
                });

                try {
                    currentUserId = mAuth.getCurrentUser().getUid();
                    likePostButton.setOnClickListener(new View.OnClickListener() {
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
}
