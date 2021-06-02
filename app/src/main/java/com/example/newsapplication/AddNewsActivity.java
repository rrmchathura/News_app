package com.example.newsapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddNewsActivity extends AppCompatActivity
{
    private Toolbar mainToolbar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference, newsDatabaseReference;

    private RadioButton verifiedBtn, fakeBtn;
    private EditText headline, description;
    private Button submitBtn;
    private TextView error;

    private ProgressDialog loadingBar;

    String currentUserID, strHeadline="", strDescription="", strNewsType="";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);

        /* adding the toolbar to the profile activity */
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Add News");

        /* toolbar back button */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        newsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("News");


        verifiedBtn = findViewById(R.id.add_news_verified_button);
        fakeBtn = findViewById(R.id.add_news_fake_button);
        headline = findViewById(R.id.add_news_headline_input);
        description = findViewById(R.id.add_news_description_input);
        error = findViewById(R.id.add_news_error);
        error.setVisibility(View.GONE);
        submitBtn = findViewById(R.id.add_news_submit_button);


        verifiedBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fakeBtn.setChecked(false);
                strNewsType = "verified";
            }
        });


        fakeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                verifiedBtn.setChecked(false);
                strNewsType = "fake";
            }
        });


        submitBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                error.setVisibility(View.GONE);
                AddNews();
            }
        });
    }


    private void AddNews()
    {
        strHeadline = headline.getText().toString();
        strDescription = description.getText().toString();

        if(strNewsType.isEmpty())
        {
            error.setText("Please select the news type.");
            error.setVisibility(View.VISIBLE);
        }
        else if(strHeadline.isEmpty())
        {
            error.setText("Please enter the headline.");
            error.setVisibility(View.VISIBLE);
        }
        else if(strDescription.isEmpty())
        {
            error.setText("Please enter the description.");
            error.setVisibility(View.VISIBLE);
        }
        else
        {
            /* adding Loading bar for 1000ms */
            loadingBar = new ProgressDialog(AddNewsActivity.this);
            String ProgressDialogMessage="Submitting...";
            SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
            spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
            loadingBar.setMessage(spannableMessage);
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.setCancelable(false);

            /* getting current date and time to create news ID */
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
            String date = simpleDateFormat1.format(calendar.getTime());
            SimpleDateFormat simpleTimeFormat1 = new SimpleDateFormat("HHmmss");
            String time = simpleTimeFormat1.format(calendar.getTime());
            String randomNewsID = date + time + currentUserID;


            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM yyyy");
            String newsPublishDate = simpleDateFormat2.format(calendar.getTime());
            SimpleDateFormat simpleTimeFormat2 = new SimpleDateFormat("hh:mm aa");
            String newsPublishTime = simpleTimeFormat2.format(calendar.getTime());

            HashMap newsMap = new HashMap();
            newsMap.put("headline",strHeadline);
            newsMap.put("description",strDescription);
            newsMap.put("newsType",strNewsType);
            newsMap.put("date",newsPublishDate);
            newsMap.put("time",newsPublishTime);

            newsDatabaseReference.child(randomNewsID).updateChildren(newsMap).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        loadingBar.cancel();
                        UserSendToMainPage();
                    }
                    else
                    {
                        String msg = task.getException().getMessage();
                        Toast.makeText(AddNewsActivity.this, "Error : "+msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void UserSendToMainPage()
    {
        Intent mainIntent = new Intent(AddNewsActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }


    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}
