package com.example.final_mobile_project.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.final_mobile_project.Adapter.PhotoAdapter;
import com.example.final_mobile_project.MainActivity;
import com.example.final_mobile_project.Model.Post;
import com.example.final_mobile_project.Model.Users;
import com.example.final_mobile_project.R;
import com.example.final_mobile_project.SignInActivity;
import com.example.final_mobile_project.databinding.ActivityStartBinding;
import com.example.final_mobile_project.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {
    private CircleImageView imgProfile;

    private ImageView opt;
    private ImageView myPic;
    private ImageView savePic;

    private TextView followers;
    private TextView following;
    private TextView posts;
    private TextView fullname;
    private TextView bio;
    private TextView username_profile;

    private FirebaseUser fUser;
    String profileId;

    private Button editProfile;

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private List<Post> myPhotoList;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);



        fUser =FirebaseAuth.getInstance().getCurrentUser();

        String data= getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId", "none");
        if (data.equals("none")){
            profileId =fUser.getUid();
        }else {
            profileId= data;
        }

        imgProfile = view.findViewById(R.id.image_profile);
        opt = view.findViewById(R.id.options);
        myPic = view.findViewById(R.id.pic_profile);
        savePic = view.findViewById(R.id.save_pic_profile);
        followers = view.findViewById(R.id.follower_profile);
        following = view.findViewById(R.id.following_profile);
        posts = view.findViewById(R.id.posts_profile);
        fullname = view.findViewById(R.id.fullname_profile);
        bio = view.findViewById(R.id.bio_profile);
        username_profile = view.findViewById(R.id.username_profile);
        editProfile = view.findViewById(R.id.edit_profile);

        recyclerView = view.findViewById(R.id.recycler_view_pic);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        myPhotoList=  new ArrayList<>();
        photoAdapter = new PhotoAdapter(getContext(), myPhotoList);
        recyclerView.setAdapter(photoAdapter);


        userInfo();
        getFollowerAndFollowing();
        getPostsCount();
        myPhoto();

        if (profileId.equals(fUser.getUid())){
            editProfile.setText("Edit Profile");
        }else {
            checkFollowingStatus();
        }

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnText= editProfile.getText().toString();
                if(btnText.equals("Edit profile")){
                    //edit profile
                }else {
                    if(btnText.equals("follow")){
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("following")
                                .child(profileId).setValue(true);
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("followers")
                                .child(profileId).setValue(true);
                    }else {
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("following")
                                .child(profileId).removeValue();
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("followers")
                                .child(profileId).removeValue();
                    }
                }
            }
        });

        return view;
    }

    private void myPhoto() {

        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                myPhotoList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if(post.getPublisher().equals(profileId)){
                        myPhotoList.add(post);
                    }
                }
                Collections.reverse(myPhotoList);
                photoAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void checkFollowingStatus() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(profileId).exists()){
                    editProfile.setText("following");
                }else {
                    editProfile.setText("follow");
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getPostsCount() {
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datSnapshot) {
                int counter = 0;
                for (DataSnapshot snapshot :  datSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);

                    if(post.getPublisher().equals((profileId))) counter++;
                }

                posts.setText(String.valueOf(counter));
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getFollowerAndFollowing() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId);

        ref.child("follower").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datSnapshot) {
                followers.setText(""+ datSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        ref.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datSnapshot) {
                following.setText("" + datSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void userInfo() {

        FirebaseDatabase.getInstance().getReference().child("Users").child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datSnapshot) {
                Users user = datSnapshot.getValue(Users.class);
                Picasso.get().load(user.getProfilePic()).into(imgProfile);
                username_profile.setText(user.getUserName());
                fullname.setText(user.getUserName());
                bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}