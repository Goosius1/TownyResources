package io.github.townyadvanced.townyresources.listeners;

import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.util.ChatTools;
import io.github.townyadvanced.townyresources.TownyResources;
import io.github.townyadvanced.townyresources.metadata.TownyResourcesGovernmentMetaDataController;
import io.github.townyadvanced.townyresources.settings.TownyResourcesSettings;
import io.github.townyadvanced.townyresources.settings.TownyResourcesTranslation;
import io.github.townyadvanced.townyresources.util.TownyResourcesMessagingUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Goosius
 *
 */
public class TownyResourcesTownEventListener implements Listener {

	@SuppressWarnings("unused")
	private final TownyResources plugin;
	
	public TownyResourcesTownEventListener(TownyResources instance) {

		plugin = instance;
	}

	/*
	 * TownyResources will add resource info to the town screen
	 */
	@EventHandler
	public void onTownStatusScreen(TownStatusScreenEvent event) {
		if (TownyResourcesSettings.isEnabled()) {
			Town town = event.getTown();
			String availableAsString = TownyResourcesGovernmentMetaDataController.getAvailableForCollection(town);
			String townProductionAsString = TownyResourcesGovernmentMetaDataController.getDailyProduction(town);
			List<String> textLines;
			String[] resourcesAsFormattedArray;

			if(event.getTown().isCapital()) {
				String nationProductionAsString = TownyResourcesGovernmentMetaDataController.getDailyProduction(town.getNationOrNull());
				if(townProductionAsString.isEmpty() && nationProductionAsString.isEmpty() && availableAsString.isEmpty())
					return;

				//Resources:
			 	textLines = new ArrayList<>();
				textLines.add(TownyResourcesTranslation.of("town.screen.header"));

				// > Daily Production - Town: [2] 32 oak Log, 32 sugar cane
				if(!townProductionAsString.isEmpty()) {
					resourcesAsFormattedArray = TownyResourcesMessagingUtil.formatResourcesStringForGovernmentScreenDisplay(townProductionAsString);
					textLines.addAll(ChatTools.listArr(resourcesAsFormattedArray, TownyResourcesTranslation.of("town.screen.daily.capital.production", resourcesAsFormattedArray.length)));
				}

				// > Daily Production - Nation: [3] 32 oak Log, 32 sugar cane, 8 Diamond
				if(!nationProductionAsString.isEmpty()) {
					resourcesAsFormattedArray = TownyResourcesMessagingUtil.formatResourcesStringForGovernmentScreenDisplay(nationProductionAsString);
					textLines.addAll(ChatTools.listArr(resourcesAsFormattedArray, TownyResourcesTranslation.of("town.screen.daily.nation.production", resourcesAsFormattedArray.length)));
				}

			} else {
				if(townProductionAsString.isEmpty() && availableAsString.isEmpty())
					return;

				//Resources:
			 	textLines = new ArrayList<>();
				textLines.add(TownyResourcesTranslation.of("town.screen.header"));

				// > Daily Production: [2] 32 oak Log, 32 sugar cane
				if(!townProductionAsString.isEmpty()) {
					resourcesAsFormattedArray = TownyResourcesMessagingUtil.formatResourcesStringForGovernmentScreenDisplay(townProductionAsString);
					textLines.addAll(ChatTools.listArr(resourcesAsFormattedArray, TownyResourcesTranslation.of("town.screen.daily.production", resourcesAsFormattedArray.length)));
				} 
			}

			// > Available For Collection [2]: 64 oak log, 64 sugar cane
			if(!availableAsString.isEmpty()) {
				resourcesAsFormattedArray = TownyResourcesMessagingUtil.formatResourcesStringForGovernmentScreenDisplay(availableAsString); 
				textLines.addAll(ChatTools.listArr(resourcesAsFormattedArray, TownyResourcesTranslation.of("town.screen.available.for.collection", resourcesAsFormattedArray.length)));
			}

			event.addLines(textLines);
		}
	}
}
