package io.github.townyadvanced.townyresources.listeners;

import io.github.townyadvanced.townyresources.TownyResources;
import io.github.townyadvanced.townyresources.metadata.TownyResourcesGovernmentMetaDataController;
import io.github.townyadvanced.townyresources.settings.TownyResourcesSettings;
import io.github.townyadvanced.townyresources.settings.TownyResourcesTranslation;
import io.github.townyadvanced.townyresources.util.TownyResourcesMessagingUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.dynmap.towny.events.BuildTownMarkerDescriptionEvent;

public class TownyResourcesDynmapTownyListener implements Listener {

    @SuppressWarnings("unused")
    private final TownyResources plugin;

    public TownyResourcesDynmapTownyListener(TownyResources instance) {
        plugin = instance;
    }

    /**
     * This method updates the town popup box on Dynmap-Towny
     *
     * 1. It looks for the %town_resources% tag in the popup
     * 2. If the %town_resouces% tag exists, it replaces it with a list of town resources.
     * 3. If the town is a capital, the list of nation resources are also shown
     */
    @EventHandler
    public void on(BuildTownMarkerDescriptionEvent event) {
        if (TownyResourcesSettings.isEnabled()) {
            if (event.getDescription().contains("%town_resources%")) {
                if(event.getTown().isCapital()) {
                    String townProductionAsString = TownyResourcesGovernmentMetaDataController.getDailyProduction(event.getTown());
                    String formattedTownProductionAsString = TownyResourcesMessagingUtil.formatProductionStringForDynmapTownyDisplay(townProductionAsString);
                    String nationProductionAsString = TownyResourcesGovernmentMetaDataController.getDailyProduction(event.getTown().getNationOrNull());
                    String formattedNationProductionAsString = TownyResourcesMessagingUtil.formatProductionStringForDynmapTownyDisplay(nationProductionAsString);
                    String finalDescription = event.getDescription().replace("%town_resources%", TownyResourcesTranslation.of("dynmap_capital_resources_text", formattedTownProductionAsString, formattedNationProductionAsString));
                    event.setDescription(finalDescription);
                } else {
                    String productionAsString = TownyResourcesGovernmentMetaDataController.getDailyProduction(event.getTown());
                    String formattedProductionAsString = TownyResourcesMessagingUtil.formatProductionStringForDynmapTownyDisplay(productionAsString);
                    String finalDescription = event.getDescription().replace("%town_resources%", formattedProductionAsString);
                    event.setDescription(finalDescription);
                }
            }
        }
    }
}
