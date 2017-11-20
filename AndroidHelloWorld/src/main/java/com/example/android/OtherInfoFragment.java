package com.example.android;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import java.util.Arrays;

/**
 * A {@link Fragment} for containing Facebook login button. It handles
 * logging in with a Facebook account.
 */
public class OtherInfoFragment extends Fragment {

    private ProfilePictureView otherProfilePicture;
    private Button otherBtnConfirm;
    private TextView otherUsrName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_otherinfo, container, false);

        // TODO: Change for otherInfo profile pic
        otherProfilePicture = (ProfilePictureView) view.findViewById(R.id.usProfilePicture);
        otherProfilePicture.setProfileId("107457569994960");

        otherBtnConfirm = (Button) view.findViewById(R.id.otherBtnConfirm);
        // TODO: Handle confirm click

        otherUsrName = (TextView) view.findViewById(R.id.otherUsrName);
        // TODO: Set otherUsrName text to otherInfo name

        return view;
    }


}
