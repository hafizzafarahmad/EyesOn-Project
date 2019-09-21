package com.princedev.eyesonapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.listeners.OnCountryPickerListener;
import com.princedev.eyesonapp.Models.User;
import com.princedev.eyesonapp.Utils.FirebaseMethods;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity implements OnCountryPickerListener {
    private static final String TAG = "RegisterActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods firebaseMethods;

    private Context mContext = RegisterActivity.this;
    private EditText registerEmail, registerName, registerPassword;
    private Button registerButton, selectCountry;
    private ImageView countryFlag;

    private String username, email, password, nation;
    private String append = "";

    private CountryPicker countryPicker;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseMethods = new FirebaseMethods(mContext);
        loading = new ProgressDialog(this);

        initWidget();
        setupFirebaseAuth();
        init();
    }

    private void init(){
        selectCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nationality();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = registerEmail.getText().toString();
                username = registerName.getText().toString();
                password = registerPassword.getText().toString();
                nation = selectCountry.getText().toString();

                if (checkInputs(email, username, password, nation)){
//                    mProgressBar.setVisibility(View.VISIBLE);
//                    loadingPleaseWait.setVisibility(View.VISIBLE);
                    firebaseMethods.registerNewEmail(email, password, username);
                }
            }
        });
    }

    private boolean checkInputs(String email, String username, String password, String nation){
        Log.d(TAG, "checkInputs: checking input for null value.");
        if(email.equals("") || username.equals("") || password.equals("") || nation.equals("")){
            Toast.makeText(mContext, "The field cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initWidget(){
        Log.d(TAG, "initWidget: Initializing Widget.");
        registerEmail = (EditText) findViewById(R.id.register_email);
        registerName = (EditText) findViewById(R.id.register_name);
        registerPassword = (EditText) findViewById(R.id.register_password);
        registerButton = (Button) findViewById(R.id.register_button);
        selectCountry = (Button) findViewById(R.id.select_country_button);
        countryFlag = (ImageView) findViewById(R.id.country_flag);

    }

    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string is null.");

        if(string.equals("")){
            return true;
        }
        else {
            return false;
        }
    }

    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: checking if " + username + " already exists.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("Users")
                .orderByChild("username")
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnaphot: dataSnapshot.getChildren()){
                    if (singleSnaphot.exists()){
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH " + singleSnaphot.getValue(User.class).getUsername());
                        append = myRef.push().getKey().substring(3,10);
                        Log.d(TAG, "onDataChange: username already exist. Appending random string to name: " + append);
                    }
                }
                String mUsername = "";
                mUsername = username + append;

                //menambahkan user ke database
                firebaseMethods.addNewUser(mUsername, username, "" ,"", "",
                        "none", "offline", nation, "", "");
                Toast.makeText(mContext, "Sent Verification Email.", Toast.LENGTH_SHORT).show();

                mAuth.signOut();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // select nationality
    private void nationality(){

        CountryPicker.Builder builder = new CountryPicker.Builder().with(this).listener(this);
        builder.theme(CountryPicker.THEME_NEW);
        builder.canSearch(true);
        builder.sortBy(CountryPicker.SORT_BY_NAME);
        countryPicker = builder.build();
        countryPicker.showBottomSheet(this);
    }

    private void showResultActivity(Country country) {
        String nationality = country.getName();
        countryFlag.setVisibility(View.VISIBLE);
        selectCountry.setText(nationality);
        countryFlag.setImageResource(country.getFlag());
    }

    @Override
    public void onSelectCountry(Country country) {
        showResultActivity(country);
    }

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setup firebase");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            checkIfUsernameExists(username);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    finish();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
