package com.example.final_mobile_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.final_mobile_project.Fragment.HomePageFragment;
import com.example.final_mobile_project.Fragment.NotificationFragment;
import com.example.final_mobile_project.Fragment.ProfileFragment;
import com.example.final_mobile_project.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomePageActivity extends AppCompatActivity {


    BottomNavigationView bottomNavigationView;
    Fragment selectFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);



        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(listener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomePageFragment()).commit();
    }

    NavigationBarView.OnItemSelectedListener listener = item -> {
        if (item.getItemId() == R.id.nav_home) {
            selectFragment = new HomePageFragment();
        } else if (item.getItemId() == R.id.nav_search) {
            selectFragment = new SearchFragment();
        } else if (item.getItemId() == R.id.nav_newPost) {
            startActivity(new Intent(HomePageActivity.this, PostActivity.class));
        } else if (item.getItemId() == R.id.nav_follow) {
            selectFragment = new NotificationFragment();;
        } else if (item.getItemId() == R.id.nav_profile) {
            SharedPreferences.Editor editor = getSharedPreferences("PREPS", MODE_PRIVATE).edit();
            editor.putString("profile", FirebaseAuth.getInstance().getCurrentUser().getUid());
            editor.apply();
            selectFragment = new ProfileFragment();
        }
        FragmentTransaction transaction =  getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectFragment);
        transaction.commit();
        return true;
    };
}