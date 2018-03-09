package com.gpl.rpg.AndorsTrail.view;

import java.util.List;

import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.util.L;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class CloudsAnimatorView extends FrameLayout {

	private static final int Y_MIN = 0;
	private static final int Y_MAX = 100;
	
	private static final int DURATION = 12000;
	private static final int SPEED_MIN = 10;
	private static final int SPEED_MAX = 15;

	private static final float BELOW_SPEED_FACTOR = 0.8f;
	private static final float CENTER_SPEED_FACTOR = 1.0f;
	private static final float ABOVE_SPEED_FACTOR = 1.2f;

	private static final int BELOW_CLOUD_COUNT = 30;
	private static final int CENTER_CLOUD_COUNT = 20;
	private static final int ABOVE_CLOUD_COUNT = 15;
	
	private static final int[] belowDrawablesId = new int[]{R.drawable.ts_clouds_s_01, R.drawable.ts_clouds_s_02, R.drawable.ts_clouds_s_03};
	private static final int[] centerDrawablesId = new int[]{R.drawable.ts_clouds_m_01, R.drawable.ts_clouds_m_02};
	private static final int[] aboveDrawablesId = new int[]{R.drawable.ts_clouds_l_01, R.drawable.ts_clouds_l_02, R.drawable.ts_clouds_l_03, R.drawable.ts_clouds_l_04};
	
	ViewGroup belowLayer, centerLayer, aboveLayer;
	View belowStart, centerStart, aboveStart;
	


	public CloudsAnimatorView(Context context) {
		super(context);
		init();
	}

	public CloudsAnimatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CloudsAnimatorView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	public void init() {
		L.log("Cloud animator created");
		setFocusable(false);
		inflate(getContext(), R.layout.clouds_animator, this);
		
		belowLayer = (ViewGroup) findViewById(R.id.ts_clouds_below);
		centerLayer = (ViewGroup) findViewById(R.id.ts_clouds_center);
		aboveLayer = (ViewGroup) findViewById(R.id.ts_clouds_above);
		
		belowStart = (ViewGroup) findViewById(R.id.ts_clouds_below_start);
		centerStart = (ViewGroup) findViewById(R.id.ts_clouds_center_start);
		aboveStart = (ViewGroup) findViewById(R.id.ts_clouds_above_start);
	}
	
	private void addCloudBelow() {
		if (belowLayer == null) {
			L.log("Cloud below is null. Deferring.");
			postDelayed(new Runnable() {
				@Override
				public void run() {addCloudBelow();}
			}, (int)(DURATION * Math.random()));
		} else {
			addCloud(belowLayer, R.id.ts_clouds_below_start, belowDrawablesId, BELOW_SPEED_FACTOR);
		}
		
	}
	private void addCloudCenter() {
		if (centerLayer == null) {
			L.log("Cloud center is null. Deferring.");
			postDelayed(new Runnable() {
				@Override
				public void run() {addCloudCenter();}
			}, (int)(DURATION * Math.random()));
		} else {
			addCloud(centerLayer, R.id.ts_clouds_center_start, centerDrawablesId, CENTER_SPEED_FACTOR);
		}
	}
	private void addCloudAbove() {
		if (aboveLayer == null) {
			L.log("Cloud above is null. Deferring.");
			postDelayed(new Runnable() {
				@Override
				public void run() {addCloudAbove();}
			}, (int)(DURATION * Math.random()));
		} else {
			addCloud(aboveLayer, R.id.ts_clouds_above_start, aboveDrawablesId, ABOVE_SPEED_FACTOR);
		}
	}
	
	
	private void addCloud(final ViewGroup layer, final int startId, final int[] ids, final float speedFactor) {
		final ImageView iv = new ImageView(getContext());
		iv.setImageDrawable(getResources().getDrawable(ids[(int)(ids.length * Math.random())]));
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		//lp.addRule(RelativeLayout.LEFT_OF, startId);
//		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//		lp.topMargin = (int) (layer.getHeight() * Math.random());
		final float y = (float) (layer.getHeight() * Math.random());
		L.log("Cloud added at y="+y);
		layer.addView(iv, lp);
		TranslateAnimation anim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, -1.0f, TranslateAnimation.RELATIVE_TO_PARENT, 2.0f, 
				TranslateAnimation.ABSOLUTE, y, TranslateAnimation.ABSOLUTE, y);
		anim.setAnimationListener(new Animation.AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				L.log("Cloud ended at y="+y);
				layer.removeView(iv);
				if (CloudsAnimatorView.this.getVisibility() == View.VISIBLE) {
					postDelayed(new Runnable() {
						@Override
						public void run() {addCloud(layer, startId, ids, speedFactor);}
					}, (int)(DURATION * Math.random()));
				}
			}
		});
		anim.setDuration((long)(DURATION / speedFactor));
		iv.startAnimation(anim);
	}

	/*@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (changedView == this && visibility == View.VISIBLE) {
			startAnimation();
		} else if (changedView == this) {
			stopAll();
		}
	}*/
	
	public void startAnimation() {
		L.log("Cloud animator started");
		int i = BELOW_CLOUD_COUNT;
		while (i-- > 0) {
			postDelayed(new Runnable() {
				@Override
				public void run() {addCloudBelow();}
			}, (int)(DURATION * Math.random()));
		}
		i = CENTER_CLOUD_COUNT;
		while (i-- > 0) {
			postDelayed(new Runnable() {
				@Override
				public void run() {addCloudCenter();}
			}, (int)(DURATION * Math.random()));
		}
		i = ABOVE_CLOUD_COUNT;
		while (i-- > 0) {
			postDelayed(new Runnable() {
				@Override
				public void run() {addCloudAbove();}
			}, (int)(DURATION * Math.random()));
		}
	}	
	
//	private void stopAll() {}

}
