package com.princedev.eyesonapp.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.princedev.eyesonapp.R;

/**
 * Created by Fizz on 20/08/2018.
 */

public class PersonProfileFragment extends Fragment {

    private static final String TAG = "PersonProfileFragment";

    private FirebaseAuth mAuth;
    private DatabaseReference profileUserRef;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView myAboutme, myHobbies, myNationality;

    String currentUserId, receiverUserId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        receiverUserId = getActivity().getIntent().getExtras().get("visit_user_id").toString();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receiverUserId);

        myNationality = (TextView) view.findViewById(R.id.nationality);
        myAboutme = (TextView) view.findViewById(R.id.my_about);
        myHobbies = (TextView) view.findViewById(R.id.my_hobbies);

        init();

        return view;
    }

    private void init(){
        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String aboutMe = dataSnapshot.child("aboutme").getValue().toString();
                    String hobbies = dataSnapshot.child("hobbies").getValue().toString();
                    String nation = dataSnapshot.child("nationality").getValue().toString();

                    myNationality.setText(nation);
                    myAboutme.setText(aboutMe);
                    myHobbies.setText(hobbies);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
