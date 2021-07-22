package com.example.final_mobile_project.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.final_mobile_project.R;
import com.example.final_mobile_project.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;


public class NotificationFragment extends Fragment {
    FirebaseAuth auth;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        auth = FirebaseAuth.getInstance();
        Button btn_SignOut1 = (Button) view.findViewById(R.id.btn_SignOut1);
        btn_SignOut1.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
        });
        return view;
    }
}