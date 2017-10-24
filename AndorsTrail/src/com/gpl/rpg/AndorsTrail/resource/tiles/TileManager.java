package com.gpl.rpg.AndorsTrail.resource.tiles;

import java.util.HashMap;
import java.util.HashSet;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.AndorsTrailPreferences;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.ability.ActorConditionType;
import com.gpl.rpg.AndorsTrail.model.actor.Monster;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.item.Inventory;
import com.gpl.rpg.AndorsTrail.model.item.ItemContainer;
import com.gpl.rpg.AndorsTrail.model.item.ItemContainer.ItemEntry;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.model.map.LayeredTileMap;
import com.gpl.rpg.AndorsTrail.model.map.MapObject;
import com.gpl.rpg.AndorsTrail.model.map.MonsterSpawnArea;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;
import com.gpl.rpg.AndorsTrail.model.map.TMXMapTranslator;
import com.gpl.rpg.AndorsTrail.util.L;

public final class TileManager {
	public static final int CHAR_HERO = 1;
	public static final int iconID_selection_red = 2;
	public static final int iconID_selection_yellow = 3;
	public static final int iconID_attackselect = iconID_selection_red;
	public static final int iconID_moveselect = iconID_selection_yellow;
	public static final int iconID_groundbag = 4;
	public static final int iconID_boxopened = 5;
	public static final int iconID_boxclosed = 6;
	public static final int iconID_shop = iconID_groundbag;
	public static final int iconID_unassigned_quickslot = iconID_groundbag;
	public static final int iconID_selection_blue = 7;
	public static final int iconID_selection_purple = 8;
	public static final int iconID_selection_green = 9;

	public static final int iconID_splatter_red_1a = 10;
	public static final int iconID_splatter_red_1b = 11;
	public static final int iconID_splatter_red_2a = 12;
	public static final int iconID_splatter_red_2b = 13;
	public static final int iconID_splatter_brown_1a = 14;
	public static final int iconID_splatter_brown_1b = 15;
	public static final int iconID_splatter_brown_2a = 16;
	public static final int iconID_splatter_brown_2b = 17;
	public static final int iconID_splatter_white_1a = 18;
	public static final int iconID_splatter_white_1b = 19;
	
	public static final int iconID_immunity_overlay = 20;

	public int tileSize;
	public float density;
	public float uiIconScale;

	public int viewTileSize;
	public float scale;


	public final TileCache tileCache = new TileCache();
	public final TileCollection preloadedTiles = new TileCollection(114);
	public TileCollection currentMapTiles;
	public TileCollection adjacentMapTiles;
	private final HashSet<Integer> preloadedTileIDs = new HashSet<Integer>();


	public TileCollection loadTilesFor(HashSet<Integer> tileIDs, Resources r) {
		return tileCache.loadTilesFor(tileIDs, r);
	}

	public TileCollection loadTilesFor(ItemContainer container, Resources r) {
		return tileCache.loadTilesFor(getTileIDsFor(container), r);
	}

	public HashSet<Integer> getTileIDsFor(ItemContainer container) {
		HashSet<Integer> iconIDs = new HashSet<Integer>();
		for(ItemEntry i : container.items) {
			iconIDs.add(i.itemType.iconID);
		}
		return iconIDs;
	}

	public TileCollection loadTilesFor(Inventory inventory, Resources r) {
		HashSet<Integer> iconIDs = getTileIDsFor(inventory);
		for (Inventory.WearSlot slot : Inventory.WearSlot.values()) {
			ItemType t = inventory.getItemTypeInWearSlot(slot);
			if (t != null) iconIDs.add(t.iconID);
		}
		return tileCache.loadTilesFor(iconIDs, r);
	}

	public TileCollection loadTilesFor(PredefinedMap map, LayeredTileMap tileMap, WorldContext world, Resources r) {
		HashSet<Integer> iconIDs = getTileIDsFor(map, tileMap, world);
		TileCollection result = tileCache.loadTilesFor(iconIDs, r);
		for(int i : preloadedTileIDs) {
			result.setBitmap(i, preloadedTiles.getBitmap(i));
		}
		return result;
	}

	public HashSet<Integer> getTileIDsFor(PredefinedMap map, LayeredTileMap tileMap, WorldContext world) {
		HashSet<Integer> iconIDs = new HashSet<Integer>();
		for (MonsterSpawnArea a : map.spawnAreas) {
			for (String monsterTypeID : a.monsterTypeIDs) {
				iconIDs.add(world.monsterTypes.getMonsterType(monsterTypeID).iconID);
			}
			// Add icons for monsters that are already spawned, but that do not belong to the group of
			// monsters that usually spawn here. This could happen if we change the contents of spawn-
			// areas in a later release,
			for (Monster m : a.monsters) {
				iconIDs.add(m.iconID);
			}
		}
		iconIDs.addAll(tileMap.usedTileIDs);
		return iconIDs;
	}

	public void setDensity(Resources r) {
		density = r.getDisplayMetrics().density;
		uiIconScale = 100 * density;
//		tileSize = (int) (32 * density);
		if (density < 1) tileSize = (int) (32 * density);
		else tileSize = 32;
	}

	public void updatePreferences(AndorsTrailPreferences prefs) {
		float densityScaler = 1;
		if (density > 1) densityScaler = density;
		scale = prefs.scalingFactor * densityScaler;
		viewTileSize = (int) (tileSize * prefs.scalingFactor * densityScaler);
	}



	public void setImageViewTile(Resources res, TextView textView, Monster monster) { setImageViewTileForMonster(res, textView, monster.iconID); }
	public void setImageViewTile(Resources res, TextView textView, Player player) { setImageViewTileForPlayer(res, textView, player.iconID); }
	public void setImageViewTileForMonster(Resources res, TextView textView, int iconID) { setImageViewTile(res, textView, currentMapTiles.getBitmap(iconID)); }
	public void setImageViewTileForPlayer(Resources res, TextView textView, int iconID) { setImageViewTile(res, textView, preloadedTiles.getBitmap(iconID)); }
	public void setImageViewTile(Resources res, TextView textView, ActorConditionType conditionType) { setImageViewTile(res, textView, preloadedTiles.getBitmap(conditionType.iconID)); }
	public void setImageViewTile(Resources res, TextView textView, ActorConditionType conditionType, boolean immunityOverlay) { setImageViewTile(res, textView, preloadedTiles.getBitmap(conditionType.iconID), immunityOverlay); }
	public void setImageViewTileForUIIcon(Resources res, TextView textView, int iconID) { setImageViewTile(res, textView, preloadedTiles.getBitmap(iconID)); }
	private void setImageViewTile(Resources res, TextView textView, Bitmap b) { 
		if (density > 1) {
			setImageViewTile(textView, new BitmapDrawable(res, Bitmap.createScaledBitmap(b, (int)(tileSize*density), (int)(tileSize*density), true)));
		} else {
			setImageViewTile(textView, new BitmapDrawable(res, b)); 
		}
	}
	public void setImageViewTile(Resources res, TextView textView, Bitmap b, boolean immunityOverlay) {
		if (!immunityOverlay) setImageViewTile(res, textView, b);
		else {
			Drawable[] layers = new Drawable[2];
			if (density > 1) {
				layers[0] = new BitmapDrawable(res, Bitmap.createScaledBitmap(b, (int)(tileSize*density), (int)(tileSize*density), true));
				layers[1] = new BitmapDrawable(res, preloadedTiles.getBitmap(iconID_immunity_overlay));
			} else {
				layers[0] = new BitmapDrawable(res, b);
				layers[1] = new BitmapDrawable(res, preloadedTiles.getBitmap(iconID_immunity_overlay));
			}
			LayerDrawable layered = new LayerDrawable(layers);
			setImageViewTile(textView, layered);
		}
	}
	private void setImageViewTile(TextView textView, Drawable d) {
		/*if (density > 1) {
			ScaleDrawable sd = new ScaleDrawable(d, 0, uiIconScale, uiIconScale);
			sd.setLevel(8000);
			d.setBounds(0, 0, (int)(tileSize * density), (int)(tileSize * density));
			textView.setCompoundDrawables(sd, null, null, null);
		}
		else */textView.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
	}
	
	public void setImageViewTileForSingleItemType(Resources res, TextView textView, ItemType itemType) {
		final Bitmap icon = tileCache.loadSingleTile(itemType.iconID, res);
		setImageViewTile(res, textView, itemType, icon);
	}
	public void setImageViewTile(Resources res, TextView textView, ItemType itemType, TileCollection itemTileCollection) {
		final Bitmap icon = itemTileCollection.getBitmap(itemType.iconID);
		setImageViewTile(res, textView, itemType, icon);
	}
	private void setImageViewTile(Resources res, TextView textView, ItemType itemType, Bitmap icon) {
		final int overlayIconID = itemType.getOverlayTileID();
		if (overlayIconID != -1) {
			
			if (density > 1) {
			
			setImageViewTile(textView,
				new LayerDrawable(new Drawable[] {
					new BitmapDrawable(res, Bitmap.createScaledBitmap(preloadedTiles.getBitmap(overlayIconID), (int)(tileSize*density), (int)(tileSize*density), true))
					,new BitmapDrawable(res, Bitmap.createScaledBitmap(icon, (int)(tileSize*density), (int)(tileSize*density), true))
				})
			);
			} else {
				setImageViewTile(textView,
						new LayerDrawable(new Drawable[] {
							new BitmapDrawable(res, preloadedTiles.getBitmap(overlayIconID))
							,new BitmapDrawable(res, icon)
						})
					);	
			}
		} else {
			setImageViewTile(res, textView, icon);
		}
	}

	public void setImageViewTile(Resources res, ImageView imageView, Monster monster) { setImageViewTileForMonster(res, imageView, monster.iconID); }
	public void setImageViewTile(Resources res, ImageView imageView, Player player) { setImageViewTileForPlayer(res, imageView, player.iconID); }
	public void setImageViewTileForMonster(Resources res, ImageView imageView, int iconID) {  setImageViewTile(res, imageView, currentMapTiles.getBitmap(iconID)); }
	public void setImageViewTileForPlayer(Resources res, ImageView imageView, int iconID) {  setImageViewTile(res, imageView, preloadedTiles.getBitmap(iconID)); }
//	public void setImageViewTile(Resources res, ImageView imageView, ActorConditionType conditionType) {  setImageViewTile(res, imageView, preloadedTiles.getBitmap(conditionType.iconID)); }
	public void setImageViewTile(Resources res, ImageView imageView, ActorConditionType conditionType, boolean immunityOverlay) {  setImageViewTile(res, imageView, preloadedTiles.getBitmap(conditionType.iconID), immunityOverlay); }
	public void setImageViewTile(Resources res, ImageView imageView, ActorConditionType conditionType, boolean immunityOverlay, String exponent, String index) {  setImageViewTile(res, imageView, preloadedTiles.getBitmap(conditionType.iconID), immunityOverlay, exponent, index); }
	public void setImageViewTileForUIIcon(Resources res, ImageView imageView, int iconID) { setImageViewTile(res, imageView, preloadedTiles.getBitmap(iconID)); }
	public void setImageViewTile(Resources res, ImageView imageView, Bitmap b) {
		if (density > 1) {
			setImageViewTile(imageView, new BitmapDrawable(res, Bitmap.createScaledBitmap(b, (int)(tileSize*density), (int)(tileSize*density), true)));
		} else {
			setImageViewTile(imageView, new BitmapDrawable(res, b)); 
		}
	}
	public void setImageViewTile(Resources res, ImageView imageView, Bitmap b, boolean immunityOverlay) {
		setImageViewTile(res, imageView, b, immunityOverlay, null, null);
	}
	public void setImageViewTile(Resources res, ImageView imageView, Bitmap b, boolean immunityOverlay, String exponent, String index) {
		if (!immunityOverlay && exponent == null && index == null) setImageViewTile(res, imageView, b);
		else {
			Drawable[] layers = new Drawable[1+
			                                 (immunityOverlay ? 1 : 0)+
			                                 (exponent != null ? 1 : 0)+
			                                 (index != null ? 1 : 0)];
			int tileWidth;
			if (density > 1) {
				tileWidth = (int)(tileSize*density);
				layers[0] = new BitmapDrawable(res, Bitmap.createScaledBitmap(b, tileWidth, tileWidth, true));
			} else {
				tileWidth = tileSize;
				layers[0] = new BitmapDrawable(res, b);
			}
			int nextIndex = 1;
			if (immunityOverlay) {
				layers[nextIndex] = new BitmapDrawable(res, preloadedTiles.getBitmap(iconID_immunity_overlay));
				nextIndex++;
			}
			if (exponent != null) {
				layers[nextIndex] = new TextDrawable(res, tileWidth, tileWidth, exponent, TextDrawable.Align.TOP_RIGHT);
				nextIndex++;
			}
			if (index != null) {
				layers[nextIndex] = new TextDrawable(res, tileWidth, tileWidth, index, TextDrawable.Align.BOTTOM_RIGHT);
				nextIndex++;
			}
			LayerDrawable layered = new LayerDrawable(layers);
			setImageViewTile(imageView, layered);
		}
	}
	
	public void setImageViewTile(ImageView imageView, Drawable d) {
		imageView.setImageDrawable(d);
	}
	
	public void setImageViewTile(Resources res, ImageView imageView, ItemType itemType, TileCollection itemTileCollection) {
		final Bitmap icon = itemTileCollection.getBitmap(itemType.iconID);
		setImageViewTile(res, imageView, itemType, icon);
	}
	public void setImageViewTileWithOverlay(Resources res, ImageView imageView, int overlayIconID, Bitmap icon, boolean overlayAbove) {
		if (overlayIconID != -1) {
			Drawable overlayDrawable, iconDrawable;
			if (density > 1) {
				overlayDrawable = new BitmapDrawable(res, Bitmap.createScaledBitmap(preloadedTiles.getBitmap(overlayIconID), (int)(tileSize*density), (int)(tileSize*density), true));
				iconDrawable = new BitmapDrawable(res, Bitmap.createScaledBitmap(icon, (int)(tileSize*density), (int)(tileSize*density), true));
			} else {
				overlayDrawable = new BitmapDrawable(res, preloadedTiles.getBitmap(overlayIconID));
				iconDrawable = new BitmapDrawable(res, icon);
				}
			
			if (overlayAbove) {
				LayerDrawable layered = new LayerDrawable(new Drawable[] {
						iconDrawable
						,overlayDrawable
				});
				setImageViewTile(imageView, layered);
			} else {
				LayerDrawable layered = new LayerDrawable(new Drawable[] {
						overlayDrawable
						,iconDrawable
				});
				setImageViewTile(imageView, layered);
			}
		} else {
			setImageViewTile(res, imageView, icon);
		}
	}
	private void setImageViewTile(Resources res, ImageView imageView, ItemType itemType, Bitmap icon) {
		final int overlayIconID = itemType.getOverlayTileID();
		setImageViewTileWithOverlay(res, imageView, overlayIconID, icon, false);
	}

	public void loadPreloadedTiles(Resources r) {
		int maxTileID = tileCache.getMaxTileID();
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			if (maxTileID > preloadedTiles.maxTileID) {
				L.log("ERROR: TileManager.preloadedTiles needs to be initialized with at least " + maxTileID + " slots. Application will crash now.");
				throw new IndexOutOfBoundsException("ERROR: TileManager.preloadedTiles needs to be initialized with at least " + maxTileID + " slots. Application will crash now.");
			}
		}
		for(int i = TileManager.CHAR_HERO; i <= maxTileID; ++i) {
			preloadedTileIDs.add(i);
		}
		tileCache.loadTilesFor(preloadedTileIDs, r, preloadedTiles);
	}

	private final HashMap<String, HashSet<Integer>> tileIDsPerMap = new HashMap<String, HashSet<Integer>>();
	private void addTileIDsFor(HashSet<Integer> dest, String mapName, final Resources res, final WorldContext world) {
		HashSet<Integer> cachedTileIDs = tileIDsPerMap.get(mapName);
		if (cachedTileIDs == null) {
			PredefinedMap adjacentMap = world.maps.findPredefinedMap(mapName);
			if (adjacentMap == null) return;
			LayeredTileMap adjacentMapTiles = TMXMapTranslator.readLayeredTileMap(res, tileCache, adjacentMap);
			cachedTileIDs = getTileIDsFor(adjacentMap, adjacentMapTiles, world);
			tileIDsPerMap.put(mapName, cachedTileIDs);
		}
		dest.addAll(cachedTileIDs);
	}
	public void cacheAdjacentMaps(final Resources res, final WorldContext world, final PredefinedMap nextMap) {
		(new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... arg0) {
				adjacentMapTiles = null;

				HashSet<String> adjacentMapNames = new HashSet<String>();
				for (MapObject o : nextMap.eventObjects) {
					if (o.type != MapObject.MapObjectType.newmap) continue;
					if (o.map == null) continue;
					adjacentMapNames.add(o.map);
				}

				HashSet<Integer> tileIDs = new HashSet<Integer>();
				for (String mapName : adjacentMapNames) {
					addTileIDsFor(tileIDs, mapName, res, world);
				}

				long freeMemRequired = tileSize * tileSize * tileIDs.size() * 4 /*RGBA_8888*/ * 2 /*Require twice the needed size, to leave room for others*/;
				Runtime r = Runtime.getRuntime();
				
				if (r.maxMemory() - r.totalMemory() > freeMemRequired) {
					adjacentMapTiles = tileCache.loadTilesFor(tileIDs, res);
				}
				return null;
			}
		}).execute();
	}
	
	private static class TextDrawable extends Drawable {

		private String text;
		private int size = 15;
		private Align align = Align.CENTER;
		private Paint mPaint;
		private Rect textBounds;
		private int cHeight;
		private int cWidth;
		
		public enum Align {
			TOP,
			TOP_LEFT,
			TOP_RIGHT,
			CENTER,
			LEFT,
			RIGHT,
			BOTTOM,
			BOTTOM_LEFT,
			BOTTOM_RIGHT
		}
		
		public TextDrawable(Resources res, int cWidth, int cHeight, String text, Align align, int size) {
			this.text= text;
			this.align = align;
			this.size = size;
			this.cWidth = cWidth;
			this.cHeight = cHeight;
			init(res);
		}

		public TextDrawable(Resources res, int cWidth, int cHeight,  String text, Align align) {
			this.text= text;
			this.align = align;
			this.cWidth = cWidth;
			this.cHeight = cHeight;
			init(res);
		}
		
		public TextDrawable(Resources res, int cWidth, int cHeight,  String text) {
			this.text= text;
			this.cWidth = cWidth;
			this.cHeight = cHeight;
			init(res);
		}
		
		public void init(Resources res) {
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

			mPaint.setColor(res.getColor(android.R.color.black));
			mPaint.setShadowLayer(1, 1, 1, res.getColor(android.R.color.white));
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setTextSize(size * res.getDisplayMetrics().scaledDensity);
			textBounds = new Rect();
			mPaint.getTextBounds(text, 0, text.length(), textBounds);
		}
		
		
		
		@Override
		public void draw(Canvas canvas) {
			float x,y;
			switch (align) {
			case BOTTOM:
			case BOTTOM_LEFT:
			case BOTTOM_RIGHT:
				y = cHeight - textBounds.bottom;
				break;
			case CENTER:
			case LEFT:
			case RIGHT:
				y = (cHeight - textBounds.height()) / 2;
				break;
			case TOP:
			case TOP_LEFT:
			case TOP_RIGHT:
			default:
				y = 0 - textBounds.top;
				break;
			}
			
			switch (align) {
			case BOTTOM:
			case CENTER:
			case TOP:
				x = (cWidth - textBounds.width()) / 2;
				break;
			case BOTTOM_LEFT:
			case LEFT:
			case TOP_LEFT:
			default:
				x = 0 - textBounds.left;
				break;
			case BOTTOM_RIGHT:
			case RIGHT:
			case TOP_RIGHT:
				x = cWidth - textBounds.right;
				break;
			
			}
			canvas.drawText(text, x, y, mPaint);
		}

		@Override
		public void setAlpha(int alpha) {
			mPaint.setAlpha(alpha);
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			mPaint.setColorFilter(cf);
		}

		@Override
		public int getOpacity() {
			return mPaint.getAlpha();
		}
		
		@Override
		public int getIntrinsicWidth() {
			return cWidth;
		}
		
		@Override
		public int getIntrinsicHeight() {
			return cHeight;
		}
		
		@Override
		public boolean getPadding(Rect padding) {
			padding.bottom = 0;
			padding.top = 0;
			padding.left = 0;
			padding.right = 0;
			return false;
		}
		
	}
}
