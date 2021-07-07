package com.example.final_mobile_project;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_mobile_project.Model.Users;
import com.example.final_mobile_project.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {
        ActivitySignInBinding binding;
        ProgressDialog progressDialog;
        FirebaseAuth auth;
        GoogleSignInClient mGoogleSignInClient;
        FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Login to your account");

        //Init
        InitializeGoogle();
        InitializeSignInBtn();
        InitializeSignUpBtn();
        updateUI(auth.getCurrentUser());
    }


    private void InitializeSignUpBtn(){
        binding.tvClickSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
    private void InitializeSignInBtn(){
        binding.btnSignIn.setOnClickListener(v -> {
            progressDialog.show();
            auth.signInWithEmailAndPassword(binding.etEmail2.getText().toString(), binding.etPassword2.getText().toString()).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(SignInActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private final int RC_SIGN_IN = 65;
    private void signInGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        someIntentActivityResultLauncher.launch(signInIntent);
    }
    private void InitializeGoogle(){
        binding.btnGoogle.setOnClickListener(v -> signInGoogle());
    }

    ActivityResultLauncher<Intent> someIntentActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (Exception e) {
                        // Google Sign In failed, update UI appropriately
                        Log.w("TAG", "Google sign in failed"+ e.getMessage());
                    }
                }
            });

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithCredential:success");
                        FirebaseUser user = auth.getCurrentUser();
                        Users users = new Users();
                        users.setUserId(user.getUid());
                        users.setUserName(user.getDisplayName());
                        users.setProfilePic(user.getPhotoUrl().toString());
                        database.getReference().child("Users").child(user.getUid()).setValue(users);
                        updateUI(user);
                        Toast.makeText(SignInActivity.this,"Sign in Google", Toast.LENGTH_SHORT).show();
                      //  updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithCredential:failure", task.getException());
                        Toast.makeText(SignInActivity.this, task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        Snackbar.make(binding.getRoot(),"Authentication Failure",Snackbar.LENGTH_SHORT).show();
                       // updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user){
        if (user!=null){
            Log.d("user", "updateUI: " +user.getUid());
            startActivity(new Intent(SignInActivity.this,MainActivity.class));
            finish();
        }
    }
}