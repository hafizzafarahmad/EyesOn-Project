package com.princedev.eyesonapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.princedev.eyesonapp.Fragment.PersonPostFragment;
import com.princedev.eyesonapp.Fragment.PersonProfileFragment;
import com.princedev.eyesonapp.Fragment.ProfileFragment;
import com.princedev.eyesonapp.Fragment.ProfileMyPostFragment;
import com.princedev.eyesonapp.Utils.BottomNavigationViewHelper;
import com.princedev.eyesonapp.Utils.SectionsPagerAdapter;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private static final String TAG = "PersonProfileActivity";

    private static final int ACTIVITY_NUM = 4;
    private Context mContext = PersonProfileActivity.this;

    private ViewPager viewPager;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, friendsRef, postRef, friendRequestRef, chatRef;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView userUsername, userFullName, userAge, userAboutme, userHobbies;
    private CircleImageView userProfilePic;

    private Toolbar mToolbar;
    private Button sendFriendRequestButton, declineFriendRequestButton;
    private TextView mLogout;

    private String currentUserId;

    private String saveCurrentDate, senderUserId, receiverUserId, CURRENT_STATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        chatRef = FirebaseDatabase.getInstance().getReference().child("Messages");

        setupWidget();
        setupViewPager();
        setupBottomNavigationView();

        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String username = dataSnapshot.child("username").getValue().toString();
                    String profileName = dataSnapshot.child("fullname").getValue().toString();
                    String age = dataSnapshot.child("age").getValue().toString();
//                    String aboutMe = dataSnapshot.child("aboutme").getValue().toString();
//                    String hobbies = dataSnapshot.child("hobbies").getValue().toString();

                    Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfilePic);
                    userUsername.setText("@" + username);
                    userFullName.setText(profileName);
                    userAge.setText(age + " Years Old");
//                    userAboutme.setText(aboutMe);
//                    userHobbies.setText(hobbies);

                    MaintenanceOfButton();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        declineFriendRequestButton.setVisibility(View.INVISIBLE);
        declineFriendRequestButton.setEnabled(false);

        if (!senderUserId.equals(receiverUserId)){
            sendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendFriendRequestButton.setEnabled(false);
                    if(CURRENT_STATE.equals("not_friends")){
                        SendFriendRequest();
                    }if (CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest();
                    }if (CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }if (CURRENT_STATE.equals("friends")){
                        Unfriend();
                    }
                }
            });
        }else {
            declineFriendRequestButton.setVisibility(View.INVISIBLE);
            sendFriendRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PersonProfileFragment()); //index 0
        adapter.addFragment(new PersonPostFragment()); //index 1
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText("About");
        tabLayout.getTabAt(1).setText("Posts");
    }

    private void Unfriend() {
        friendsRef.child(senderUserId).child(receiverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendsRef.child(receiverUserId).child(senderUserId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        sendFriendRequestButton.setEnabled(true);
                                        CURRENT_STATE = "not_friends";
                                        sendFriendRequestButton.setText("Add Friend");

                                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                        declineFriendRequestButton.setEnabled(false);

                                        chatRef.child(senderUserId).child(receiverUserId)
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    chatRef.child(receiverUserId).child(senderUserId)
                                                            .removeValue();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                }
            }
        });
    }

    private void AcceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        friendsRef.child(senderUserId)
                .child(receiverUserId)
                .child("date")
                .setValue(saveCurrentDate);

        friendsRef.child(senderUserId)
                .child(receiverUserId)
                .child("uid")
                .setValue(receiverUserId);

        friendsRef.child(receiverUserId)
                .child(senderUserId)
                .child("date")
                .setValue(saveCurrentDate);

        friendsRef.child(receiverUserId)
                .child(senderUserId)
                .child("uid")
                .setValue(senderUserId);

        friendRequestRef.child(senderUserId)
                .child(receiverUserId)
                .removeValue();

        friendRequestRef.child(receiverUserId)
                .child(senderUserId)
                .removeValue();

        sendFriendRequestButton.setEnabled(true);
        CURRENT_STATE = "friends";
        sendFriendRequestButton.setText("Unfriend");

        declineFriendRequestButton.setVisibility(View.INVISIBLE);
        declineFriendRequestButton.setEnabled(false);


    }

    private void SendFriendRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendRequestRef.child(receiverUserId).child(senderUserId)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        sendFriendRequestButton.setEnabled(true);
                                        CURRENT_STATE = "request_sent";
                                        sendFriendRequestButton.setText("Cancel");

                                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                        declineFriendRequestButton.setEnabled(false);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void MaintenanceOfButton() {

        friendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserId)){
                    String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if (request_type.equals("sent")){
                        CURRENT_STATE = "request_sent";
                        sendFriendRequestButton.setText("Cancel");

                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                        declineFriendRequestButton.setEnabled(false);
                    }else if (request_type.equals("received")){
                        CURRENT_STATE = "request_received";

                        sendFriendRequestButton.setText("Accept");
                        declineFriendRequestButton.setVisibility(View.VISIBLE);
                        declineFriendRequestButton.setEnabled(true);

                        declineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelFriendRequest();
                            }
                        });
                    }
                }else {
                    friendsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserId)){
                                CURRENT_STATE = "friends";
                                sendFriendRequestButton.setText("Unfriend");

                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                declineFriendRequestButton.setEnabled(false);
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
    }

    private void CancelFriendRequest() {

        friendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendRequestRef.child(receiverUserId).child(senderUserId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        sendFriendRequestButton.setEnabled(true);
                                        CURRENT_STATE = "not_friends";
                                        sendFriendRequestButton.setText("Add Friend");

                                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                        declineFriendRequestButton.setEnabled(false);
                                    }
                                }
                            });
                }
            }
        });

    }

    private void setupWidget() {

        mToolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");

        userUsername = (TextView) findViewById(R.id.user_username);
        userFullName = (TextView) findViewById(R.id.user_profile_full_name);
        userAge = (TextView) findViewById(R.id.user_age);
//        userAboutme = (TextView) findViewById(R.id.user_about);
//        userHobbies = (TextView) findViewById(R.id.user_hobbies);
        userProfilePic = (CircleImageView) findViewById(R.id.user_profile_pic);
        sendFriendRequestButton = (Button) findViewById(R.id.person_send_friend_request_btn);
        declineFriendRequestButton = (Button) findViewById(R.id.person_decline_friend_request);

        CURRENT_STATE = "not_friends";
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
