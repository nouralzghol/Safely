package com.nouralzghoulpractice.safely;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button button1;
    private Button button2;
    private Button button3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        button1= (Button) findViewById(R.id.buttonLogInRider);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenRiderLogin();

            }
        });
        button2= (Button) findViewById(R.id.buttonLogInDriver);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenDriverLogin();

            }
        });
        button3= (Button) findViewById(R.id.buttonSignUpNow);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenSignupPage();

            }
        });

    }


    public void OpenRiderLogin(){
        Intent intent1= new Intent(this, Login_cust.class);
        startActivity(intent1);
    }
//public void OpenRiderLogin(){
//    Intent intent1= new Intent(this, AfterNextButton.class);
//    startActivity(intent1);
//}





    public void OpenDriverLogin(){
        Intent intent2= new Intent(this,Login_driver.class);
        startActivity(intent2);
    }

//    public void OpenDriverLogin(){
//        Intent intent2= new Intent(this,CustomerInfoFD.class);
//        startActivity(intent2);
//    }
    public void OpenSignupPage(){
        Intent intent3= new Intent(this,SignUpPage.class);
        startActivity(intent3);
    }


}