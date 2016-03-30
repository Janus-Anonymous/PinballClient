package com.mobicom.pinballclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.pinball.PinballRequest;
import com.android.pinball.Response;
import com.mobicom.pinballclientlib.ByteRequest;
import com.mobicom.pinballclientlib.PinballFrameworkClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStringRequestButtonClick(View view){
        Intent intent = new Intent(this, SelectOptionsActivity.class);
        intent.putExtra(Constants.Extra.FRAGMENT_INDEX, StringRequestFragment.INDEX);
        startActivity(intent);
    }

    public void onJsonRequestButtonClick(View view){
        Intent intent = new Intent(this, SelectOptionsActivity.class);
        intent.putExtra(Constants.Extra.FRAGMENT_INDEX, JsonRequestFragment.INDEX);
        startActivity(intent);
    }

    public void onImageRequestButtonClick(View view){
        Intent intent = new Intent(this, SelectOptionsActivity.class);
        intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImageRequestFragment.INDEX);
        startActivity(intent);
    }

}
