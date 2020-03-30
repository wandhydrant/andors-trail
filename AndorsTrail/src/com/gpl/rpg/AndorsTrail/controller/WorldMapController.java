package com.gpl.rpg.AndorsTrail.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.CancellationSignal;
import android.os.Environment;
import android.widget.Toast;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.activity.DisplayWorldMapActivity;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.map.LayeredTileMap;
import com.gpl.rpg.AndorsTrail.model.map.MapLayer;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;
import com.gpl.rpg.AndorsTrail.model.map.WorldMapSegment;
import com.gpl.rpg.AndorsTrail.model.map.WorldMapSegment.NamedWorldMapArea;
import com.gpl.rpg.AndorsTrail.model.map.WorldMapSegment.WorldMapSegmentMap;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileCollection;
import com.gpl.rpg.AndorsTrail.util.Coord;
import com.gpl.rpg.AndorsTrail.util.CoordRect;
import com.gpl.rpg.AndorsTrail.util.L;
import com.gpl.rpg.AndorsTrail.util.Size;

public final class WorldMapController {

	private static final int WORLDMAP_SCREENSHOT_TILESIZE = 8;
	public static final int WORLDMAP_DISPLAY_TILESIZE = WORLDMAP_SCREENSHOT_TILESIZE;
	private static List<MapUpdateTask> mapUpdatesInProgress = new LinkedList<MapUpdateTask>();
	private static int updateCount = 0;

	private interface ICancellationToken {
		boolean isCancelled();
	}

	private static final class CancelationToken implements ICancellationToken {
		private AsyncTask<Void, Void, Void> task;

		@Override
		public boolean isCancelled() {
			return task.isCancelled();
		}

		public CancelationToken(AsyncTask<Void, Void, Void> task) {
			this.task = task;
		}
	}

	private static final class MapUpdateTask {
		private AsyncTask<Void, Void, Void> mapUpdateTask;
		private String mapName;
		private int updateCount;
		public MapUpdateTask(final AsyncTask<Void, Void, Void> mapUpdateTask, String mapName, int updateCount) {
			this.mapUpdateTask = mapUpdateTask;
			this.mapName = mapName;
			this.updateCount = updateCount;
		}
	}

	public static void updateWorldMap(final WorldContext world, final Resources res) {
		updateWorldMap(world, world.model.currentMaps.map, world.model.currentMaps.tileMap, world.model.currentMaps.tiles, res);
	}

	private static void updateWorldMap(
			final WorldContext world,
			final PredefinedMap map,
			final LayeredTileMap mapTiles,
			final TileCollection cachedTiles,
			final Resources res) {
		final String worldMapSegmentName = world.maps.getWorldMapSegmentNameForMap(map.name);
		if (worldMapSegmentName == null) return;

		if (!shouldUpdateWorldMap(map, worldMapSegmentName, world.maps.worldMapRequiresUpdate)) return;

		WorldMapController.updateCount++;
		final int updateCount = WorldMapController.updateCount;

		if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) {
			L.log("WorldMapController: Triggered update " + updateCount + " of worldmap segment " + worldMapSegmentName + " for map " + map.name);
		}

		synchronized (mapUpdatesInProgress) {
			ListIterator<MapUpdateTask> iterator = mapUpdatesInProgress.listIterator();
			while (iterator.hasNext()) {
				final MapUpdateTask task = iterator.next();
				if (task.mapName.equalsIgnoreCase(map.name)) {
					task.mapUpdateTask.cancel(true);
					iterator.remove();
					if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) {
						L.log("WorldMapController: Initialized cancelling update " + task.updateCount + " of worldmap segment " + worldMapSegmentName + " for map " + map.name);
					}
				}
			}
		}

		final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPostExecute(Void result) {
				synchronized (mapUpdatesInProgress) {
					ListIterator<MapUpdateTask> iterator = mapUpdatesInProgress.listIterator();
					while (iterator.hasNext()) {
						if (this == iterator.next().mapUpdateTask) {
							iterator.remove();
						}
					}
				}
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				final MapRenderer renderer = new MapRenderer(world, map, mapTiles, cachedTiles);
				try {
					if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) {
						L.log("WorldMapController: doInBackground " + updateCount + " of worldmap segment " + worldMapSegmentName + " for map " + map.name);
					}
					if (isCancelled()) {
						if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) {
							L.log("WorldMapController: Cancelling update " + updateCount + " of worldmap segment " + worldMapSegmentName + " for map " + map.name);
						}
						return null;
					}

					if (isCancelled()) {
						if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) {
							L.log("WorldMapController: Cancelling update " + updateCount + " of worldmap segment " + worldMapSegmentName + " for map " + map.name);
						}
						return null;
					}
					updateCachedBitmap(map, renderer);
					if (isCancelled()) {
						if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) {
							L.log("WorldMapController: Cancelling update " + updateCount + " of worldmap segment " + worldMapSegmentName + " for map " + map.name);
						}
						return null;
					}
					if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) {
						L.log("WorldMapController: Before updateWorldMapSegment " + updateCount + " of worldmap segment " + worldMapSegmentName + " for map " + map.name);
					}
					updateWorldMapSegment(res, world, worldMapSegmentName, new CancelationToken(this));
					if (isCancelled()) {
						if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) {
							L.log("WorldMapController: Cancelling update received too late " + updateCount + " of worldmap segment " + worldMapSegmentName + " for map " + map.name)
						}
						return null;
					}


					world.maps.worldMapRequiresUpdate = false;
					if (isCancelled()) {
						if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) {
							L.log("WorldMapController: Cancelling update received too late " + updateCount + " of worldmap segment " + worldMapSegmentName + " for map " + map.name);
						}
					}
					if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) {
						L.log("WorldMapController: Updated " + updateCount + " worldmap segment " + worldMapSegmentName + " for map " + map.name);
					}
				} catch (IOException e) {
					L.log("Error creating worldmap file for map " + map.name + " : " + e.toString());
				}
				return null;
			}
		};
		synchronized (mapUpdatesInProgress) {
			mapUpdatesInProgress.add(new MapUpdateTask(task, map.name, updateCount));
		}
		task.execute();
	}

	private static boolean shouldUpdateWorldMap(PredefinedMap map, String worldMapSegmentName, boolean forceUpdate) {
		if (forceUpdate) return true;
		if (!map.visited) return true;
		File file = getFileForMap(map, false);
		if (!file.exists()) return true;

		file = getCombinedWorldMapFile(worldMapSegmentName);
		if (!file.exists()) return true;

		return false;
	}

	private static void updateCachedBitmap(PredefinedMap map, MapRenderer renderer) throws IOException {
		ensureWorldmapDirectoryExists();

		File file = getFileForMap(map, false);
		if (file.exists()) return;

		Bitmap image = renderer.drawMap();
		FileOutputStream fos = new FileOutputStream(file);
		image.compress(Bitmap.CompressFormat.PNG, 70, fos);
		fos.flush();
		fos.close();
		image.recycle();
		L.log("WorldMapController: Wrote " + file.getAbsolutePath());
	}

	private static final class MapRenderer {
		private final PredefinedMap map;
		private final LayeredTileMap mapTiles;
		private final TileCollection cachedTiles;
		private final int tileSize;
		private final float scale;
		private final Paint mPaint = new Paint();

		public MapRenderer(final WorldContext world, final PredefinedMap map, final LayeredTileMap mapTiles, final TileCollection cachedTiles) {
			this.map = map;
			this.mapTiles = mapTiles;
			this.cachedTiles = cachedTiles;
			this.tileSize = world.tileManager.tileSize;
			this.scale = (float) WORLDMAP_SCREENSHOT_TILESIZE / world.tileManager.tileSize;
			mapTiles.setColorFilter(mPaint, null, true);
		}

		public Bitmap drawMap() {
			Bitmap image = Bitmap.createBitmap(map.size.width * WORLDMAP_SCREENSHOT_TILESIZE, map.size.height * WORLDMAP_SCREENSHOT_TILESIZE, Config.RGB_565);
			image.setDensity(Bitmap.DENSITY_NONE);
			Canvas canvas = new Canvas(image);
			canvas.scale(scale, scale);

			synchronized (cachedTiles) {
				drawMapLayer(canvas, mapTiles.currentLayout.layerGround);
				tryDrawMapLayer(canvas, mapTiles.currentLayout.layerObjects);
				tryDrawMapLayer(canvas, mapTiles.currentLayout.layerAbove);
				tryDrawMapLayer(canvas, mapTiles.currentLayout.layerTop);
			}
			return image;
		}

		private void tryDrawMapLayer(Canvas canvas, final MapLayer layer) {
			if (layer != null) drawMapLayer(canvas, layer);
		}

		private void drawMapLayer(Canvas canvas, final MapLayer layer) {
			int py = 0;
			for (int y = 0; y < map.size.height; ++y, py += tileSize) {
				int px = 0;
				for (int x = 0; x < map.size.width; ++x, px += tileSize) {
					final int tile = layer.tiles[x][y];
					if (tile == 0) continue;
					cachedTiles.drawTile(canvas, tile, px, py, mPaint);
				}
			}
		}
	}

	private static void ensureWorldmapDirectoryExists() throws IOException {
		File root = Environment.getExternalStorageDirectory();
		File dir = new File(root, Constants.FILENAME_SAVEGAME_DIRECTORY);
		if (!dir.exists()) dir.mkdir();
		dir = new File(dir, Constants.FILENAME_WORLDMAP_DIRECTORY);
		if (!dir.exists()) dir.mkdir();

		File noMediaFile = new File(dir, ".nomedia");
		if (!noMediaFile.exists()) noMediaFile.createNewFile();
	}
	public static boolean fileForMapExists(PredefinedMap map) {
		if (map.lastSeenLayoutHash.length() > 0) {
			return getPngFile(map.name + '.' + map.lastSeenLayoutHash).exists();
		}
		return getPngFile(map.name).exists();
	}
	private static File getFileForMap(PredefinedMap map, boolean verifyFileExists) {
		if (map.lastSeenLayoutHash.length() > 0) {
			File fileWithHash = getPngFile(map.name + '.' + map.lastSeenLayoutHash);
			if (!verifyFileExists) return fileWithHash;
			else if (fileWithHash.exists()) return fileWithHash;
		}
		return getPngFile(map.name);
	}
	private static File getPngFile(String fileName) {
		return new File(getWorldmapDirectory(), fileName + ".png");
	}
	private static File getWorldmapDirectory() {
		File dir = Environment.getExternalStorageDirectory();
		dir = new File(dir, Constants.FILENAME_SAVEGAME_DIRECTORY);
		return new File(dir, Constants.FILENAME_WORLDMAP_DIRECTORY);
	}
	public static File getCombinedWorldMapFile(String segmentName) {
		return new File(getWorldmapDirectory(), Constants.FILENAME_WORLDMAP_HTMLFILE_PREFIX + segmentName + Constants.FILENAME_WORLDMAP_HTMLFILE_SUFFIX);
	}

	private static String getWorldMapSegmentAsHtml(Resources res, WorldContext world, String segmentName, ICancellationToken cancellationToken) {
		WorldMapSegment segment = world.maps.worldMapSegments.get(segmentName);
		L.log("A1");
		Map<String, File> displayedMapFilenamesPerMapName = new HashMap<String, File>(segment.maps.size());
		Coord offsetWorldmapTo = new Coord(999999, 999999);
		L.log("A11");
		for (WorldMapSegmentMap map : segment.maps.values()) {
			if (cancellationToken.isCancelled()) {
				return "";
			}
			PredefinedMap predefinedMap = world.maps.findPredefinedMap(map.mapName);
			if (predefinedMap == null) continue;
			if (!predefinedMap.visited) continue;
			File f = WorldMapController.getFileForMap(predefinedMap, true);
			if (!f.exists()) continue;
			displayedMapFilenamesPerMapName.put(map.mapName, f);

			offsetWorldmapTo.x = Math.min(offsetWorldmapTo.x, map.worldPosition.x);
			offsetWorldmapTo.y = Math.min(offsetWorldmapTo.y, map.worldPosition.y);
			L.log("end");
		}

		L.log("A2");
		Coord bottomRight = new Coord(0, 0);

		StringBuilder mapsAsHtml = new StringBuilder(1000);
		for (WorldMapSegmentMap segmentMap : segment.maps.values()) {
			File f = displayedMapFilenamesPerMapName.get(segmentMap.mapName);
			if (f == null) continue;

			Size size = getMapSize(segmentMap, world);
			mapsAsHtml
				.append("<img src=\"")
				.append(f.getName())
				.append("\" id=\"")
				.append(segmentMap.mapName)
				.append("\" style=\"width:")
				.append(size.width * WorldMapController.WORLDMAP_DISPLAY_TILESIZE)
				.append("px; height:")
				.append(size.height * WorldMapController.WORLDMAP_DISPLAY_TILESIZE)
				.append("px; left:")
				.append((segmentMap.worldPosition.x - offsetWorldmapTo.x) * WorldMapController.WORLDMAP_DISPLAY_TILESIZE)
				.append("px; top:")
				.append((segmentMap.worldPosition.y - offsetWorldmapTo.y) * WorldMapController.WORLDMAP_DISPLAY_TILESIZE)
				.append("px;\" />");
			if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) mapsAsHtml.append('\n');

			bottomRight.x = Math.max(bottomRight.x, segmentMap.worldPosition.x + size.width);
			bottomRight.y = Math.max(bottomRight.y, segmentMap.worldPosition.y + size.height);
		}
		L.log("A3");
		Size worldmapSegmentSize = new Size(
				(bottomRight.x - offsetWorldmapTo.x) * WorldMapController.WORLDMAP_DISPLAY_TILESIZE
				,(bottomRight.y - offsetWorldmapTo.y) * WorldMapController.WORLDMAP_DISPLAY_TILESIZE
			);

		StringBuilder namedAreasAsHtml = new StringBuilder(500);
		for (NamedWorldMapArea area : segment.namedAreas.values()) {
			CoordRect r = determineNamedAreaBoundary(area, segment, world, displayedMapFilenamesPerMapName.keySet());
			if (r == null) continue;
			namedAreasAsHtml
				.append("<div class=\"namedarea ")
				.append(area.type)
				.append("\" style=\"width:")
				.append(r.size.width * WorldMapController.WORLDMAP_DISPLAY_TILESIZE)
				.append("px; line-height:")
				.append(r.size.height * WorldMapController.WORLDMAP_DISPLAY_TILESIZE)
				.append("px; left:")
				.append((r.topLeft.x - offsetWorldmapTo.x) * WorldMapController.WORLDMAP_DISPLAY_TILESIZE)
				.append("px; top:")
				.append((r.topLeft.y - offsetWorldmapTo.y) * WorldMapController.WORLDMAP_DISPLAY_TILESIZE)
				.append("px;\"><span>")
				.append(area.name)
				.append("</span></div>");
			if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) namedAreasAsHtml.append('\n');
		}
		L.log("A4");
		return res.getString(R.string.worldmap_template)
				.replace("{{maps}}", mapsAsHtml.toString())
				.replace("{{areas}}", namedAreasAsHtml.toString())
				.replace("{{sizex}}", Integer.toString(worldmapSegmentSize.width))
				.replace("{{sizey}}", Integer.toString(worldmapSegmentSize.height))
				.replace("{{offsetx}}", Integer.toString(offsetWorldmapTo.x * WorldMapController.WORLDMAP_DISPLAY_TILESIZE))
				.replace("{{offsety}}", Integer.toString(offsetWorldmapTo.y * WorldMapController.WORLDMAP_DISPLAY_TILESIZE));
	}

	private static Size getMapSize(WorldMapSegmentMap map, WorldContext world) {
		return world.maps.findPredefinedMap(map.mapName).size;
	}

	private static CoordRect determineNamedAreaBoundary(NamedWorldMapArea area, WorldMapSegment segment, WorldContext world, Set<String> displayedMapNames) {
		Coord topLeft = null;
		Coord bottomRight = null;

		for (String mapName : area.mapNames) {
			if (!displayedMapNames.contains(mapName)) continue;
			WorldMapSegmentMap map = segment.maps.get(mapName);
			Size size = getMapSize(map, world);
			if (topLeft == null) {
				topLeft = new Coord(map.worldPosition);
			} else {
				topLeft.x = Math.min(topLeft.x, map.worldPosition.x);
				topLeft.y = Math.min(topLeft.y, map.worldPosition.y);
			}
			if (bottomRight == null) {
				bottomRight = new Coord(map.worldPosition.x + size.width, map.worldPosition.y + size.height);
			} else {
				bottomRight.x = Math.max(bottomRight.x, map.worldPosition.x + size.width);
				bottomRight.y = Math.max(bottomRight.y, map.worldPosition.y + size.height);
			}
		}
		if (topLeft == null) return null;
		return new CoordRect(topLeft, new Size(bottomRight.x - topLeft.x, bottomRight.y - topLeft.y));
	}

	public static void updateWorldMapSegment(Resources res, WorldContext world, String segmentName, ICancellationToken cancellationToken) throws IOException {
		L.log("A");
		String mapAsHtml = getWorldMapSegmentAsHtml(res, world, segmentName, cancellationToken);
		if (cancellationToken.isCancelled()) {
			return;
		}

		L.log("B");
		File outputFile = getCombinedWorldMapFile(segmentName);
		PrintWriter pw = new PrintWriter(outputFile);
		L.log("C");
		pw.write(mapAsHtml);
		L.log("D");
		pw.close();
		L.log("E");
	}

	public static boolean displayWorldMap(Context context, WorldContext world) {
		String worldMapSegmentName = world.maps.getWorldMapSegmentNameForMap(world.model.currentMaps.map.name);
		if (worldMapSegmentName == null) {
			Toast.makeText(context, context.getResources().getString(R.string.display_worldmap_not_available), Toast.LENGTH_LONG).show();
			return false;
		}

		Intent intent = new Intent(context, DisplayWorldMapActivity.class);
		intent.putExtra("worldMapSegmentName", worldMapSegmentName);
		context.startActivity(intent);

		return true;
	}
}
