package com.princedev.eyesonapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.isapanah.awesomespinner.AwesomeSpinner;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.listeners.OnCountryPickerListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements OnCountryPickerListener {

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference settingsUserRef, postsRef;
    private StorageReference userProfileImageRef;

    private ProgressDialog loadingBar;

    private EditText editUsername, editFullName, editAge, editAboutme, editHobbies;
    private ImageButton editSaveProfileButton, editNativeButton, editLearnButton;
    private TextView changeProfilePhoto, editTextNative, editTextLearn;
    private CircleImageView editProfileImage;
    private Button edtNationality;
    private CountryPicker countryPicker;
    private AwesomeSpinner editNative, editLearning;

    private String currentUserId;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(this);
        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editUsername = (EditText) findViewById(R.id.edit_username);
        editFullName = (EditText) findViewById(R.id.edit_full_name);
        editAge = (EditText) findViewById(R.id.edit_age);
        editAboutme = (EditText) findViewById(R.id.edit_about);
        editHobbies = (EditText) findViewById(R.id.edit_hobbies);
        editSaveProfileButton = (ImageButton) findViewById(R.id.save_edit_profile_button);
        editProfileImage = (CircleImageView) findViewById(R.id.edit_profileimage);
        changeProfilePhoto = (TextView) findViewById(R.id.changeProfilePhoto);
        edtNationality = (Button) findViewById(R.id.edit_nationality);
        editNative = (AwesomeSpinner) findViewById(R.id.edit_nativeSpeaker);
        editLearning = (AwesomeSpinner) findViewById(R.id.edit_learning);
        editTextNative = (TextView) findViewById(R.id.textEditNative);
        editTextLearn = (TextView) findViewById(R.id.textEditLearn);
        editNativeButton = (ImageButton) findViewById(R.id.editNativeButton);
        editLearnButton = (ImageButton) findViewById(R.id.editLearnButton);

        init();

    }

    private void init(){
        settingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUsername = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileAge = dataSnapshot.child("age").getValue().toString();
                    String myAboutMe = dataSnapshot.child("aboutme").getValue().toString();
                    String myInterest = dataSnapshot.child("hobbies").getValue().toString();
                    String myNationality = dataSnapshot.child("nationality").getValue().toString();
                    String myNative = dataSnapshot.child("native_speaker").getValue().toString();
                    String myLearn = dataSnapshot.child("learning").getValue().toString();

                    //Spinner Native
                    if (myNative.equals("")){

                    }else {
                        editTextNative.setVisibility(View.VISIBLE);
                        editNativeButton.setVisibility(View.VISIBLE);
                        editNative.setVisibility(View.GONE);
                    }
                    //Spinner Learning
                    if (myLearn.equals("")){

                    }else {
                        editTextLearn.setVisibility(View.VISIBLE);
                        editLearnButton.setVisibility(View.VISIBLE);
                        editLearning.setVisibility(View.GONE);
                    }

                    Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(editProfileImage);
                    editUsername.setText(myUsername);
                    editFullName.setText(myProfileName);
                    editAge.setText(myProfileAge);
                    editAboutme.setText(myAboutMe);
                    editHobbies.setText(myInterest);
                    edtNationality.setText(myNationality);
                    editTextNative.setText(myNative);
                    editTextLearn.setText(myLearn);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        editSaveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = editUsername.getText().toString();
                String profilename = editFullName.getText().toString();
                String age = editAge.getText().toString();
                String aboutme = editAboutme.getText().toString();
                String hobbies = editHobbies.getText().toString();
                String nation = edtNationality.getText().toString();
                String nativeSpeak = editTextNative.getText().toString();
                String learn = editTextLearn.getText().toString();

                HashMap userMap = new HashMap();
                userMap.put("username", username);
                userMap.put("fullname", profilename);
                userMap.put("age", age);
                userMap.put("aboutme", aboutme);
                userMap.put("hobbies", hobbies);
                userMap.put("nationality", nation);
                userMap.put("native_speaker", nativeSpeak);
                userMap.put("learning", learn);

                settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){

                            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();
//                            Toast.makeText(SettingsActivity.this, "Account Telah Diupdate.", Toast.LENGTH_SHORT).show();
                            //loadingBar.dismiss();
                        }else {
                            Toast.makeText(SettingsActivity.this, "Error.", Toast.LENGTH_SHORT).show();
                           // loadingBar.dismiss();
                        }
                    }
                });
            }
        });

        edtNationality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nationality();
            }
        });

        //Spinner Native
        ArrayAdapter<CharSequence> speak = ArrayAdapter.createFromResource(this, R.array.native_arrays, android.R.layout.simple_spinner_item);
        editNative.setAdapter(speak, 0);
        editNative.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
            @Override
            public void onItemSelected(int position, String itemAtPosition) {
                String substring = itemAtPosition.substring(5);
                editTextNative.setText(substring);
            }
        });
        editNativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNative.setVisibility(View.VISIBLE);
                editTextNative.setVisibility(View.GONE);
                editNativeButton.setVisibility(View.GONE);
            }
        });

        //Spinner Learn
        ArrayAdapter<CharSequence> learn = ArrayAdapter.createFromResource(this, R.array.native_arrays, android.R.layout.simple_spinner_item);
        editLearning.setAdapter(learn, 0);
        editLearning.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
            @Override
            public void onItemSelected(int position, String itemAtPosition) {
                String substring = itemAtPosition.substring(5);
                editTextLearn.setText(substring);
            }
        });
        editLearnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editLearning.setVisibility(View.VISIBLE);
                editTextLearn.setVisibility(View.GONE);
                editLearnButton.setVisibility(View.GONE);
            }
        });


        changeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

    }

    // select profile
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){

                loadingBar.setTitle("Updating Profile Image");
                loadingBar.setMessage("Please Wait...");

                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();
                StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){
//                            Toast.makeText(SettingsActivity.this, "Profile Image Telah Disimpan.", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            settingsUserRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                startActivity(selfIntent);

                                                loadingBar.dismiss();
//                                                Toast.makeText(SettingsActivity.this, "Profile Image Disimpan di database", Toast.LENGTH_SHORT).show();
                                            }else {
                                                loadingBar.dismiss();
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }else {
                loadingBar.dismiss();
//                Toast.makeText(this, "Error : Gambar Tidak bisa di Crop", Toast.LENGTH_SHORT).show();
            }
        }
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
        edtNationality.setText(nationality);
    }
    @Override
    public void onSelectCountry(Country country) {
        showResultActivity(country);
    }
}
