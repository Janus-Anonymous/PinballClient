package com.mobicom.pinballclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class SelectOptionsActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int frIndex = getIntent().getIntExtra(Constants.Extra.FRAGMENT_INDEX, 0);
        Fragment fr;
        String tag;
        int titleRes;
        switch (frIndex){
            default:
            case StringRequestFragment.INDEX:
                tag = StringRequestFragment.class.getSimpleName();
                fr = getSupportFragmentManager().findFragmentByTag(tag);
                if(fr == null){
                    fr = new StringRequestFragment();
                }
                titleRes = R.string.string_request_example;
                break;
            case JsonRequestFragment.INDEX:
                tag = JsonRequestFragment.class.getSimpleName();
                fr = getSupportFragmentManager().findFragmentByTag(tag);
                if(fr == null){
                    fr = new JsonRequestFragment();
                }
                titleRes = R.string.json_request_example;
                break;
            case ImageRequestFragment.INDEX:
                tag = ImageRequestFragment.class.getSimpleName();
                fr = getSupportFragmentManager().findFragmentByTag(tag);
                if(fr == null){
                    fr = new ImageRequestFragment();
                }
                titleRes = R.string.image_request_example;
                break;
        }
        setTitle(titleRes);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fr, tag).commit();
    }
}
