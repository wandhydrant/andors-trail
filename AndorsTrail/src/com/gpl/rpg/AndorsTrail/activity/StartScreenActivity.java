package com.gpl.rpg.AndorsTrail.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.activity.fragment.StartScreenActivity_MainMenu;
import com.gpl.rpg.AndorsTrail.activity.fragment.StartScreenActivity_MainMenu.OnNewGameRequestedListener;
import com.gpl.rpg.AndorsTrail.activity.fragment.StartScreenActivity_NewGame;
import com.gpl.rpg.AndorsTrail.activity.fragment.StartScreenActivity_NewGame.GameCreationOverListener;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileManager;

public final class StartScreenActivity extends FragmentActivity implements OnNewGameRequestedListener, GameCreationOverListener {


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this);
		final Resources res = getResources();
		TileManager tileManager = app.getWorld().tileManager;
		tileManager.setDensity(res);
		app.setWindowParameters(this);

		setContentView(R.layout.startscreen);

		if (findViewById(R.id.startscreen_fragment_container) != null) {
			StartScreenActivity_MainMenu mainMenu = new StartScreenActivity_MainMenu();
			
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.startscreen_fragment_container, mainMenu)
				.commit();
			
		}
		
		
		
		TextView tv = (TextView) findViewById(R.id.startscreen_version);
		tv.setText('v' + AndorsTrailApplication.CURRENT_VERSION_DISPLAY);
		
		TextView development_version = (TextView) findViewById(R.id.startscreen_dev_version);
		if (AndorsTrailApplication.DEVELOPMENT_INCOMPATIBLE_SAVEGAMES) {
			development_version.setText(R.string.startscreen_incompatible_savegames);
			development_version.setVisibility(View.VISIBLE);
		} else if (!AndorsTrailApplication.IS_RELEASE_VERSION) {
			development_version.setText(R.string.startscreen_non_release_version);
			development_version.setVisibility(View.VISIBLE);
		}
//		if (development_version.getVisibility() == View.VISIBLE) {
//			development_version.setText(development_version.getText() +
//					"\nMax Heap: " + Runtime.getRuntime().maxMemory() / 1024 +
//					"\nTile size: " + (int) (32 * res.getDisplayMetrics().density));
//		}

		app.getWorldSetup().startResourceLoader(res);

	}

	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		((AnimationDrawable)((ImageView)findViewById(R.id.title_logo)).getDrawable()).start();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
				backPressed();
				return true;
			} else {
				return super.onKeyDown(keyCode, event);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void backPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		}
	}
	
	public void onNewGameRequested() {
		if (findViewById(R.id.startscreen_fragment_container) != null) {
			StartScreenActivity_NewGame newGameFragment = new StartScreenActivity_NewGame();
			
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.startscreen_fragment_container, newGameFragment)
				.addToBackStack(null)
				.commit();
			
		}
	}
	
	@Override
	public void onGameCreationCancelled() {
		backPressed();
	}
	
}
