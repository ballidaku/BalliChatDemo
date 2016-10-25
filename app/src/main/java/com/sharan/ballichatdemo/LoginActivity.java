package com.sharan.ballichatdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener
{

    EditText edtv_username;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        context = this;

        edtv_username = (EditText) findViewById(R.id.edtv_username);


        findViewById(R.id.btn_login).setOnClickListener(this);

        if (!MySharedPreference.getInstance().getUSER_ID(context).isEmpty())
        {
            startActivity(new Intent(context, MainActivity.class));
            finish();
        }


    }


    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_login:

                Firebase.getInstance().createUser(this, edtv_username.getText().toString().trim());


                break;
        }
    }


    }







