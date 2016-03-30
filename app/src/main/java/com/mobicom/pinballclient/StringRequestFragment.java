package com.mobicom.pinballclient;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.pinball.PinballRequest;
import com.android.pinball.Request;
import com.android.pinball.Response;
import com.mobicom.pinballclientlib.ByteRequest;
import com.mobicom.pinballclientlib.PinballFrameworkClient;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;

public class StringRequestFragment extends Fragment {

    public static final int INDEX = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.string_request_fragment, null);
        final EditText editText = (EditText) rootView.findViewById(R.id.string_request_text_input);
        final Button button = (Button) rootView.findViewById(R.id.send_string_request_button);
        final TextView textView = (TextView) rootView.findViewById(R.id.string_request_response);
        final PinballFrameworkClient pfc = PinballFrameworkClient.getInstance(getActivity().getApplicationContext());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pfc.putRequest(new ByteRequest(editText.getText().toString(), PinballRequest.ACTIVE, "New String Request"), new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        try {
                            textView.setText(new String(response, "GB2312"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        return rootView;
    }
}
