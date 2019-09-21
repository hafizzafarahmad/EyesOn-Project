package com.princedev.eyesonapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.princedev.eyesonapp.Models.Comment;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    private ImageButton postCommentButton, backArrow;
    private EditText commentInputText;
    private RecyclerView commentsList;
    private ImageView commentPostImage;
    private TextView commentCaption, commentDate, commentLikes;

    private DatabaseReference usersRef, postsRef, postCommentRef, likesRef;
    private FirebaseAuth mAuth;

    private int countLikes, mPageEndOffset, mPageLimit ;
    private int currentPage = 0;

    private String post_Key, current_user_id;
    private long countComment = 0;
    private int positionComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

//        mPageEndOffset = 0;
//        mPageLimit = 6;
//
//        mPageEndOffset += mPageLimit;

        post_Key = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_Key).child("Comments");
        postCommentRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_Key);
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        commentsList = (RecyclerView) findViewById(R.id.comments_list);
        commentsList.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setHasFixedSize(true);
        commentsList.setLayoutManager(linearLayoutManager);

        commentInputText = (EditText) findViewById(R.id.comment_input);
        postCommentButton = (ImageButton) findViewById(R.id.post_comment_button);
        commentPostImage = (ImageView) findViewById(R.id.comment_post_image);
        commentCaption = (TextView) findViewById(R.id.comment_caption);
        commentDate = (TextView) findViewById(R.id.comment_date);
        commentLikes = (TextView) findViewById(R.id.comment_likes);
        backArrow = (ImageButton) findViewById(R.id.backArrow);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            final String userName = dataSnapshot.child("username").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                            postsRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        countComment = dataSnapshot.getChildrenCount();
                                        ValidateComment(userName, profileImage, countComment);
                                        commentInputText.setText("");
                                    }else {
                                        countComment = 0;
                                        ValidateComment(userName, profileImage, countComment);
                                        commentInputText.setText("");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                commentInputText.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });
        init();
    }

    private void init() {

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    countLikes = (int) dataSnapshot.child(post_Key).getChildrenCount();
                    commentLikes.setText((Integer.toString(countLikes) + (" Likes")));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        postCommentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String postImage = dataSnapshot.child("postimage").getValue().toString();
                    String caption = dataSnapshot.child("description").getValue().toString();
                    String date = dataSnapshot.child("date").getValue().toString();


                    Picasso.with(CommentActivity.this).load(postImage).placeholder(R.drawable.profile).into(commentPostImage);
                    commentCaption.setText(caption);
                    commentDate.setText(date);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Comment, CommentsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Comment, CommentsViewHolder>(
                Comment.class,
                R.layout.all_comments_layout,
                CommentsViewHolder.class,
                postsRef.orderByChild("id")
        ) {
            @Override
            protected void populateViewHolder(CommentsViewHolder viewHolder, Comment model, int position) {
                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
                viewHolder.setProfileimage(getApplication(), model.getProfileimage());

                final String key = getRef(position).getKey().substring(0, 28);

                viewHolder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent = new Intent(CommentActivity.this, PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", key);
                        startActivity(profileIntent);
                    }
                });

                viewHolder.myUserName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent userIntent = new Intent(CommentActivity.this, PersonProfileActivity.class);
                        userIntent.putExtra("visit_user_id", key);
                        startActivity(userIntent);
                    }
                });
            }
        };

        positionComment = firebaseRecyclerAdapter.getItemCount();
        firebaseRecyclerAdapter.notifyDataSetChanged();
        commentsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView myUserName, myComment, myDate, myTime;
        CircleImageView image;

        public CommentsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setUsername(String username){
            myUserName = (TextView) mView.findViewById(R.id.comment_username);
            myUserName.setText("@" + username + "  ");
        }

        public void setComment(String comment){
            myComment = (TextView) mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }

        public void setDate(String date){
            myDate = (TextView) mView.findViewById(R.id.comment_date);
            myDate.setText(" Date: " + date);
        }

        public void setTime(String time){
            myTime = (TextView) mView.findViewById(R.id.comment_time);
            myTime.setText(time);
        }

        public void setProfileimage(Context ctx, String profileimage) {
            image = (CircleImageView) mView.findViewById(R.id.comment_user_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(image);
        }


    }

    private void ValidateComment(String userName, String profileImage, long count) {
        String commentText = commentInputText.getText().toString();
        if (TextUtils.isEmpty(commentText)){

        }else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            final String saveCurrentTime = currentTime.format(calForTime.getTime());

            final String randomKey = current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentMap = new HashMap();
            commentMap.put("uid", current_user_id);
            commentMap.put("comment", commentText);
            commentMap.put("date", saveCurrentDate);
            commentMap.put("time", saveCurrentTime);
            commentMap.put("username", userName);
            commentMap.put("profileimage", profileImage);
            commentMap.put("id", count);

            postsRef.child(randomKey).updateChildren(commentMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        commentsList.smoothScrollToPosition((int) dataSnapshot.getChildrenCount());
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }else {
                                Toast.makeText(CommentActivity.this, "error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }
}
