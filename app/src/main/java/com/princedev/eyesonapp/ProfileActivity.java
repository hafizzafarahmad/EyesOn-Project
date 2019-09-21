package com.princedev.eyesonapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.princedev.eyesonapp.Fragment.ChatFragment;
import com.princedev.eyesonapp.Fragment.HomeFragment;
import com.princedev.eyesonapp.Fragment.ProfileFragment;
import com.princedev.eyesonapp.Fragment.ProfileMyPostFragment;
import com.princedev.eyesonapp.Fragment.RequestFragment;
import com.princedev.eyesonapp.Utils.BottomNavigationViewHelper;
import com.princedev.eyesonapp.Utils.SectionsPagerAdapter;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private static final int ACTIVITY_NUM = 4;
    private Context mContext = ProfileActivity.this;

    private ViewPager viewPager;

    private FirebaseAuth mAuth;
    private DatabaseReference profileUserRef;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView myUsername, myFullName, myAge, myAboutme, myHobbies;
    private CircleImageView myProfilePic;

    private Toolbar mToolbar;
    private Button mEditProfile;
    private ImageButton mLogout;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");

        myUsername = (TextView) findViewById(R.id.my_profile_full_name);
        myFullName = (TextView) findViewById(R.id.my_username);
        myAge = (TextView) findViewById(R.id.my_age);
//        myAboutme = (TextView) findViewById(R.id.my_about);
//        myHobbies = (TextView) findViewById(R.id.my_hobbies);
        myProfilePic = (CircleImageView) findViewById(R.id.my_profile_pic);

        setupFirebaseAuth();
        init();

        mLogout = (ImageButton) findViewById(R.id.logout_button);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PrettyDialog logoutDialog = new PrettyDialog(ProfileActivity.this);
                logoutDialog.setTitle("Logout")
                        .setMessage("are you sure ?")
                        .setIcon(R.drawable.icon_alert)
                        .addButton(
                                "YES",					    // button text
                                R.color.pdlg_color_white,		// button text color
                                R.color.pdlg_color_green,		// button background color
                                new PrettyDialogCallback() {	// button OnClick listener
                                    @Override
                                    public void onClick() {
                                        mAuth.signOut();
                                        finish();
                                    }
                                }
                        ).addButton(
                                "CANCEL",					// button text
                                R.color.pdlg_color_white,
                                R.color.pdlg_color_red,		    // button background color
                                new PrettyDialogCallback() {	// button OnClick listener
                                    @Override
                                    public void onClick() {
                                        logoutDialog.dismiss();
                                    }
                                }
                        );
                logoutDialog.show();


            }
        });

        mEditProfile = (Button) findViewById(R.id.my_edit_profile);
        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setupIntent = new Intent(mContext, SettingsActivity.class);
                startActivity(setupIntent);
                finish();
            }
        });

        setupViewPager();
        isOnline();
        setupBottomNavigationView();
    }

    private void setupViewPager(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ProfileFragment()); //index 0
        adapter.addFragment(new ProfileMyPostFragment()); //index 1
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText("About");
        tabLayout.getTabAt(1).setText("Posts");
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        final PrettyDialog logoutDialog = new PrettyDialog(ProfileActivity.this);

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

    private void init(){
        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String username = dataSnapshot.child("username").getValue().toString();
                    String profileName = dataSnapshot.child("fullname").getValue().toString();
                    String age = dataSnapshot.child("age").getValue().toString();

                    Picasso.with(ProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(myProfilePic);
                    myUsername.setText("@" + username);
                    myFullName.setText(profileName);
                    myAge.setText(age + " Years Old");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    /**
     * setting auth Firebase
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: Setting up Firebase Auth");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    Log.d(TAG, "onAuthStateChanged: navigating to login screen");
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
