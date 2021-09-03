package io.github.townyadvanced.townyresources.listeners;

import io.github.thebusybiscuit.slimefun4.api.geo.GEOResource;
import io.github.thebusybiscuit.slimefun4.api.items.ItemState;
import io.github.thebusybiscuit.slimefun4.implementation.items.geo.OilPump;
import io.github.thebusybiscuit.slimefun4.implementation.resources.GEOResourcesSetup;
import io.github.townyadvanced.townyresources.TownyResources;
import io.github.townyadvanced.townyresources.controllers.PlayerExtractionLimitsController;
import io.github.townyadvanced.townyresources.settings.TownyResourcesSettings;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import io.github.thebusybiscuit.slimefun4.api.events.*;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;

public class TownyResourcesSlimefunEventListener implements Listener {

	@SuppressWarnings("unused")
	private final TownyResources plugin;
	
	public TownyResourcesSlimefunEventListener(TownyResources instance) {
		plugin = instance;
	}
	
	
	@EventHandler()
	public void onMachineFinishEvent(AsyncMachineOperationFinishEvent event) {
		System.out.println("A machine finished an event");
		
		//try {              
			//Get amount extracted
			//OilPump oilPump = (OilPump)event.getProcessor().getOwner();
			AContainer aContainer = (AContainer)event.getProcessor().getOwner();
								
	        //Field oil = oilPump.getClass().getDeclaredField("oil");  
	        //oil.setAccessible(true);      
	        //GEOResource gpsResource = (GEOResource)(oil.get(oilPump));
	        //int amountExtracted = gpsResource.getItem().getAmount();
	        //System.out.println("Amount extracted: " + amountExtracted);
	        
	    	//Remove that amount from the machine
	    	Block block = event.getPosition().getBlock();
			BlockMenu inv = BlockStorage.getInventory(block);			
			if (inv != null) {
				for(int outputSlot: aContainer.getOutputSlots()) {
					if(inv.getItemInSlot(outputSlot) != null && inv.getItemInSlot(outputSlot).getAmount() > 0) {
						inv.getItemInSlot(outputSlot).setAmount(inv.getItemInSlot(outputSlot).getAmount() - 1);
					}
				}
			}
	        
		//} catch (NoSuchFieldException | IllegalAccessException e) {
		//	e.printStackTrace();
		//}


/*
		try {
			System.out.println("1");
			Field f1 = slimefunItem.getClass().getDeclaredField("state");
			System.out.println("2");
			f1.setAccessible(true);
			System.out.println("3");
			f1.set(slimefunItem, ItemState.DISABLED);			
			System.out.println("4");
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		 */

	}


}
