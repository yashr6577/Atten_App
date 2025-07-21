package com.suyogbauskar.atten;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.suyogbauskar.atten.utils.ProgressDialog;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button googleSignInBtn;
    private final ProgressDialog progressDialog = new ProgressDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mAuth = FirebaseAuth.getInstance();
        googleSignInBtn = findViewById(R.id.signInBtn);

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        }

        googleSignInBtn.setOnClickListener(view -> signIn());
    }

    void signIn() {
        googleSignInBtn.setClickable(false);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        GoogleSignInClient gsc = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        progressDialog.show(MainActivity.this);

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount result = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(result);
            } catch (ApiException e) {
                progressDialog.hide();
                googleSignInBtn.setClickable(true);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressDialog.hide();
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                        if (isNewUser) {
                            startActivity(new Intent(MainActivity.this, FillDetailsActivity.class));
                        } else {
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        }
                    } else {
                        progressDialog.hide();
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}