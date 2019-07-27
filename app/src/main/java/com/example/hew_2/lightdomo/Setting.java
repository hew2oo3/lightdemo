package com.example.hew_2.lightdomo;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

/**
 * Created by hew_2 on 2018/6/23.
 */

public class Setting extends Fragment {
    public Setting(){
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.setting_fragment,container,false);
        return view;
    }
}
