package com.mobicom.pinballclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.pinball.PinballRequest;
import com.android.pinball.Response;
import com.mobicom.pinballclientlib.ByteRequest;
import com.mobicom.pinballclientlib.PinballFrameworkClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class JsonRequestFragment extends Fragment {

    public static final int INDEX = 1;
    private ListView lvJson;
    private ArrayAdapter<String> arrayAdapter = null;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.json_request_fragment, container, false);

        lvJson = (ListView) rootView.findViewById(R.id.json_request_listview);

        final Handler mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                lvJson.setAdapter(arrayAdapter);
            }
        };
        final PinballFrameworkClient pfc = PinballFrameworkClient.getInstance(getActivity().getApplicationContext());
        pfc.putRequest(new ByteRequest(Constants.JSON_DEFAULT_URL, PinballRequest.ACTIVE, "New Json Request"), new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] response) {
                try {
                    JSONArray weatherDataArray = new JSONObject(new String(response, "GB2312")).getJSONArray("results").getJSONObject(0).getJSONArray("weather_data");
                    arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, parseJsonData(weatherDataArray));
                    Message msg = new Message();
                    mHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        return rootView;
    }

    private List<String> parseJsonData(JSONArray weatherDataArray) throws JSONException {
        List<String> list = new ArrayList<String>();
        JSONObject everyDayData;
        for(int i = 1; i < weatherDataArray.length(); i++){
            everyDayData = weatherDataArray.getJSONObject(i);
            list.add(everyDayData.getString("date") + "\n" + everyDayData.getString("weather") + "\n" + everyDayData.getString("temperature"));
            Log.i("parseJsonDataResult",everyDayData.getString("date") + " " + everyDayData.getString("weather") + " " + everyDayData.getString("temperature"));
        }
        return list;
    }
}


