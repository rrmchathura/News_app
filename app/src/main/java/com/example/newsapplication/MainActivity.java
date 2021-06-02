package com.example.newsapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{
    private Toolbar mainToolbar;
    private CircleImageView toolbarProfileImage;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Adapter adapter;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference;
    FirebaseUser currentUser;

    String userID;

    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* adding the toolbar to the main activity */
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("NEWS");


        viewPager = (ViewPager) findViewById(R.id.main_viewpager);
        adapter = new Adapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);


        tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();


        toolbarProfileImage = (CircleImageView) findViewById(R.id.toolbar_profile_image);
        toolbarProfileImage.setVisibility(View.GONE);
        toolbarProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /* checking whether the user has an account or not */
                if(currentUser == null)
                {
                    OpenCreateAccountDialog();
                }
                else
                {
                    SendUserProfilePage();
                }
            }
        });


    }



    @Override
    public void onResume()
    {
        super.onResume();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
    }


    /* popup create account dialog */
    private void OpenCreateAccountDialog()
    {
        final Dialog createAccountDialog = new Dialog(this);
        createAccountDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        createAccountDialog.setContentView(R.layout.create_account_dialog_layout);
        createAccountDialog.setTitle("Create Account Window");
        createAccountDialog.show();
        createAccountDialog.setCanceledOnTouchOutside(false);
        Window createAccountWindow = createAccountDialog.getWindow();
        createAccountWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final EditText username = (EditText) createAccountDialog.findViewById(R.id.create_account_dialog_username);
        final EditText email = (EditText) createAccountDialog.findViewById(R.id.create_account_dialog_email);
        final EditText password = (EditText) createAccountDialog.findViewById(R.id.create_account_dialog_password);
        final EditText confirmPassword = (EditText) createAccountDialog.findViewById(R.id.create_account_dialog_confirm_password);

        final TextView errorMsg = (TextView) createAccountDialog.findViewById(R.id.create_account_dialog_error_msg);

        /* create account button click action */
        Button createAccountBtn = (Button) createAccountDialog.findViewById(R.id.create_account_dialog_create_account_button);
        createAccountBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(TextUtils.isEmpty(username.getText().toString()) || TextUtils.isEmpty(username.getText().toString()) ||
                        TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(password.getText().toString()) ||
                TextUtils.isEmpty(confirmPassword.getText().toString()))
                {
                    errorMsg.setText("All fields are required.");
                    errorMsg.setVisibility(View.VISIBLE);
                }
                else if(!(password.getText().toString()).equals((confirmPassword.getText().toString())))
                {
                    errorMsg.setText("Passwords do not match.");
                    errorMsg.setVisibility(View.VISIBLE);
                }
                else
                {
                    errorMsg.setVisibility(View.GONE);

                    /* adding Loading bar */
                    loadingbar = new ProgressDialog(MainActivity.this);
                    String ProgressDialogMessage="Creating Account...";
                    SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
                    spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
                    loadingbar.setMessage(spannableMessage);
                    loadingbar.show();
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.setCancelable(false);

                    firebaseAuth.createUserWithEmailAndPassword((email.getText().toString()), (password.getText().toString())).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                /* adding other details to database */
                                userID = firebaseAuth.getCurrentUser().getUid();
                                userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);

                                HashMap userMap = new HashMap();
                                userMap.put("username", (username.getText().toString()));
                                userMap.put("email", (email.getText().toString()));

                               userDatabaseReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            loadingbar.dismiss();
                                            createAccountDialog.cancel();
                                            SendUserProfilePage();
                                        }
                                        else
                                        {
                                            password.setText("");
                                            confirmPassword.setText("");

                                            loadingbar.dismiss();

                                            errorMsg.setText(task.getException().getMessage());
                                            errorMsg.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }
                            else
                            {
                                password.setText("");
                                confirmPassword.setText("");

                                loadingbar.dismiss();

                                errorMsg.setText(task.getException().getMessage());
                                errorMsg.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });

        /* login button action */
        Button loginBtn = (Button) createAccountDialog.findViewById(R.id.create_account_dialog_login_button);
        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createAccountDialog.cancel();
                OpenLoginDialog();
            }
        });

        /* cancel button action */
        Button cancelBtn = (Button) createAccountDialog.findViewById(R.id.create_account_dialog_cancel_button);
        cancelBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createAccountDialog.cancel();
            }
        });
    }



    /* popup login dialog */
    private void OpenLoginDialog()
    {
        final Dialog loginDialog = new Dialog(this);
        loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loginDialog.setContentView(R.layout.login_dialog_layout);
        loginDialog.setTitle("Login Window");
        loginDialog.show();
        loginDialog.setCanceledOnTouchOutside(false);
        Window loginWindow = loginDialog.getWindow();
        loginWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final EditText email = (EditText) loginDialog.findViewById(R.id.login_dialog_email);
        final EditText password = (EditText) loginDialog.findViewById(R.id.login_dialog_password);

        final TextView errorMsg = (TextView) loginDialog.findViewById(R.id.login_dialog_error_msg);


        /* login button click actions */
        Button loginBtn = (Button) loginDialog.findViewById(R.id.login_dialog_login_button);
        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(password.getText().toString()))
                {
                    errorMsg.setText("All fields are required.");
                    errorMsg.setVisibility(View.VISIBLE);
                }
                else
                {
                    errorMsg.setVisibility(View.GONE);

                    /* adding Loading bar */
                    loadingbar = new ProgressDialog(MainActivity.this);
                    String ProgressDialogMessage="Logging...";
                    SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
                    spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
                    loadingbar.setMessage(spannableMessage);
                    loadingbar.show();
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.setCancelable(false);

                    firebaseAuth.signInWithEmailAndPassword((email.getText().toString()), (password.getText().toString())).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                loadingbar.dismiss();
                                loginDialog.cancel();
                                SendUserProfilePage();
                            }
                            else
                            {
                                password.setText("");

                                loadingbar.dismiss();

                                errorMsg.setText(task.getException().getMessage());
                                errorMsg.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });

        Button createAccountBtn = (Button) loginDialog.findViewById(R.id.login_dialog_create_account_button);
        createAccountBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                loginDialog.cancel();
                OpenCreateAccountDialog();
            }
        });

        Button cancelBtn = (Button) loginDialog.findViewById(R.id.login_dialog_cancel_button);
        cancelBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                loginDialog.cancel();
            }
        });
    }

    /* redirecting to the profile page */
    private void SendUserProfilePage()
    {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }


}
