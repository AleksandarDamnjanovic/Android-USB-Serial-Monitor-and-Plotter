package com.aleksandardamnjanovic.usbserialmonitorandplotter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;


public class Meta extends Fragment {
    View view;
    TextView metaTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_meta, container, false);
        metaTextView = view.findViewById(R.id.meta_textview);
        String text = getArguments().getString("text","There is no connected usb device!");
        metaTextView.setText(text);
        return  view;
    }

}