package com.gpl.rpg.AndorsTrail.view;

import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.util.L;

public class CloudsAnimatorView extends FrameLayout {

	private static final float Y_MIN = 0f;
	private static final float Y_MAX = 0.6f;
	
	private static final int DEFAULT_DURATION = 30000;
	private static final float SPEED_VARIANCE = 0.2f;
	private static final float BELOW_SPEED_FACTOR = 0.5f;
	private static final float CENTER_SPEED_FACTOR = 1.0f;
	private static final float ABOVE_SPEED_FACTOR = 1.5f;

	private static final int BELOW_CLOUD_COUNT = 30;
	private static final int CENTER_CLOUD_COUNT = 20;
	private static final int ABOVE_CLOUD_COUNT = 15;
	
	private static final int[] belowDrawablesId = new int[]{R.drawable.ts_clouds_s_01, R.drawable.ts_clouds_s_02, R.drawable.ts_clouds_s_03};
	private static final int[] centerDrawablesId = new int[]{R.drawable.ts_clouds_m_01, R.drawable.ts_clouds_m_02};
	private static final int[] aboveDrawablesId = new int[]{R.drawable.ts_clouds_l_01, R.drawable.ts_clouds_l_02, R.drawable.ts_clouds_l_03, R.drawable.ts_clouds_l_04};
	
	ViewGroup belowLayer, centerLayer, aboveLayer;
	private int duration = DEFAULT_DURATION;
	
	private final ConcurrentHashMap<ImageView, PausableTranslateAnimation> animations = new ConcurrentHashMap<ImageView, PausableTranslateAnimation>(BELOW_CLOUD_COUNT + CENTER_CLOUD_COUNT + ABOVE_CLOUD_COUNT);


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
		setFocusable(false);
		inflate(getContext(), R.layout.clouds_animator, this);
		
		belowLayer = (ViewGroup) findViewById(R.id.ts_clouds_below);
		centerLayer = (ViewGroup) findViewById(R.id.ts_clouds_center);
		aboveLayer = (ViewGroup) findViewById(R.id.ts_clouds_above);
	}
	
	private void createCloudBelow() {
		createCloud(belowLayer, belowDrawablesId, BELOW_SPEED_FACTOR);
	}
	private void createCloudCenter() {
		createCloud(centerLayer, centerDrawablesId, CENTER_SPEED_FACTOR);
	}
	private void createCloudAbove() {
		createCloud(aboveLayer, aboveDrawablesId, ABOVE_SPEED_FACTOR);
	}
	
	
	private void createCloud(final ViewGroup layer, final int[] ids, final float speedFactor) {
		final ImageView iv = new ImageView(getContext());
		iv.setImageDrawable(getResources().getDrawable(ids[(int)(ids.length * Math.random())]));
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layer.addView(iv, lp);

		final float y = Y_MIN + (float) (layer.getHeight() * Math.random() * (Y_MAX - Y_MIN));
		float ratio = (float)Math.random();
		final float x = (float) (((1-ratio) * (iv.getWidth() + layer.getWidth())) - iv.getWidth());
		final long d = (long)((ratio * duration) / (speedFactor + (Math.random() * SPEED_VARIANCE)));
		

		prepareAnimation(iv, layer, speedFactor, x, y, d);
	}
	
	private void resetCloud(final ViewGroup layer, final float speedFactor, final ImageView iv) {
		final float y = Y_MIN + (float) (layer.getHeight() * Math.random() * (Y_MAX - Y_MIN));
		final float x = -iv.getWidth();
		final long d = (long)(duration / (speedFactor + (Math.random() * SPEED_VARIANCE)));
		
		prepareAnimation(iv, layer, speedFactor, x, y, d);
	}
	
	private void prepareAnimation(final ImageView iv, final ViewGroup layer, final float speedFactor, final float x, final float y, final long d) {
		PausableTranslateAnimation anim = new PausableTranslateAnimation(
				TranslateAnimation.ABSOLUTE, x, TranslateAnimation.ABSOLUTE, layer.getWidth(), 
				TranslateAnimation.ABSOLUTE, y, TranslateAnimation.ABSOLUTE, y);
		
		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				iv.setVisibility(View.VISIBLE);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationEnd(Animation animation) {
				iv.setVisibility(View.GONE);
				resetCloud(layer, speedFactor, iv);
			}
		});
		anim.setInterpolator(new LinearInterpolator());
		anim.setDuration(d);
		animations.put(iv, anim);
		iv.startAnimation(anim);
		if (!hasWindowFocus()) {
			anim.pause();
		}
	}

	public void startAnimation() {
		int i = BELOW_CLOUD_COUNT;
		while (i-- > 0) {
			createCloudBelow();
		}
		i = CENTER_CLOUD_COUNT;
		while (i-- > 0) {
			createCloudCenter();
		}
		i = ABOVE_CLOUD_COUNT;
		while (i-- > 0) {
			createCloudAbove();
		}
	}
	
	
	boolean started = false;
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		L.log("Clouds onWindowFocusChanged("+hasWindowFocus+")");
		if (hasWindowFocus) {
			if (!started) {
				duration = (int) (DEFAULT_DURATION * getWidth() / (1024 * getResources().getDisplayMetrics().density)); 
				startAnimation();
				started = true;
			} else {
				resumeAnimation();
			}
		} else {
			pauseAnimation();
		}
	}
	
	private void resumeAnimation() {
		for (PausableTranslateAnimation a : animations.values()) {
			a.resume();
		}
	}
	private void pauseAnimation() {
		for (PausableTranslateAnimation a : animations.values()) {
			a.pause();
		}
	}

	private static class PausableTranslateAnimation extends TranslateAnimation {
		
		private long elapsedAtPause = 0;
		private boolean paused = false;
		private boolean resume = false;
		
		public PausableTranslateAnimation(int fromXType, float fromXValue, int toXType, float toXValue,
	            int fromYType, float fromYValue, int toYType, float toYValue) {
			super(fromXType, fromXValue, toXType, toXValue, fromYType, fromYValue, toYType, toYValue);
		}
		
		@Override
		public boolean getTransformation(long currentTime, Transformation outTransformation) {
			if (paused && elapsedAtPause == 0) {
				elapsedAtPause = currentTime - getStartTime();
			}
			if (paused) {
				setStartTime(currentTime - elapsedAtPause); 
				if (resume) {
					paused = false;
					resume = false;
				}
			}
			return super.getTransformation(currentTime, outTransformation);
		}
		
		public void pause() {
			elapsedAtPause = 0;
			paused = true;
		}
		
		public void resume() {
			resume = true;
		}
	}
	
}
