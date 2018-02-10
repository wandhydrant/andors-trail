package com.gpl.rpg.AndorsTrail.view;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.gpl.rpg.AndorsTrail.AndorsTrailPreferences;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.listeners.ActorConditionListener;
import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.actor.Actor;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileManager;

public final class DisplayActiveActorConditionIcons implements ActorConditionListener {

	private final AndorsTrailPreferences preferences;
	private final TileManager tileManager;
	private final ControllerContext controllers;
	private final WorldContext world;
	private final RelativeLayout activeConditions;
	private final ArrayList<ActiveConditionIcon> currentConditionIcons = new ArrayList<ActiveConditionIcon>();
	private final WeakReference<Context> androidContext;
	
	private Actor target;

	public DisplayActiveActorConditionIcons(
			final ControllerContext controllers,
			final WorldContext world,
			Context androidContext,
			RelativeLayout activeConditions) {
		this.controllers = controllers;
		this.world = world;
		this.preferences = controllers.preferences;
		this.tileManager = world.tileManager;
		this.androidContext = new WeakReference<Context>(androidContext);
		this.activeConditions = activeConditions;
	}
	
	public void setTarget(Actor target) {
		if (this.target == target) return;
		this.target = target;
		cleanUp();
	}

	@Override
	public void onActorConditionAdded(Actor actor, ActorCondition condition) {
		if (actor != target) return;
		ActiveConditionIcon icon = getFirstFreeIcon();
		icon.setActiveCondition(condition, false);
		icon.show();
	}

	@Override
	public void onActorConditionRemoved(Actor actor, ActorCondition condition) {
		if (actor != target) return;
		ActiveConditionIcon icon = getIconFor(condition, false);
		if (icon == null) return;
		icon.hide(true);
	}

	@Override
	public void onActorConditionDurationChanged(Actor actor, ActorCondition condition) {
	}

	@Override
	public void onActorConditionMagnitudeChanged(Actor actor, ActorCondition condition) {
		if (actor != target) return;
		ActiveConditionIcon icon = getIconFor(condition, false);
		if (icon == null) return;
		icon.setIconText();
	}

	@Override
	public void onActorConditionRoundEffectApplied(Actor actor, ActorCondition condition) {
		if (actor != target) return;
		ActiveConditionIcon icon = getIconFor(condition, false);
		if (icon == null) return;
		icon.pulseAnimate();
	}


	@Override
	public void onActorConditionImmunityAdded(Actor actor, ActorCondition condition) {
		if (actor != target) return;
		ActiveConditionIcon icon = getFirstFreeIcon();
		icon.setActiveCondition(condition, true);
		icon.show();
	}

	@Override
	public void onActorConditionImmunityRemoved(Actor actor, ActorCondition condition) {
		if (actor != target) return;
		ActiveConditionIcon icon = getIconFor(condition, true);
		if (icon == null) return;
		icon.hide(true);
	}

	@Override
	public void onActorConditionImmunityDurationChanged(Actor actor, ActorCondition condition) {
	}
	
	public void unsubscribe() {
		controllers.actorStatsController.actorConditionListeners.remove(this);
		for (ActiveConditionIcon icon : currentConditionIcons) icon.condition = null;
	}

	public void subscribe() {
		cleanUp();
		controllers.actorStatsController.actorConditionListeners.add(this);
	}
	
	private void cleanUp() {
		for (ActiveConditionIcon icon : currentConditionIcons) icon.hide(false);
		if (target != null) {
			for (ActorCondition condition : target.conditions) {
				getFirstFreeIcon().setActiveCondition(condition, false);
			}
			for (ActorCondition condition : target.immunities) {
				getFirstFreeIcon().setActiveCondition(condition, true);
			}
		}
	}

	private final class ActiveConditionIcon implements AnimationListener {
		public final int id;
		private boolean immunity = false;
		public ActorCondition condition;
		public final ImageView image;
		public final TextView text;
		private final Animation onNewIconAnimation;
		private final Animation onRemovedIconAnimation;
		private final Animation onAppliedEffectAnimation;
		final Resources res;
		
		public ActiveConditionIcon(Context context, int id) {
			this.id = id;
			this.image = new ImageView(context);
			this.image.setId(id);
			GradientDrawable grad = new GradientDrawable(target == world.model.player ? Orientation.BOTTOM_TOP : Orientation.TOP_BOTTOM, new int[]{Color.argb(190, 68, 68, 68), Color.argb(10, 34, 34, 34)});
			grad.setCornerRadius(3);
			this.image.setBackgroundDrawable(grad);
			this.text = new TextView(context);
			this.onNewIconAnimation = AnimationUtils.loadAnimation(context, R.anim.scaleup);
			this.onRemovedIconAnimation = AnimationUtils.loadAnimation(context, R.anim.scaledown);
			this.onAppliedEffectAnimation = AnimationUtils.loadAnimation(context, R.anim.scalebeat);
			this.onRemovedIconAnimation.setAnimationListener(this);

			res = context.getResources();

			text.setTextColor(res.getColor(android.R.color.white));
			text.setShadowLayer(1, 1, 1, res.getColor(android.R.color.black));
		}

		private void setActiveCondition(ActorCondition condition, boolean immunity) {
			this.immunity = immunity;
			this.condition = condition;
			tileManager.setImageViewTile(res, image, condition.conditionType, immunity);
			image.setVisibility(View.VISIBLE);
			setIconText();
		}

		public void setIconText() {
			boolean showMagnitude = (condition.magnitude != 1 && condition.magnitude != ActorCondition.MAGNITUDE_REMOVE_ALL);
			if (showMagnitude) {
				text.setText(Integer.toString(condition.magnitude));
				text.setVisibility(View.VISIBLE);
			} else {
				text.setVisibility(View.GONE);
			}
		}

		public void hide(boolean useAnimation) {
			if (useAnimation) {
				if (preferences.enableUiAnimations) {
					image.startAnimation(onRemovedIconAnimation);
				} else {
					onAnimationEnd(onRemovedIconAnimation);
				}
			} else {
				image.setVisibility(View.GONE);
				condition = null;
			}
			text.setVisibility(View.GONE);
		}
		public void show() {
			if (!preferences.enableUiAnimations) return;
			image.startAnimation(onNewIconAnimation);
			if (text.getVisibility() == View.VISIBLE) text.startAnimation(onNewIconAnimation);
		}

		public void pulseAnimate() {
			if (!preferences.enableUiAnimations) return;
			image.startAnimation(onAppliedEffectAnimation);
		}

		public boolean isVisible() {
			return condition != null;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (animation == this.onRemovedIconAnimation) {
				hide(false);
				rearrangeIconsLeftOf(this);
			}
		}

		@Override public void onAnimationRepeat(Animation animation) { }
		@Override public void onAnimationStart(Animation animation) { }
	}

	private void rearrangeIconsLeftOf(ActiveConditionIcon icon) {
		int i = currentConditionIcons.indexOf(icon);
		currentConditionIcons.remove(i);
		currentConditionIcons.add(icon);
		for(; i < currentConditionIcons.size(); ++i) {
			currentConditionIcons.get(i).image.setLayoutParams(getLayoutParamsForIconIndex(i));
		}
	}

	private ActiveConditionIcon getIconFor(ActorCondition condition, boolean immunity) {
		for (ActiveConditionIcon icon : currentConditionIcons) {
			if (icon.condition == condition && icon.immunity == immunity) return icon;
		}
		return null;
	}
	private ActiveConditionIcon getFirstFreeIcon() {
		for (ActiveConditionIcon icon : currentConditionIcons) {
			if (!icon.isVisible()) return icon;
		}
		return addNewActiveConditionIcon();
	}

	private RelativeLayout.LayoutParams getLayoutParamsForIconIndex(int index) {
		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		if (target == world.model.player) layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		else layout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		if (index == 0) {
			layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		} else {
			layout.addRule(RelativeLayout.LEFT_OF, currentConditionIcons.get(index-1).id);
		}
		return layout;
	}

	private ActiveConditionIcon addNewActiveConditionIcon() {
		int index = currentConditionIcons.size();

		ActiveConditionIcon icon = new ActiveConditionIcon(androidContext.get(), index+1);

		activeConditions.addView(icon.image, getLayoutParamsForIconIndex(index));

		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layout.addRule(RelativeLayout.ALIGN_RIGHT, icon.id);
		layout.addRule(RelativeLayout.ALIGN_BOTTOM, icon.id);
		activeConditions.addView(icon.text, layout);

		currentConditionIcons.add(icon);

		return icon;
	}
}
