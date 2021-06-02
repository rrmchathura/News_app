package com.example.newsapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    private Toolbar mainToolbar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference;

    private TextView username;
    private Button addNewsBtn, logoutBtn;

    private ProgressDialog loadingBar;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        /* adding the toolbar to the profile activity */
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Profile");

        /* toolbar back button */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        firebaseAuth= FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        username = findViewById(R.id.profile_username);
        addNewsBtn = findViewById(R.id.profile_add_news_button);
        logoutBtn = findViewById(R.id.profile_logout_button);


        addNewsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToAddNewsPage();
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LogoutTheUser();
            }
        });

        RetrieveUserDetails();
    }


    private void RetrieveUserDetails()
    {
        userDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("username"))
                    {
                        String strUsername = dataSnapshot.child("username").getValue().toString();
                        username.setText(strUsername);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void LogoutTheUser()
    {
        /* adding Loading bar for 1000ms */
        loadingBar = new ProgressDialog(ProfileActivity.this);
        String ProgressDialogMessage="Logging Out...";
        SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
        spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
        loadingBar.setMessage(spannableMessage);
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.setCancelable(false);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                loadingBar.dismiss();
                firebaseAuth.signOut();
                Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }
        },1000);
    }


    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }


    private void UserSendToAddNewsPage()
    {
        Intent addNewsIntent = new Intent(ProfileActivity.this, AddNewsActivity.class);
        startActivity(addNewsIntent);
    }

}
