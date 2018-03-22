package com.gpl.rpg.AndorsTrail.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.WorldSetup;
import com.gpl.rpg.AndorsTrail.WorldSetup.OnResourcesLoadedListener;
import com.gpl.rpg.AndorsTrail.WorldSetup.OnSceneLoadedListener;
import com.gpl.rpg.AndorsTrail.savegames.Savegames;
import com.gpl.rpg.AndorsTrail.util.L;
import com.gpl.rpg.AndorsTrail.util.ThemeHelper;
import com.gpl.rpg.AndorsTrail.view.CloudsAnimatorView;
import com.gpl.rpg.AndorsTrail.view.CustomDialogFactory;

public final class LoadingActivity extends Activity implements OnResourcesLoadedListener, OnSceneLoadedListener {

	private WorldSetup setup;
	private Dialog progressDialog;
	private CloudsAnimatorView clouds_back, clouds_mid, clouds_front;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		L.log("ANIM Activity Create");
		setTheme(ThemeHelper.getBaseTheme());
		super.onCreate(savedInstanceState);
		AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this);
		app.setWindowParameters(this);
		setContentView(R.layout.startscreen);
		
		clouds_back = (CloudsAnimatorView) findViewById(R.id.ts_clouds_animator_back);
		if (clouds_back != null) clouds_back.setCloudsCount(40, 0, 0);
		clouds_mid = (CloudsAnimatorView) findViewById(R.id.ts_clouds_animator_mid);
		if (clouds_mid != null) clouds_mid.setCloudsCount(0, 15, 0);
		clouds_front = (CloudsAnimatorView) findViewById(R.id.ts_clouds_animator_front);
		if (clouds_front != null) clouds_front.setCloudsCount(0, 0, 8);
		
		this.setup = app.getWorldSetup();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		L.log("ANIM Activity Focus "+hasFocus);
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			((AnimationDrawable)((ImageView)findViewById(R.id.title_logo)).getDrawable()).start();
			ImageView iv = (ImageView) findViewById(R.id.ts_foreground);
			int ivWidth = iv.getWidth();
			int drawableWidth = iv.getDrawable().getIntrinsicWidth();
			
			float ratio = ((float)ivWidth) / ((float)drawableWidth);
			if (clouds_back != null)clouds_back.setScalingRatio(ratio);
			if (clouds_mid != null)clouds_mid.setScalingRatio(ratio);
			if (clouds_front != null)clouds_front.setScalingRatio(ratio);
			
			if (progressDialog == null) {
				progressDialog = CustomDialogFactory.createDialog(this, getResources().getString(R.string.dialog_loading_message), 
						getResources().getDrawable(R.drawable.loading_anim), null, null, false, false);
				progressDialog.setOwnerActivity(this);
				CustomDialogFactory.show(progressDialog);
			}
		}
	}
	
	@Override
	public void onResume() {
		L.log("ANIM Activity Resume");
		super.onResume();
		
		setup.setOnResourcesLoadedListener(this);
		if (clouds_back != null)clouds_back.resumeAnimation();
		if (clouds_mid != null)clouds_mid.resumeAnimation();
		if (clouds_front != null)clouds_front.resumeAnimation();
	}

	@Override
	public void onPause() {
		L.log("ANIM Activity Pause");
		super.onPause();
		setup.setOnResourcesLoadedListener(null);
		setup.removeOnSceneLoadedListener(this);
		if (clouds_back != null)clouds_back.pauseAnimation();
		if (clouds_mid != null)clouds_mid.pauseAnimation();
		if (clouds_front != null)clouds_front.pauseAnimation();
	}
	
	
	@Override
	public void onResourcesLoaded() {
		setup.startCharacterSetup(this);
	}

	@Override
	public void onSceneLoaded() {
		if (progressDialog != null) progressDialog.dismiss();
		startActivity(new Intent(this, MainActivity.class));
		this.finish();
	}

	@Override
	public void onSceneLoadFailed(Savegames.LoadSavegameResult loadResult) {
		if (progressDialog != null) progressDialog.dismiss();
		if (loadResult == Savegames.LoadSavegameResult.savegameIsFromAFutureVersion) {
			showLoadingFailedDialog(R.string.dialog_loading_failed_incorrectversion);
		} else {
			showLoadingFailedDialog(R.string.dialog_loading_failed_message);
		}
	}

	private void showLoadingFailedDialog(int messageResourceID) {
//		Dialog d = new AlertDialog.Builder(this)
//			.setTitle(R.string.dialog_loading_failed_title)
//			.setMessage(messageResourceID)
//			.setNeutralButton(android.R.string.ok, null)
//			.create();
//		d.setOnDismissListener(new OnDismissListener() {
//			@Override
//			public void onDismiss(DialogInterface dialog) {
//				LoadingActivity.this.finish();
//			}
//		});
//		d.show();
		
		final Dialog d = CustomDialogFactory.createDialog(this, getResources().getString(R.string.dialog_loading_failed_title), null, getResources().getString(messageResourceID), null, true);
		CustomDialogFactory.addDismissButton(d, android.R.string.ok);
		CustomDialogFactory.setDismissListener(d, new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				LoadingActivity.this.finish();
			}
		});
		CustomDialogFactory.show(d);
		
	}
}
