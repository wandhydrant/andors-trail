package com.gpl.rpg.AndorsTrail.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

public class ThemeHelper {
	
	public static int getThemeColor(Context context, int attrResId) {
		TypedArray ta = context.getTheme().obtainStyledAttributes(new int[] {attrResId});
		int c = ta.getColor(0, Color.BLACK);
		ta.recycle();
		return c;
	}
}
