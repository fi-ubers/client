package com.example.Principal;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


import java.util.Arrays;

/**
 * A {@link Fragment} for containing Facebook login button. It handles
 * logging in with a Facebook account.
 */
public class LoginFragment extends Fragment {

    private LoginButton fbLoginBtn;
    private CallbackManager callbackManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        callbackManager = CallbackManager.Factory.create();
        fbLoginBtn = (LoginButton) view.findViewById(R.id.fbLoginBtn);
        fbLoginBtn.setReadPermissions(Arrays.asList("email", "user_friends", "user_birthday", "public_profile"));
        fbLoginBtn.setFragment(this);

        // Facebook login button
        fbLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                goMainScreen();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getActivity().getApplicationContext(), "Log in cancelled" , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("Fiuber LoginFragment", "error:", error);
                Toast.makeText(getActivity().getApplicationContext(), "Error in log in!" , Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    /**
     * Private method for goint to MainActivity.
     */
    private void goMainScreen(){
        ActivityChanger.getInstance().gotoMenuScreen(this.getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
