package com.example.android;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
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
	private TextView tripDestination,tripOrigin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_otherinfo, container, false);
        otherProfilePicture = (ProfilePictureView) view.findViewById(R.id.otherProfilePicture);
        otherProfilePicture.setVisibility(View.INVISIBLE);

        otherBtnConfirm = (Button) view.findViewById(R.id.otherBtnConfirm);
		otherBtnConfirm.setVisibility(View.INVISIBLE);
		otherBtnConfirm.setEnabled(false);
		otherBtnConfirm.setHeight(0);

        otherUsrName = (TextView) view.findViewById(R.id.otherUsrName);
		tripDestination = (TextView) view.findViewById(R.id.tripDestination);
		tripOrigin = (TextView) view.findViewById(R.id.tripOrigin);

        return view;
    }


    public void updateFragmentElements(OtherUsersInfo uInfo, boolean enableBtn){
		String picString = uInfo.getPicture();
		if((picString != null) && (picString.length() > 0))
			otherProfilePicture.setProfileId(picString);
		else
			otherProfilePicture.setProfileId("107457569994960");
		// TODO: if picString.equals("-1") set default pic
		String nameString = uInfo.getName();
		if((nameString != null) && (nameString.length() > 0)) {
			// If the other is driver, we add the rate ;)
			double rate = uInfo.getDriverRate();
			if(rate > 0)
				nameString = nameString + " (" + rate + " stars)";
			otherUsrName.setText(nameString);
		}
		String origString = uInfo.getOrig();
		if((origString != null) && (origString.length() > 0))
			tripOrigin.setText(origString.split(",")[0]);
		String destString = uInfo.getDest();
		if((destString != null) && (destString.length() > 0))
			tripDestination.setText(destString.split(",")[0]);

		otherProfilePicture.setVisibility(View.VISIBLE);
		if(enableBtn) {
			otherBtnConfirm.setVisibility(View.VISIBLE);
			otherBtnConfirm.setEnabled(true);
			otherBtnConfirm.setHeight(36);
		}
		else
			((ViewManager)otherBtnConfirm.getParent()).removeView(otherBtnConfirm);
	}

}
