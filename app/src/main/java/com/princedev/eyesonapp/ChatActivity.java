package com.princedev.eyesonapp;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.isapanah.awesomespinner.AwesomeSpinner;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.princedev.eyesonapp.Models.Message;
import com.princedev.eyesonapp.Utils.MessagesAdapter;
import com.princedev.eyesonapp.Utils.YandexTranslateAPI;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;

public class ChatActivity extends AppCompatActivity {

    Context context = this;

    private Toolbar chatToolBar;
    private ImageButton languageButton;
    private FloatingActionButton sendMessageButton;
    private EditText userMessageInput;
    private RecyclerView userMessageList;

    private TextView receiverName;
    private CircleImageView receiverProfileImage;
    private final List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    private MaterialSearchView searchView;
    private String messageReceiverID, messageReceiverName, messageSenderID, saveCurrentDate, saveCurrentTime, currentUserId;
    private String changeLanguage, translateLanguage, selectedLanguage, selectedTranslate;

    private DatabaseReference rootRef, userRef;
    private FirebaseAuth mAuth;
    private String translationResult = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        languageButton = (ImageButton) findViewById(R.id.language_button);

        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.spinner_dialog, null);
                builder.setTitle("Change Language");
                final AwesomeSpinner spinnerLanguage = (AwesomeSpinner) mView.findViewById(R.id.language_spinner);
                final AwesomeSpinner translateSpinner = (AwesomeSpinner) mView.findViewById(R.id.translate_spinner);

                    //spinnerLanguage
                    ArrayAdapter<CharSequence> language = ArrayAdapter.createFromResource(ChatActivity.this, R.array.native_arrays, android.R.layout.simple_spinner_item);
                    spinnerLanguage.setAdapter(language, 0);
                    //spinnerLanguage
                    ArrayAdapter<CharSequence> translate = ArrayAdapter.createFromResource(ChatActivity.this, R.array.native_arrays, android.R.layout.simple_spinner_item);
                    translateSpinner.setAdapter(translate, 0);

                    spinnerLanguage.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
                        @Override
                        public void onItemSelected(int position, String itemAtPosition) {
                            selectedLanguage = itemAtPosition.substring(0,2);
                        }
                    });

                    translateSpinner.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
                        @Override
                        public void onItemSelected(int position, String itemAtPosition) {
                            selectedTranslate = itemAtPosition.substring(0,2);
                        }
                    });

                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        changeLanguage = selectedLanguage;
                        translateLanguage = selectedTranslate;
                        Log.d("TRANSLATE", "onClick:" + changeLanguage + "," + translateLanguage);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setView(mView);
                builder.show();

            }
        });

        InitializeFields();
        DisplayReceiverInfo();
        searchTranslate();
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });
        FetchMessages();
    }

    private void searchTranslate(){
        try{
            if (changeLanguage.isEmpty()&&translateLanguage.isEmpty()){
                searchView.setHint("No language to translate");
            }else {
                searchView.setHint(changeLanguage + "-" + translateLanguage);
            }
        }catch (NullPointerException e){

        }
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String languagePair = changeLanguage + "-" + translateLanguage; //("<source_language>-<target_language>")"en-id"
                YandexTranslateAPI yandexTranslateAPI= new YandexTranslateAPI(getApplication().getBaseContext());
                try {
                    translationResult = String.valueOf(yandexTranslateAPI.execute(query, languagePair).get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                }
                userMessageInput.setText(translationResult);
//                View toastView = getLayoutInflater().inflate(R.layout.custom_toast, null);
//                TextView textToast = (TextView) toastView.findViewById(R.id.customToastText);
//                textToast.setText(translationResult);
//                Toast toast = Toast.makeText(context, translationResult, Toast.LENGTH_LONG);
//                toast.setView(toastView);
//                toast.setGravity(Gravity.CENTER, 0,0);
//                toast.show();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_item, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    private void FetchMessages() {
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists()){
                            Message messages = dataSnapshot.getValue(Message.class);
                            messagesList.add(messages);
                            messagesAdapter.notifyDataSetChanged();
                            userMessageList.smoothScrollToPosition(messagesAdapter.getItemCount() - 1);
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage() {

        String messageText = userMessageInput.getText().toString();

        if (TextUtils.isEmpty(messageText)){
//            Toast.makeText(this, "Write a message.", Toast.LENGTH_SHORT).show();
        }else {
            String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference user_message_key = rootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();
            String message_push_id = user_message_key.getKey();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(calForTime.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);

            Map messageBodyDetail = new HashMap();
            messageBodyDetail.put(message_sender_ref + "/" + message_push_id, messageTextBody);
            messageBodyDetail.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

            rootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        userMessageInput.setText("");
                    }else {
                        String message = task.getException().getMessage();
                        Toast.makeText(ChatActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }
                }
            });
        }
        userMessageInput.onEditorAction(EditorInfo.IME_ACTION_DONE);
    }

    private void DisplayReceiverInfo() {

        receiverName.setText(messageReceiverName);
        rootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.with(ChatActivity.this).load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {

        chatToolBar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chatToolBar);

        //connect chat_custom_bar to chat activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        sendMessageButton = (FloatingActionButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_message);

        receiverName = (TextView) findViewById(R.id.custom_profile_name);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessageList = (RecyclerView) findViewById(R.id.messages_list_users);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        userMessageList.smoothScrollToPosition(messagesAdapter.getItemCount()-1);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messagesAdapter);



    }

    private void status(String status){
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        userRef.updateChildren(hashMap);
    }

    @Override
    public void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    public void onPause() {
        super.onPause();
        status("offline");
    }
}
