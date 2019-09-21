package com.princedev.eyesonapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

//    private ImageView postImage;
    private TextView viewUsername, postDescription;
    private ImageButton backArrow;
    private ZoomageView postImage;

    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;

    private String postKey, currentUserId, databaseUserId, description, image, viewName;
    boolean isImageFitToScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        postKey = getIntent().getExtras().get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);

        postDescription = (TextView) findViewById(R.id.post_description);
        viewUsername = (TextView) findViewById(R.id.view_username);
        postImage = (ZoomageView) findViewById(R.id.post_image);
        backArrow = (ImageButton) findViewById(R.id.backArrow);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();
                    databaseUserId = dataSnapshot.child("uid").getValue().toString();
                    viewName = dataSnapshot.child("fullname").getValue().toString();

                    viewUsername.setText(viewName);
                    postDescription.setText(description);
                    Picasso.with(ClickPostActivity.this)
                            .load(image)
                            .placeholder( R.drawable.progress_animation )
                            .into(postImage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClickPostActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
