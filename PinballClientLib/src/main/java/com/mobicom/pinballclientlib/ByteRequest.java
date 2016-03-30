package com.mobicom.pinballclientlib;

import com.android.pinball.PinballRequest;

public class ByteRequest extends PinballRequest{

    String byteRequestUrl;

    public ByteRequest(String url, int delay, String tag) {
        super(url, delay, tag);
        byteRequestUrl = url;
    }
}
