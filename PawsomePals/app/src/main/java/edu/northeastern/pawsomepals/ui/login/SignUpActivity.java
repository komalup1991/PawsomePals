package edu.northeastern.pawsomepals.ui.login;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.ui.profile.NewUserProfileActivity;


public class SignUpActivity extends AppCompatActivity {

    private TextView alreadyExistingAccount;
    private EditText inputEmail, inputPassword, inputConfirmPassword;
    private Button btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private int emailCursorPosition, passwordCursorPosition, confirmPasswordCursorPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        alreadyExistingAccount = findViewById(R.id.alreadyExistingAccount);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        progressBar.setVisibility(View.GONE);

        alreadyExistingAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAuthentication();
            }
        });

    }

    private void performAuthentication() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (!email.matches(emailPattern) || TextUtils.isEmpty(email)) {
            inputEmail.setError("Enter valid email.");
        } else if (TextUtils.isEmpty(password) || password.length() < 6) {
            inputPassword.setError("Enter valid password.");
        } else if (!password.equals(confirmPassword)) {
            inputPassword.setError("Password mismatch.");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = auth.getCurrentUser();
                        if (currentUser != null) {
                            String loggedInUserId = currentUser.getUid();
                            createUserDetails(email, loggedInUserId);
                        }

                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(SignUpActivity.this, R.string.signup_successful_registration, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(SignUpActivity.this, NewUserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, R.string.signup_unsuccessful_registration, Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void createUserDetails(String email, String loggedInUserId) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", loggedInUserId);
        userInfo.put("email", email);

        firebaseFirestore.collection("user")
                .document(loggedInUserId)
                .set(userInfo)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.w("Create User", "Error adding document", e);
                });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String email = inputEmail.getText().toString();
        outState.putString("email", email);
        emailCursorPosition = inputEmail.getSelectionStart();
        outState.putInt("emailCursorPosition", emailCursorPosition);

        String password = inputPassword.getText().toString();
        outState.putString("password", password);
        passwordCursorPosition = inputPassword.getSelectionStart();
        outState.putInt("passwordCursorPosition", passwordCursorPosition);

        String confirmPassword = inputConfirmPassword.getText().toString();
        outState.putString("confirmPassword", confirmPassword);
        confirmPasswordCursorPosition = inputConfirmPassword.getSelectionStart();
        outState.putInt("confirmPasswordCursorPosition", confirmPasswordCursorPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            String email = savedInstanceState.getString("email");
            inputEmail.setText(email);
            emailCursorPosition = savedInstanceState.getInt("emailCursorPosition");
            EditText editEmailText = inputEmail;
            editEmailText.setSelection(emailCursorPosition);

            String password = savedInstanceState.getString("password");
            inputPassword.setText(password);
            passwordCursorPosition = savedInstanceState.getInt("passwordCursorPosition");
            EditText editPasswordText = inputPassword;
            editPasswordText.setSelection(passwordCursorPosition);

            String confirmPassword = savedInstanceState.getString("confirmPassword");
            inputConfirmPassword.setText(confirmPassword);
            confirmPasswordCursorPosition = savedInstanceState.getInt("confirmPasswordCursorPosition");
            EditText editConfirmPasswordText = inputConfirmPassword;
            editConfirmPasswordText.setSelection(confirmPasswordCursorPosition);
        }
    }

}
