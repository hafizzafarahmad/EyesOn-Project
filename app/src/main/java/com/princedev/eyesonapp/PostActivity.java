package com.princedev.eyesonapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.princedev.eyesonapp.Utils.BottomNavigationViewHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = "PostActivity";
    private static final int ACTIVITY_NUM = 2;
    private Context mContext = PostActivity.this;

    private Toolbar mToolbar;
    private FloatingActionButton selectPostImage, updatePostButton;
    private ImageButton backArrow;
    private ImageView postImage;
    private EditText postDescription;
    private ProgressDialog loadingBar;

    private static final int Gallery_Pick = 1;
    private Uri imageUri;
    private String description;
    private String saveCurrentDate, saveCurrentTime, randomCurrentDate, randomCurrentTime, postRandomName, downloadUrl, current_user_id;

    private StorageReference postImagesRefrence;
    private DatabaseReference usersRef, postsRef;
    private FirebaseAuth mAuth;

    private long countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

//        mToolbar = (Toolbar) findViewById(R.id.post_toolbar);
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setTitle("Add Post");

        selectPostImage = (FloatingActionButton) findViewById(R.id.select_post_image);
        postDescription = (EditText) findViewById(R.id.post_description);
        updatePostButton = (FloatingActionButton) findViewById(R.id.save_post_button);
        postImage = (ImageView) findViewById(R.id.post_image);
        backArrow = (ImageButton) findViewById(R.id.backArrow);
        loadingBar = new ProgressDialog(this);

        setupFirebaseAuth();
        //setupBottomNavigationView();

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open gallery
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidatePostInfo();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            postImage.setImageURI(imageUri);
        }
    }

    private void ValidatePostInfo() {
        description = postDescription.getText().toString();

        if (imageUri == null){
            Toast.makeText(this, "Pilih Gambar.", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Masukan Caption.", Toast.LENGTH_SHORT).show();
        }else {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please Wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage() {

        Calendar randomForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        randomCurrentDate = currentDate.format(randomForDate.getTime());

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate1 = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate1.format(calForDate.getTime());

        Calendar randomForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        randomCurrentTime = currentTime.format(randomForTime.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime1 = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime1.format(calForTime.getTime());

        postRandomName = randomCurrentDate + randomCurrentTime;

        StorageReference filePath = postImagesRefrence.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    downloadUrl = task.getResult().getDownloadUrl().toString();
                    SavingPostInformationToDatabase();

                    Toast.makeText(PostActivity.this, "image uploaded.", Toast.LENGTH_SHORT).show();
                }else {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SavingPostInformationToDatabase() {

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    countPosts = dataSnapshot.getChildrenCount();
                }else {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", description);
                    postsMap.put("postimage", downloadUrl);
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullname", userFullName);
                    postsMap.put("counter", countPosts);
                    postsRef.child(current_user_id + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        loadingBar.dismiss();
                                        Intent intent = new Intent(PostActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                        Toast.makeText(PostActivity.this, "New Post Telah DiUpdate.", Toast.LENGTH_SHORT).show();
                                    }else {
                                        loadingBar.dismiss();
                                        Toast.makeText(PostActivity.this, "Error Saat Update Post.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    /**
     * setting auth Firebase
     */
    private void setupFirebaseAuth(){

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        postImagesRefrence = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

    }
}
