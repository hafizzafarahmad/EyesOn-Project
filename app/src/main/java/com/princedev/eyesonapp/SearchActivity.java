package com.princedev.eyesonapp;

import android.content.Context;
import android.content.Intent;
import android.support.design.internal.NavigationMenu;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.isapanah.awesomespinner.AwesomeSpinner;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.listeners.OnCountryPickerListener;
import com.princedev.eyesonapp.Models.SearchFriend;
import com.princedev.eyesonapp.Utils.BottomNavigationViewHelper;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.yavski.fabspeeddial.FabSpeedDial;

public class SearchActivity extends AppCompatActivity implements OnCountryPickerListener {

    private static final String TAG = "SearchActivity";

    private static final int ACTIVITY_NUM = 1;
    private Context mContext = SearchActivity.this;

    private EditText searchInputText;
    private RecyclerView searchResultList;

    private DatabaseReference allUsersDatabaseRef;
    private FirebaseAuth mAuth;
    private String currentUserId, nationality;
    private CountryPicker countryPicker;
    private AwesomeSpinner searchNative, searchLearning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        searchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

        //by Name
        searchInputText = (EditText) findViewById(R.id.search_input);
        searchInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchBoxInput = searchInputText.getText().toString();
                searchFriends(searchBoxInput);
            }
        });

        //by Native
        searchNative = (AwesomeSpinner) findViewById(R.id.search_nativeSpeaker);
        ArrayAdapter<CharSequence> speak = ArrayAdapter.createFromResource(this, R.array.native_arrays, android.R.layout.simple_spinner_item);
        searchNative.setAdapter(speak, 0);
        searchNative.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
            @Override
            public void onItemSelected(int position, String itemAtPosition) {
                Query searchFriendsQuery = allUsersDatabaseRef.orderByChild("native_speaker").equalTo(searchNative.getSelectedItem().substring(5));
                searchQuery(searchFriendsQuery);
            }
        });

        //by Learning
        searchLearning = (AwesomeSpinner) findViewById(R.id.search_learning);
        ArrayAdapter<CharSequence> learn = ArrayAdapter.createFromResource(this, R.array.native_arrays, android.R.layout.simple_spinner_item);
        searchLearning.setAdapter(learn, 0);
        searchLearning.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
            @Override
            public void onItemSelected(int position, String itemAtPosition) {
                Query searchFriendsQuery = allUsersDatabaseRef.orderByChild("learning").equalTo(searchLearning.getSelectedItem().substring(5));
                searchQuery(searchFriendsQuery);
            }
        });

        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.fab_search);
        fabSpeedDial.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.search_nationality:
                        searchInputText.setVisibility(View.VISIBLE);
                        searchNative.setVisibility(View.GONE);
                        searchLearning.setVisibility(View.GONE);
                        nationality();
                        break;

                    case R.id.search_native:
                        searchInputText.setVisibility(View.GONE);
                        searchNative.setVisibility(View.VISIBLE);
                        searchLearning.setVisibility(View.GONE);
                        break;

                    case R.id.search_learn:
                        searchInputText.setVisibility(View.GONE);
                        searchNative.setVisibility(View.GONE);
                        searchLearning.setVisibility(View.VISIBLE);
                        break;

                    case R.id.search_by_input:
                        searchInputText.setVisibility(View.VISIBLE);
                        searchNative.setVisibility(View.GONE);
                        searchLearning.setVisibility(View.GONE);
                        break;
                }
                return false;
            }

            @Override
            public void onMenuClosed() {

            }
        });

        hideSoftKeyboard();
        setupBottomNavigationView();

    }

    private void hideSoftKeyboard(){
        if (getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void searchQuery(Query searchFriendsQuery){
        FirebaseRecyclerAdapter<SearchFriend, FindFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<SearchFriend, FindFriendsViewHolder>(
                SearchFriend.class,
                R.layout.all_users_display_layout,
                FindFriendsViewHolder.class,
                searchFriendsQuery

        ) {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder viewHolder, SearchFriend model, final int position) {
                viewHolder.setFullname(model.getFullname());
                viewHolder.setAge(model.getAge());
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visit_user_id = getRef(position).getKey();
                        if (visit_user_id.equals(currentUserId)){
                            Intent profileIntent = new Intent(SearchActivity.this, ProfileActivity.class);
                            startActivity(profileIntent);
                        }else {
                            Intent profileIntent = new Intent(SearchActivity.this, PersonProfileActivity.class);
                            profileIntent.putExtra("visit_user_id", visit_user_id);
                            startActivity(profileIntent);
                        }
                    }
                });
            }
        };

        searchResultList.setAdapter(firebaseRecyclerAdapter);
    }

    private void searchNationality(){
        Query searchFriendsQuery = allUsersDatabaseRef.orderByChild("nationality").equalTo(nationality);
        FirebaseRecyclerAdapter<SearchFriend, FindFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<SearchFriend, FindFriendsViewHolder>(
                SearchFriend.class,
                R.layout.all_users_display_layout,
                FindFriendsViewHolder.class,
                searchFriendsQuery

        ) {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder viewHolder, SearchFriend model, final int position) {
                viewHolder.setFullname(model.getFullname());
                viewHolder.setAge(model.getAge());
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visit_user_id = getRef(position).getKey();
                        if (visit_user_id.equals(currentUserId)){
                            Intent profileIntent = new Intent(SearchActivity.this, ProfileActivity.class);
                            startActivity(profileIntent);
                        }else {
                            Intent profileIntent = new Intent(SearchActivity.this, PersonProfileActivity.class);
                            profileIntent.putExtra("visit_user_id", visit_user_id);
                            startActivity(profileIntent);
                        }
                    }
                });
            }
        };

        searchResultList.setAdapter(firebaseRecyclerAdapter);
    }

    private void searchFriends(String searchBoxInput) {

        if (searchBoxInput.length() == 0 ){

        }else {
            Query searchFriendsQuery = allUsersDatabaseRef.orderByChild("fullname")
                    .startAt(searchBoxInput)
                    .endAt(searchBoxInput + "\uf8ff");

            FirebaseRecyclerAdapter<SearchFriend, FindFriendsViewHolder> firebaseRecyclerAdapter
                    = new FirebaseRecyclerAdapter<SearchFriend, FindFriendsViewHolder>(
                    SearchFriend.class,
                    R.layout.all_users_display_layout,
                    FindFriendsViewHolder.class,
                    searchFriendsQuery

            ) {
                @Override
                protected void populateViewHolder(FindFriendsViewHolder viewHolder, SearchFriend model, final int position) {
                    viewHolder.setFullname(model.getFullname());
                    viewHolder.setAge(model.getAge());
                    viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String visit_user_id = getRef(position).getKey();
                            if (visit_user_id.equals(currentUserId)){
                                Intent profileIntent = new Intent(SearchActivity.this, ProfileActivity.class);
                                startActivity(profileIntent);
                            }else {
                                Intent profileIntent = new Intent(SearchActivity.this, PersonProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                            }
                        }
                    });
                }
            };

            searchResultList.setAdapter(firebaseRecyclerAdapter);
        }


    }

    // Search nationality
    private void nationality(){
        CountryPicker.Builder builder = new CountryPicker.Builder().with(this).listener(this);
        builder.theme(CountryPicker.THEME_NEW);
        builder.canSearch(true);
        builder.sortBy(CountryPicker.SORT_BY_NAME);
        countryPicker = builder.build();
        countryPicker.showBottomSheet(this);
    }
    private void showResultActivity(Country country) {
        nationality = country.getName();
        searchInputText.setText("");
        searchInputText.setHint(nationality);
        searchNationality();
    }
    @Override
    public void onSelectCountry(Country country) {
        showResultActivity(country);
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FindFriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFullname(String fullname){
            TextView myName = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }

        public void setAge(String age){
            TextView myStatus = (TextView) mView.findViewById(R.id.all_users_age);
            myStatus.setText(age + " Years Old");
        }
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
