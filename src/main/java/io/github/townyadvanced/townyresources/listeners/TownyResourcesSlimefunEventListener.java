package io.github.townyadvanced.townyresources.listeners;

import io.github.thebusybiscuit.slimefun4.api.events.AndroidFarmEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunItemSpawnEvent;
import io.github.townyadvanced.townyresources.TownyResources;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TownyResourcesSlimefunEventListener implements Listener {

	@SuppressWarnings("unused")
	private final TownyResources plugin;
	
	public TownyResourcesSlimefunEventListener(TownyResources instance) {
		plugin = instance;
	}
	
	@EventHandler()
	public void onAndroidFarm(AndroidFarmEvent event) {
	}
	
	
	@EventHandler()
	public void onAndroidMine(AndroidMineEvent event) {
	}
	
	@EventHandler()
	public void onSlimefunItemSpawn(SlimefunItemSpawnEvent event) {
		event.setCancelled(true);
	}
	
	
	
	//Limited the extraction of GEOResources has thus far proved too difficult to code.
	//At present, it is recommended to disable the machines which extract any of the following:
	//oil, nether_ice, uranium, salt
	//-> And to simply provide these as town resources	
	//@EventHandler()
	//public void onMachineFinishEvent(AsyncMachineOperationFinishEvent event) {
	//}

}
