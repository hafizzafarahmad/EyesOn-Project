package com.princedev.eyesonapp.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.princedev.eyesonapp.ClickPostActivity;
import com.princedev.eyesonapp.Models.Posts;
import com.princedev.eyesonapp.R;
import com.princedev.eyesonapp.Utils.OpenGridActivity;
import com.princedev.eyesonapp.Utils.SquareImageView;
import com.squareup.picasso.Picasso;

/**
 * Created by Fizz on 20/08/2018.
 */

public class PersonPostFragment extends Fragment {

    private static final String TAG = "PersonPostFragment";

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, postsRef;

    private ImageView image;
    private TextView text;

    private RecyclerView postList;

    String currentUserId, receiverUserId;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_post, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        receiverUserId = getActivity().getIntent().getExtras().get("visit_user_id").toString();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        text = (TextView) view.findViewById(R.id.text);
        image = (ImageView) view.findViewById(R.id.image);

        postList = (RecyclerView) view.findViewById(R.id.all_my_post_list);
        int numberOfColumns = 3;
        postList.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), numberOfColumns);
        postList.setLayoutManager(gridLayoutManager);

        DisplayAllUsersPosts();

        return view;
    }

    private void DisplayAllUsersPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            final Query query = reference
                    .child("Posts")
                    .orderByChild("uid")
                    .equalTo(receiverUserId);

        FirebaseRecyclerAdapter<Posts, PersonPostFragment.PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PersonPostFragment.PostsViewHolder>(
                        Posts.class,
                        R.layout.all_my_post_layout,
                        PersonPostFragment.PostsViewHolder.class,
                        query
                ) {
                    @Override
                    protected void populateViewHolder(final PersonPostFragment.PostsViewHolder viewHolder, final Posts model, int position) {

                        final String postKey = getRef(position).getKey();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query friends = reference
                                .child("Friends")
                                .child(receiverUserId)
                                .child(currentUserId);
                        friends.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try {
                                    if (dataSnapshot.exists()){
                                        image.setVisibility(View.GONE);
                                        text.setVisibility(View.GONE);

                                        viewHolder.setPostimage(getActivity().getApplicationContext(), model.getPostimage());

                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent clickPostIntent = new Intent(getContext(), OpenGridActivity.class);
                                                clickPostIntent.putExtra("PostKey", postKey);
                                                startActivity(clickPostIntent);
                                            }
                                        });
                                    }else {
                                        image.setVisibility(View.VISIBLE);
                                        text.setText("Not Friend");
                                    }
                                }catch (NullPointerException e){

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        SquareImageView profileImage;

        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            profileImage = (SquareImageView)  mView.findViewById(R.id.post_profile_image);
        }

        public void setPostimage(Context ctx1, String postimage){
            ImageView postImage = (SquareImageView) mView.findViewById(R.id.gridImageView);
            Picasso.with(ctx1).load(postimage).into(postImage);
        }
    }
}
