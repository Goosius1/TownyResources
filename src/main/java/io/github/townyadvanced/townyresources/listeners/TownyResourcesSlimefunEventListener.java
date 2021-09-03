package io.github.townyadvanced.townyresources.listeners;

import io.github.townyadvanced.townyresources.TownyResources;
import io.github.townyadvanced.townyresources.controllers.PlayerExtractionLimitsController;
import io.github.townyadvanced.townyresources.settings.TownyResourcesSettings;
import org.bukkit.event.EventHandler;
import io.github.thebusybiscuit.slimefun4.api.events.*;
import org.bukkit.event.Listener;

public class TownyResourcesSlimefunEventListener implements Listener {

	@SuppressWarnings("unused")
	private final TownyResources plugin;
	
	public TownyResourcesSlimefunEventListener(TownyResources instance) {
		plugin = instance;
	}
	
	
	@EventHandler()
	public void onMachineFinishEvent(AsyncMachineOperationFinishEvent event) {
		System.out.println("A machine finished an event");
		
//		event.getOperation().addProgress(-100);
		event.getProcessor().setProgressBar(null);
		

//		if(TownyResourcesSettings.isEnabled() && !event.isCancelled()) {
//			PlayerExtractionLimitsController.processEntityDamageByEntityEvent(event);
//		}
	}


}
