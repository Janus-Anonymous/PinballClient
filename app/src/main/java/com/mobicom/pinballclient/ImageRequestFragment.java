package com.mobicom.pinballclient;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.pinball.PinballRequest;
import com.android.pinball.Response;
import com.mobicom.pinballclientlib.ByteRequest;
import com.mobicom.pinballclientlib.PinballFrameworkClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class ImageRequestFragment extends Fragment {
    public static final int INDEX = 2;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.image_request_fragment, container, false);
        final EditText editText = (EditText) rootView.findViewById(R.id.image_request_edittext);
        final Button button = (Button) rootView.findViewById(R.id.image_request_button);
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.image_request_imageview);
        final ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.image_request_progressbar);
        final Handler mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                progressBar.setVisibility(View.INVISIBLE);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray((byte[]) msg.obj, 0, ((byte[]) msg.obj).length));
            }
        };

        progressBar.setVisibility(View.INVISIBLE);
        editText.setText(Constants.IMAGE_DEFAULT_URL);

        final PinballFrameworkClient pfc = PinballFrameworkClient.getInstance(getActivity().getApplicationContext());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pfc.putRequest(new ByteRequest(editText.getText().toString(), PinballRequest.ACTIVE, "New Image Request"), new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        Message msg = new Message();
                        msg.obj = response;
                        mHandler.sendMessage(msg);
                    }
                });
                progressBar.setVisibility(View.VISIBLE);
            }
        });
        return rootView;
    }
}
