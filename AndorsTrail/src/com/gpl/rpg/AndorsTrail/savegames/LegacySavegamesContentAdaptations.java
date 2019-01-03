package com.gpl.rpg.AndorsTrail.savegames;

import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.actor.Monster;
import com.gpl.rpg.AndorsTrail.model.map.MonsterSpawnArea;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;

public class LegacySavegamesContentAdaptations {
	
	public static void adaptToNewContentForVersion45(WorldContext world, ControllerContext controllers) {
		PredefinedMap map = world.maps.findPredefinedMap("fields5");
		if (map != null) {
			for (MonsterSpawnArea area : map.spawnAreas) {
				if (area.monsters != null) {
					for (Monster m : area.monsters) {
						if (m.getMonsterTypeID().equals("feygard_bridgeguard")) {
							area.resetForNewGame();
							for (MonsterSpawnArea newarea : map.spawnAreas) {
								if (newarea.areaID.equals("guynmart_robber1")) {
									controllers.monsterSpawnController.spawnAllInArea(map, 
											(world.model.currentMap == map ? world.model.currentTileMap : null), 
											newarea, true);
									break;
								}
							}
							break;
						}
					}
				}
			}
		}
		
		
	}

}
