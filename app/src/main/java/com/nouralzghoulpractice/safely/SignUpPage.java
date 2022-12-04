package com.nouralzghoulpractice.safely;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SignUpPage extends AppCompatActivity {

    private Button button1;
    private Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
        getSupportActionBar().hide();

        button1= (Button) findViewById(R.id.buttonSignUpRider);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenRiderSignup();

            }
        });
        button2= (Button) findViewById(R.id.buttonSignUpDriver);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenDriverSignup();

            }
        });
    }
    public void OpenRiderSignup(){
        Intent intent1= new Intent(this,Sign_up_cust.class);
        startActivity(intent1);
    }

    public void OpenDriverSignup() {
        Intent intent2 = new Intent(this, Sign_up_driver.class);
        startActivity(intent2);
    }
}