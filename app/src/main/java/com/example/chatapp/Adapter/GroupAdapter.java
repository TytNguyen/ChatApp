package com.example.chatapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.GroupChatActivity;
import com.example.chatapp.Model.Group;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private Context mContext;
    private List<Group> mGroups;
    private List<String> member;
    private DatabaseReference reference;
    FirebaseUser fuser;

    public GroupAdapter (Context mContext, List<Group> mGroups) {
        this.mContext = mContext;
        this.mGroups = mGroups;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_item, parent, false);
        return new GroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupAdapter.ViewHolder holder, int position) {
        member = new ArrayList<>();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        final Group group = mGroups.get(position);
        holder.groupname.setText(group.getName());
        holder.introduction.setText(group.getDescription());

        Glide.with(mContext).load(group.getImage()).into(holder.group_image);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, GroupChatActivity.class);
                intent.putExtra("groupName", group.getName());

                if (check(group.getName())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Notification");
                    builder.setMessage("Group is full. Let's try for another group!");
                    builder.setCancelable(true);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    reference.child("GroupList").child(group.getName()).child(fuser.getUid()).setValue(fuser.getUid());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView groupname, introduction;
        public ImageView group_image;

        public ViewHolder (View itemView) {
            super(itemView);

            groupname = itemView.findViewById(R.id.groupname);
            group_image = itemView.findViewById(R.id.group_image);
            introduction = itemView.findViewById(R.id.introduction);
        }
    }

    private boolean check (String groupname) {
        reference.child("GroupList").child(groupname).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                member.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String chatlist = (String) snapshot.getValue();
                    member.add(chatlist);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (member.size() > 10) {
            return true;
        } else return false;
    }
}
