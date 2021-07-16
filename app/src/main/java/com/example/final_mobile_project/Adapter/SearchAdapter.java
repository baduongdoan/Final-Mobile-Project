package com.example.final_mobile_project.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.final_mobile_project.Fragment.ProfileFragment;
import com.example.final_mobile_project.Model.Users;
import com.example.final_mobile_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Users> mUser;

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public SearchAdapter(Context mContext, List<Users> mUser) {
        this.mContext = mContext;
        this.mUser = mUser;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.user_item_search, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SearchAdapter.ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Users user = mUser.get(position);
        holder.btn_follow.setVisibility(View.VISIBLE);
        holder.username.setText(user.getUserName());
        Glide.with(mContext).load(user.getProfilePic()).into(holder.img_profile);

        isFollowing(user.getUserId(), holder.btn_follow);


        holder.itemView.setOnClickListener(v -> {
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
            editor.putString("profileid", user.getUserId());
            editor.apply();

            ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(
                    R.id.fragment_container, new ProfileFragment()).commit();
        });
        holder.btn_follow.setOnClickListener(v -> {
            if (holder.btn_follow.getText().toString().equals("follow")) {
                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                        .child("following").child(user.getUserId()).setValue(true);
                FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUserId())
                        .child("followers").child(user.getUserId()).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                        .child("following").child(user.getUserId()).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUserId())
                        .child("followers").child(user.getUserId()).removeValue();
            }
        });

            if (user.getUserId().equals(firebaseUser.getUid())) {
                holder.btn_follow.setVisibility(View.GONE);
                holder.username.setText(user.getUserName()+" (you) ");
            }

    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public CircleImageView img_profile;
        public Button btn_follow;


        public ViewHolder(@NonNull @NotNull View itemView) {

            super(itemView);
            username = itemView.findViewById(R.id.full_name);
            img_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);

        }
    }

    public void isFollowing(final String userId, Button button) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").
                child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(snapshot.child(userId).exists()){
                        button.setText("following");
                    }else{
                        button.setText("follow");
                    }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}
