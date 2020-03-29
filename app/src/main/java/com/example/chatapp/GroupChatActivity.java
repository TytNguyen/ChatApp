package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.chatapp.Adapter.GroupMesAdapter;
import com.example.chatapp.Model.GroupChat;
import com.example.chatapp.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity {
    FirebaseUser fuser;
    DatabaseReference reference;
    Intent intent;
    ImageButton btn_send, btn_files;
    EditText text_send;
    String groupName, imageurl = "", checker = "", myUrl = "";
    RecyclerView recyclerView;
    List<GroupChat> mChat;
    GroupMesAdapter groupMesAdapter;
    ValueEventListener seenListener;
    ProgressDialog loadingBar;
    Uri fileUri;
    StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        btn_files = findViewById(R.id.btn_files);
        loadingBar = new ProgressDialog(this);

        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setHasFixedSize(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        intent = getIntent();
        groupName = intent.getStringExtra("groupName");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(groupName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GroupChatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                imageurl = user.getImageURL();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(fuser.getUid(), imageurl);
            }
        });
        readMessages();
        seenMessage();

        btn_files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "MS Word Files"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        }
                        if (i == 1) {
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);
                        }
                        if (i == 2) {
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select Ms Word File"), 438);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, we are sending that file...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if(!checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference("Document Files");

                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference userRef = reference.child("ChatGroups").child(groupName).push();

                final String mesId = userRef.getKey();

                final StorageReference filePath = storageReference.child(mesId + "." + checker);
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();

                                Map hashMap = new HashMap<>();
                                hashMap.put("imageUrl", imageurl);
                                hashMap.put("sender", fuser.getUid());
                                hashMap.put("message", downloadUrl);
                                hashMap.put("name", fileUri.getLastPathSegment());
                                hashMap.put("messageId", mesId);
                                hashMap.put("isseen", false);
                                hashMap.put("type", checker);

//                                hashMap.put("time", currentTime);
//                                hashMap.put("date", currentDate);

                                reference.child("ChatGroups").child(groupName).child(mesId).updateChildren(hashMap);
                                loadingBar.dismiss();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0* taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading...");
                    }
                });

            }
            else if(checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference("Image Files");

                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference userRef = reference.child("ChatGroups").child(groupName).push();

                final String mesId = userRef.getKey();

                final StorageReference filePath = storageReference.child(mesId + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map hashMap = new HashMap<>();

//                            hashMap.put("imageUrl", "default");
                            hashMap.put("imageUrl", imageurl);
                            hashMap.put("sender", fuser.getUid());
                            hashMap.put("message", myUrl);
                            hashMap.put("name", fileUri.getLastPathSegment());
                            hashMap.put("messageId", mesId);
                            hashMap.put("isseen", false);
                            hashMap.put("type", checker);


//                            hashMap.put("time", currentTime);
//                            hashMap.put("date", currentDate);
                            reference.child("ChatGroups").child(groupName).child(mesId).updateChildren(hashMap);
                            loadingBar.dismiss();
                        }
                    }
                });
            } else  {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing selected, Error.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void seenMessage() {
        reference = FirebaseDatabase.getInstance().getReference("ChatGroups").child(groupName);
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GroupChat groupChat = snapshot.getValue(GroupChat.class);
                    if (!groupChat.getSender().equals(fuser.getUid())) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("ChatGroups").child(groupName);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GroupChat groupChat = snapshot.getValue(GroupChat.class);
                    mChat.add(groupChat);
                    groupMesAdapter = new GroupMesAdapter(GroupChatActivity.this, mChat, groupName);
                    recyclerView.setAdapter(groupMesAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String uid, String imageurl) {
        String message = text_send.getText().toString();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("ChatGroups").child(groupName).push();
        String mesId = userRef.getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(GroupChatActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", uid);
            hashMap.put("message", message);
            hashMap.put("isseen", false);
            hashMap.put("imageUrl", imageurl);
            hashMap.put("type", "text");
            hashMap.put("messageId", mesId);

            reference.child("ChatGroups").child(groupName).child(mesId).updateChildren(hashMap);
            text_send.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GroupList");
        reference.child(groupName).child(fuser.getUid()).removeValue();
    }

    @Override
    protected void onStop() {
        super.onStop();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GroupList");
        reference.child(groupName).child(fuser.getUid()).removeValue();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GroupList");
        reference.child(groupName).child(fuser.getUid()).removeValue();
    }

    @Override
    protected void onStart() {
        super.onStart();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GroupList");
        reference.child(groupName).child(fuser.getUid()).setValue(fuser.getUid());
    }
}
