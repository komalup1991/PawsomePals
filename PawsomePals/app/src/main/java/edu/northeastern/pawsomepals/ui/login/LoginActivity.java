package edu.northeastern.pawsomepals.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.northeastern.pawsomepals.R;

public class LoginActivity extends AppCompatActivity {

    private TextView createNewAccount;
    private EditText inputEmail, inputPassword;
    private Button btnLogin, btnGoogle, btnFacebook;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private int emailCursorPosition, passwordCursorPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        createNewAccount = findViewById(R.id.alreadyExistingAccount);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        progressBar = findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        progressBar.setVisibility(View.GONE);
        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, GoogleLoginActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (!email.matches(emailPattern) || TextUtils.isEmpty(email)) {
            inputEmail.setError("Enter valid email.");
        } else if (TextUtils.isEmpty(password) || password.length() < 6) {
            inputPassword.setError("Enter valid password.");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(LoginActivity.this, R.string.login_successful_login, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.login_unsuccessful_login, Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
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
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            String email = savedInstanceState.getString("email");
            inputEmail.setText(email);

            emailCursorPosition = savedInstanceState.getInt("emailCursorPosition");
            EditText editText = (EditText) inputEmail;
            editText.setSelection(emailCursorPosition);

            String password = savedInstanceState.getString("password");
            inputPassword.setText(password);

            passwordCursorPosition = savedInstanceState.getInt("passwordCursorPosition");
            EditText editPasswordText = (EditText) inputPassword;
            editPasswordText.setSelection(passwordCursorPosition);
        }
    }
}