package com.gpl.rpg.AndorsTrail.util;

import java.util.HashMap;
import java.util.Map;

import com.gpl.rpg.AndorsTrail.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

public class ThemeHelper {
	
	private static final class ThemeSet {
		String name;
		int baseThemeRes, noBackgroundThemeRes, dialogThemeRes;
		public ThemeSet(String name, int baseThemeRes, int noBackgroundThemeRes, int dialogThemeRes) {
			this.name = name;
			this.baseThemeRes = baseThemeRes;
			this.noBackgroundThemeRes = noBackgroundThemeRes;
			this.dialogThemeRes = dialogThemeRes;
		}
	}
	
	public static enum Theme {
		blue,
		red
	}
	
	private static final Map<Theme, ThemeSet> THEME_SETS = new HashMap<ThemeHelper.Theme, ThemeHelper.ThemeSet>();
	private static Theme SELECTED_THEME = Theme.blue;
	
	static {
		THEME_SETS.put(Theme.blue, new ThemeSet("Blue", R.style.AndorsTrailTheme_Blue, R.style.AndorsTrailTheme_Blue_NoBackground, R.style.AndorsTrailDialogTheme_Blue));
		THEME_SETS.put(Theme.red, new ThemeSet("Red", R.style.AndorsTrailTheme_Red, R.style.AndorsTrailTheme_Red_NoBackground, R.style.AndorsTrailDialogTheme_Red));
	}
	
	public static int getThemeColor(Context context, int attrResId) {
		TypedArray ta = context.getTheme().obtainStyledAttributes(new int[] {attrResId});
		int c = ta.getColor(0, Color.BLACK);
		ta.recycle();
		return c;
	}
	
	public static String getThemeName(Theme t) {
		return THEME_SETS.get(t).name;
	}
	
	public static int getBaseTheme() {
		return THEME_SETS.get(SELECTED_THEME).baseThemeRes;
	}
	
	public static int getNoBackgroundTheme() {
		return THEME_SETS.get(SELECTED_THEME).noBackgroundThemeRes;
	}
	
	public static int getDialogTheme() {
		return THEME_SETS.get(SELECTED_THEME).dialogThemeRes;
	}
	
	public static void changeTheme(Theme t) {
		SELECTED_THEME = t;
	}
}
