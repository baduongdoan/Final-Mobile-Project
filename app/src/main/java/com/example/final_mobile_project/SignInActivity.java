package com.example.final_mobile_project;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_mobile_project.Model.Users;
import com.example.final_mobile_project.databinding.ActivitySignInBinding;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {

        ActivitySignInBinding binding;
        ProgressDialog progressDialog;
        FirebaseAuth auth;
        GoogleSignInClient mGoogleSignInClient;
        FirebaseDatabase database;
        private CallbackManager mCallbackManager;

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
        InitializeFacebook();
        updateUI(auth.getCurrentUser());

    }


    private void InitializeFacebook(){
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                        Log.d("fb", "onSuccess: "+ loginResult.getAccessToken().toString());
                    }

                    @Override
                    public void onCancel() {
                        Log.d("fb", "onCancel: ");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("fb", "onError: " + error.getMessage());
                    }
                });
        binding.btnFacebook.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(SignInActivity.this, Arrays.asList("email","public_profile"));
        });

    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("FBlogin", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("FBlogin", "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FBlogin", "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
                        updateDatabase(user);
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
    private void updateDatabase(FirebaseUser user){
        Users users = new Users();
        users.setUserId(user.getUid());
        users.setUserName(user.getDisplayName());
        users.setProfilePic(user.getPhotoUrl().toString());
        database.getReference().child("Users").child(user.getUid()).setValue(users);
    }
    private void updateUI(FirebaseUser user){
        if (user!=null){
            Log.d("user", "updateUI: " +user.getUid());
            startActivity(new Intent(SignInActivity.this,MainActivity.class));
            finish();
        }
    }
}