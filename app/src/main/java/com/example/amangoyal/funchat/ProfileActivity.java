package com.example.amangoyal.funchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private ImageView mImage;
    private TextView tname, tstatus, tfriends;
    private Button friend_request_btn, decline_request_btn;
    private DatabaseReference userDatabaseReference;
    private ProgressDialog mProgress;
    private DatabaseReference friendReqDatabaseReference;
    private DatabaseReference friendsDatabaseReference;
    private DatabaseReference mNotificationReference;
    private DatabaseReference mrootRef;

    private FirebaseUser currentUser;
    private String currentState = "not_friends";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("user_id");

        mImage = findViewById(R.id.imageView);
        tname = findViewById(R.id.profile_name);
        tstatus = findViewById(R.id.profile_status);
        tfriends = findViewById(R.id.profile_friends);
        friend_request_btn = findViewById(R.id.profile_send_request_button);
        decline_request_btn = findViewById(R.id.profile_decline_request_button);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Fetching data from database");
        mProgress.setTitle("Please Wait");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();


        mrootRef = FirebaseDatabase.getInstance().getReference();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        friendReqDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friend_req");
        friendsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friends");
        mNotificationReference = FirebaseDatabase.getInstance().getReference().child("notifications");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                //    Toast.makeText(ProfileActivity.this, ""+image, Toast.LENGTH_SHORT).show();
                tname.setText(displayName);
                tstatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mImage);


                //----------------------Friend request button state------------------
                friendReqDatabaseReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(userId)) {
                            String req_type = dataSnapshot.child(userId).child("request_type").getValue().toString();
                            if (req_type.equals("received")) {
                                currentState = "req_received";
                                friend_request_btn.setText("Accept friend request");
                                decline_request_btn.setEnabled(true);
                                decline_request_btn.setVisibility(View.VISIBLE);
                                Toast.makeText(ProfileActivity.this, "req_received", Toast.LENGTH_SHORT).show();

                            } else if (req_type.equals("sent")) {
                                currentState = "req_sent";
                                friend_request_btn.setText("Cancel friend request");
                                decline_request_btn.setVisibility(View.INVISIBLE);
                                decline_request_btn.setEnabled(false);
                                Toast.makeText(ProfileActivity.this, "req_sent", Toast.LENGTH_SHORT).show();

                            }

                            mProgress.dismiss();

                        } else {
                            decline_request_btn.setVisibility(View.INVISIBLE);
                            decline_request_btn.setEnabled(false);

                            friendsDatabaseReference.child(currentUser.getUid()).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(userId)) {
                                                friend_request_btn.setText("Unfriend this person");
                                                currentState = "friends";
                                                Toast.makeText(ProfileActivity.this, "friends", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ProfileActivity.this, "not_friends", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                            mProgress.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        friend_request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                friend_request_btn.setEnabled(false);

                //----------------------Send friend request-------------
                if (currentState.equals("not_friends")) {

                    DatabaseReference newNotificationrefrence = mNotificationReference.child(userId).push();
                    String notificationId = newNotificationrefrence.getKey();

                    //Hashmap used to set notification data
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", currentUser.getUid());
                    notificationData.put("type", "request");


                    Map requestMap = new HashMap();
                    requestMap.put("friend_req/" + currentUser.getUid() + "/" + userId + "/request_type", "sent");
                    requestMap.put("friend_req/" + userId + "/" + currentUser.getUid() + "/request_type", "received");
                    requestMap.put("friend_req/" + currentUser.getUid() + "/" + userId + "/timestamp", ServerValue.TIMESTAMP);
                    requestMap.put("friend_req/" + userId + "/" + currentUser.getUid() + "/timestamp", ServerValue.TIMESTAMP);
                    requestMap.put("notifications/" + userId + "/" + notificationId, notificationData);

                    mrootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "Error in sending Req", Toast.LENGTH_SHORT).show();
                            } else {
                                currentState = "req_sent";
                                friend_request_btn.setText("Cancel friend request");
                                decline_request_btn.setVisibility(View.INVISIBLE);
                                decline_request_btn.setEnabled(false);
                                friend_request_btn.setEnabled(true);

                                Toast.makeText(ProfileActivity.this, "Friend req sent", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }

                //----------------------Cancel request---------------------
                if (currentState.equals("req_sent")) {

                    Map cancelReq = new HashMap();
                    cancelReq.put("friend_req/" + userId + "/" + currentUser.getUid(), null);
                    cancelReq.put("friend_req/" + currentUser.getUid() + "/" + userId, null);

                    mrootRef.updateChildren(cancelReq, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                friend_request_btn.setEnabled(true);
                                friend_request_btn.setText("Send Friend request");
                                currentState = "not_friends";
                                decline_request_btn.setVisibility(View.INVISIBLE);
                                decline_request_btn.setEnabled(false);
                            } else {
                                Toast.makeText(ProfileActivity.this, "Error in cancelling request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }

                //----------------------------Friend request received state-----------------------
                if (currentState.equals("req_received")) {
                    DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                    dateFormatter.setLenient(false);
                    Date today = new Date();
                    final String currentDate = dateFormatter.format(today);
                    //   final String currentDat = DateFormat.getDateInstance().format(new Date());


                    Map reqReceived = new HashMap();
                    reqReceived.put("friends/" + currentUser.getUid() + "/" + userId, currentDate);
                    reqReceived.put("friends/" + userId + "/" + currentUser.getUid() + "/", currentDate);

                    reqReceived.put("friend_req/" + currentUser.getUid() + "/" + userId + "/request_type", null);
                    reqReceived.put("friend_req/" + userId + "/" + currentUser.getUid() + "/request_type", null);

                    mrootRef.updateChildren(reqReceived, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                friend_request_btn.setEnabled(true);
                                friend_request_btn.setText("Unfriend this person");
                                currentState = "friends";
                                decline_request_btn.setVisibility(View.INVISIBLE);
                                decline_request_btn.setEnabled(false);
                            } else {
                                Toast.makeText(ProfileActivity.this, "Error in accepting request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }

                //--------------------------------Unfriend--------------------------------
                if (currentState.equals("friends")) {
                    Map unfriendButton = new HashMap();
                    unfriendButton.put("friends/" + currentUser.getUid() + "/" + userId, null);
                    unfriendButton.put("friends/" + userId + "/" + currentUser.getUid(), null);

                    mrootRef.updateChildren(unfriendButton, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                friend_request_btn.setEnabled(true);
                                friend_request_btn.setText("Send Friend request");
                                currentState = "not_friends";
                                decline_request_btn.setVisibility(View.INVISIBLE);
                                decline_request_btn.setEnabled(false);
                            } else {
                                Toast.makeText(ProfileActivity.this, "Error in unfriend process", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

            }
        });


        decline_request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map declineReq = new HashMap();
                declineReq.put("friend_req/" + userId + "/" + currentUser.getUid(), null);
                declineReq.put("friend_req/" + currentUser.getUid() + "/" + userId, null);

                mrootRef.updateChildren(declineReq, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {

                            friend_request_btn.setEnabled(true);
                            friend_request_btn.setText("Send Friend request");
                            currentState = "not_friends";
                            decline_request_btn.setVisibility(View.INVISIBLE);
                            decline_request_btn.setEnabled(false);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Error in declining friend req", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mrootRef.child("users").child(currentUser.getUid()).child("online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mrootRef.child("users").child(currentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);

    }
}
