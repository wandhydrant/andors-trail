package com.gpl.rpg.AndorsTrail.activity;

import com.gpl.rpg.AndorsTrail.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class CustomDialog {
	
	private static final int MIN_RES_ID=0x7f040000;
	
	public static Dialog createDialog(final Context context, String title, Drawable icon, String desc, View content, boolean hasButtons) {
		Dialog dialog = new Dialog(new ContextThemeWrapper(context, R.style.AndorsTrailStyle_Dialog));
		
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.custom_dialog_title_icon);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

		TextView titleView = (TextView) dialog.findViewById(R.id.dialog_title);
		if (title != null || icon != null) {
			titleView.setText(title);
			titleView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
			titleView.setVisibility(View.VISIBLE);
		} else {
			titleView.setVisibility(View.GONE);
		}
		
		TextView descView = (TextView) dialog.findViewById(R.id.dialog_description);
		ViewGroup descHolder = (ViewGroup) dialog.findViewById(R.id.dialog_description_container);
		if (desc != null) {
			descView.setText(desc);
			descHolder.setVisibility(View.VISIBLE);
			descView.setVisibility(View.VISIBLE);
		} else {
			descHolder.setVisibility(View.GONE);
		}
		
		ViewGroup contentHolder = (ViewGroup) dialog.findViewById(R.id.dialog_content_container);
		if (content != null) {
			contentHolder.addView(content);
			contentHolder.setVisibility(View.VISIBLE);
		} else {
			contentHolder.setVisibility(View.GONE);
		}
		
		ViewGroup buttonsHolder = (ViewGroup) dialog.findViewById(R.id.dialog_button_container);
		if (hasButtons) {
			buttonsHolder.setVisibility(View.VISIBLE);
		} else {
			buttonsHolder.setVisibility(View.GONE);
		}
		
		return dialog;
	}
	
	public static Dialog addButton(final Dialog dialog, int textId, final OnClickListener listener) {
		
		Button template = (Button) dialog.findViewById(R.id.dialog_template_button);
		LayoutParams params = template.getLayoutParams();
		ViewGroup buttonsHolder = (ViewGroup) dialog.findViewById(R.id.dialog_button_container);
		
		Button b = new Button(dialog.getContext());
		b.setLayoutParams(params);
		b.setText(textId);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listener.onClick(v);
				dialog.dismiss();
			}
		});
		
		buttonsHolder.addView(b, params);
		return dialog;
	}
	
	public static Dialog addDismissButton(final Dialog dialog, int textId) {
		return CustomDialog.addButton(dialog, textId, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}
	
	public static Dialog setDismissListener(Dialog dialog, OnDismissListener listener) {
		dialog.setOnDismissListener(listener);
		
		return dialog;
	}
	
	public static void show(Dialog dialog) {
		
		dialog.findViewById(R.id.dialog_template_button).setVisibility(View.GONE);
		dialog.show();
		
	}

}
