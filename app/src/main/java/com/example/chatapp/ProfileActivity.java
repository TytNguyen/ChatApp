package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    CircleImageView image_profile;
    TextView username, status;
    Button btn_request, btn_cancel, btn_accept, btn_decline, btn_remove;
    FirebaseUser fuser;
    DatabaseReference reference, chatRef, contactRef;
    Intent intent;
    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        image_profile = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        status = findViewById(R.id.status);
        btn_request = findViewById(R.id.btn_request);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_accept = findViewById(R.id.btn_accept);
        btn_decline = findViewById(R.id.btn_decline);
        btn_remove = findViewById(R.id.btn_remove);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        intent = getIntent();
        userid = intent.getStringExtra("userid");

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                status.setText(user.getIntro());
                if (user.getImageURL().equals("default")) {
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(image_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        contactRef = FirebaseDatabase.getInstance().getReference("Contacts");

        chatRef = FirebaseDatabase.getInstance().getReference("Chat Requests");
        chatRef.child(fuser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(userid)) {
                            String request_type = dataSnapshot.child(userid).child("request_type").getValue().toString();

                            if (request_type.equals("sent")) {
                                btn_request.setVisibility(View.GONE);
                                btn_cancel.setVisibility(View.VISIBLE);
                            }
                            else if(request_type.equals("received")) {
                                btn_request.setVisibility(View.GONE);
                                btn_cancel.setVisibility(View.GONE);
                                btn_accept.setVisibility(View.VISIBLE);
                                btn_decline.setVisibility(View.VISIBLE);
                            }
                        }
                        else {
                            contactRef.child(fuser.getUid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(userid)) {
                                                btn_remove.setVisibility(View.VISIBLE);
                                                btn_request.setVisibility(View.GONE);
                                                btn_cancel.setVisibility(View.GONE);
                                                btn_accept.setVisibility(View.GONE);
                                                btn_decline.setVisibility(View.GONE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        btn_request.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_decline.setOnClickListener(this);
        btn_accept.setOnClickListener(this);
        btn_remove.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request:
                SendChatRequest();
                Toast.makeText(ProfileActivity.this, "Successfully sent", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_cancel:
                CancelChatRequest();
                Toast.makeText(ProfileActivity.this, "Successfully canceled", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_decline:
                CancelChatRequest();
                Toast.makeText(ProfileActivity.this, "Successfully declined", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_accept :
                AcceptChatRequest();
                Toast.makeText(ProfileActivity.this, "Successfully accepted", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_remove :
                RemoveContact();
                Toast.makeText(ProfileActivity.this, "Successfully removed", Toast.LENGTH_SHORT).show();
                break;
        }
    }



    private void RemoveContact() {
        contactRef.child(fuser.getUid()).child(userid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactRef.child(userid).child(fuser.getUid())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btn_request.setVisibility(View.VISIBLE);
                                                btn_cancel.setVisibility(View.GONE);
                                                btn_accept.setVisibility(View.GONE);
                                                btn_decline.setVisibility(View.GONE);
                                                btn_remove.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        contactRef.child(fuser.getUid()).child(userid)
                .child("id").setValue(userid)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactRef.child(userid).child(fuser.getUid())
                                    .child("id").setValue(fuser.getUid())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            chatRef.child(fuser.getUid()).child(userid)
                                                    .removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                chatRef.child(userid).child(fuser.getUid())
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                btn_remove.setVisibility(View.VISIBLE);
                                                                                btn_request.setVisibility(View.GONE);
                                                                                btn_cancel.setVisibility(View.GONE);
                                                                                btn_accept.setVisibility(View.GONE);
                                                                                btn_decline.setVisibility(View.GONE);
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                        }

                    }
                });
    }

    private void CancelChatRequest() {
        chatRef.child(fuser.getUid()).child(userid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRef.child(userid).child(fuser.getUid())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btn_request.setVisibility(View.VISIBLE);
                                                btn_cancel.setVisibility(View.GONE);
                                                btn_accept.setVisibility(View.GONE);
                                                btn_decline.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        final DatabaseReference notiRef = FirebaseDatabase.getInstance().getReference("Notifications");
        chatRef.child(fuser.getUid()).child(userid)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRef.child(userid).child(fuser.getUid())
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, String> hashMap = new HashMap<>();
                                                hashMap.put("from", fuser.getUid());
                                                hashMap.put("type", "request");

                                                notiRef.child(userid).push()
                                                        .setValue(hashMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    btn_request.setVisibility(View.GONE);
                                                                    btn_cancel.setVisibility(View.VISIBLE);
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
}
