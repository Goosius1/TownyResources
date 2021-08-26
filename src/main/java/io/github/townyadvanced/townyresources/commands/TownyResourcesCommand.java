package io.github.townyadvanced.townyresources.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import io.github.townyadvanced.townyresources.controllers.TownResourceDiscoveryController;
import io.github.townyadvanced.townyresources.controllers.TownResourceCollectionController;
import io.github.townyadvanced.townyresources.enums.TownyResourcesPermissionNodes;
import io.github.townyadvanced.townyresources.metadata.TownyResourcesGovernmentMetaDataController;
import io.github.townyadvanced.townyresources.settings.TownyResourcesSettings;
import io.github.townyadvanced.townyresources.settings.TownyResourcesTranslation;
import io.github.townyadvanced.townyresources.util.TownyResourcesMessagingUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TownyResourcesCommand implements CommandExecutor, TabCompleter {
	
	private static final List<String> townyResourcesTabCompletes = Arrays.asList("survey", "towncollect", "nationcollect");
	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1)
			return NameUtil.filterByStart(townyResourcesTabCompletes, args[0]);
		else
			return Collections.emptyList();
	}

	private void showTownyResourcesHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/townyresources"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/tr", "survey", TownyResourcesTranslation.of("help_survey")));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/tr", "towncollect", TownyResourcesTranslation.of("help_towncollect")));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/tr", "nationcollect", TownyResourcesTranslation.of("help_nationcollect")));
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player && args.length > 0)
			parseTownyResourcesCommand((Player) sender, args);
		else 
			showTownyResourcesHelp(sender);
		return true;
	}

	private void parseTownyResourcesCommand(Player player, String[] args) {
		try {
			//This permission check handles all the perms checks
			if (!player.hasPermission(TownyResourcesPermissionNodes.TOWNY_RESOURCES_COMMAND.getNode(args[0]))) {
				TownyResourcesMessagingUtil.sendErrorMsg(player, TownyResourcesTranslation.of("msg_err_command_disable"));
				return;
			}
			switch (args[0]) {
				case "survey":
					parseSurveyCommand(player);
					break;
				case "towncollect":
					parseTownCollectCommand(player);
					break;
				case "nationcollect":
					parseNationCollectCommand(player);
					break;
				default:
					showTownyResourcesHelp(player);
			}		
		} catch (TownyException te) {
			//Expected type of exception (e.g. not enough money)
			TownyResourcesMessagingUtil.sendErrorMsg(player, te.getMessage());
		} catch (Exception e) {
			//Unexpected exception
			TownyResourcesMessagingUtil.sendErrorMsg(player, e.getMessage());
			e.printStackTrace(); 		
		}
	}

	private void parseSurveyCommand(Player player) throws TownyException{
		WorldCoord playerWorldCoord = WorldCoord.parseWorldCoord(player);
			
		//Check if surveys are enabled
		if(!TownyResourcesSettings.areSurveysEnabled())
			throw new TownyException(TownyResourcesTranslation.of("msg_err_command_disable"));
			
		//Check if there is a town here
		if(!playerWorldCoord.hasTownBlock())
			throw new TownyException(TownyResourcesTranslation.of("msg_err_survey_no_town"));

		//Check if there are resources left to discover at the town
		Town town = playerWorldCoord.getTownBlock().getTown();
		List<Material> discoveredResources = TownyResourcesGovernmentMetaDataController.getDiscoveredAsList(town);
		List<Integer> costPerResourceLevel = TownyResourcesSettings.getSurveyCostsPerResourceLevel();
		List<Integer> requiredNumTownblocksPerResourceLevel = TownyResourcesSettings.getSurveyNumTownblocksRequirementsPerResourceLevel();
		if(discoveredResources.size() >= costPerResourceLevel.size())
			throw new TownyException(TownyResourcesTranslation.of("msg_err_survey_all_resources_already_discovered"));
		if(discoveredResources.size() >= requiredNumTownblocksPerResourceLevel.size())
			throw new TownyException(TownyResourcesTranslation.of("msg_err_survey_all_resources_already_discovered"));
		
		//Check if the town has enough townblocks
		int indexOfNextResourceLevel = discoveredResources.size();
		int requiredNumTownblocks = requiredNumTownblocksPerResourceLevel.get(indexOfNextResourceLevel);
		int currentNumTownblocks = town.getTownBlocks().size();
		if(currentNumTownblocks < requiredNumTownblocks)
			throw new TownyException(TownyResourcesTranslation.of("msg_err_survey_not_enough_townblocks", 
				requiredNumTownblocks, currentNumTownblocks));
		
		//Check that the player can afford the survey
		double surveyCost = costPerResourceLevel.get(indexOfNextResourceLevel);
		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (TownyEconomyHandler.isActive() && !resident.getAccount().canPayFromHoldings(surveyCost))
			throw new TownyException(TownyResourcesTranslation.of("msg_err_survey_too_expensive", 
				TownyEconomyHandler.getFormattedBalance(surveyCost), resident.getAccount().getHoldingFormattedBalance()));

		//Pay for the survey
		resident.getAccount().withdraw(surveyCost, "Cost of resources survey.");
				
		//Discover a new resource (notification will be sent from here)
		TownResourceDiscoveryController.discoverNewResource(resident, town, discoveredResources);
	}
	
	private static void parseTownCollectCommand(Player player) throws TownyException {
		//Ensure player a town member
		Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());		
		if(!resident.hasTown()) 
			throw new TownyException(TownyResourcesTranslation.of("msg_err_cannot_towncollect_not_a_town_member"));
					
		//Ensure player is actually in a town
		WorldCoord playerWorldCoord = WorldCoord.parseWorldCoord(player);
		if(!TownyUniverse.getInstance().hasTownBlock(playerWorldCoord))			
			throw new TownyException(TownyResourcesTranslation.of("msg_err_cannot_towncollect_not_in_own_town"));
			
		//Ensure player is actually in their own town
		Town town = TownyUniverse.getInstance().getTownBlock(playerWorldCoord).getTown();
		if(town != resident.getTown())
			throw new TownyException(TownyResourcesTranslation.of("msg_err_cannot_towncollect_not_in_own_town"));
			
		//Ensure some resources are available
		Map<Material, Integer> availableForCollection = TownyResourcesGovernmentMetaDataController.getAvailableForCollectionAsMap(town);
		if(availableForCollection.isEmpty())
			throw new TownyException(TownyResourcesTranslation.of("msg_err_cannot_towncollect_no_resources_available"));
		
		//Collect resources
		TownResourceCollectionController.collectAvailableTownResources(player, town, availableForCollection);
	}

	private static void parseNationCollectCommand(Player player) throws TownyException {
		//Ensure player a town member
		Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());		
		if(!resident.hasTown()) 
			throw new TownyException(TownyResourcesTranslation.of("msg_err_cannot_nationcollect_not_a_town_member"));

		//Ensure player is a nation member					
		if(!resident.getTown().hasNation())
			throw new TownyException(TownyResourcesTranslation.of("msg_err_cannot_nationcollect_not_a_nation_member"));
		
		//Ensure player is actually in a town
		WorldCoord playerWorldCoord = WorldCoord.parseWorldCoord(player);
		if(!TownyUniverse.getInstance().hasTownBlock(playerWorldCoord))			
			throw new TownyException(TownyResourcesTranslation.of("msg_err_cannot_nationcollect_not_in_capital"));
			
		//Ensure player is actually in the capital
		Town townHere = TownyUniverse.getInstance().getTownBlock(playerWorldCoord).getTown();
		Nation nation = resident.getTown().getNation();
		if(townHere != nation.getCapital())
			throw new TownyException(TownyResourcesTranslation.of("msg_err_cannot_nationcollect_not_in_capital"));
			
		//Ensure some resources are available
		Map<Material, Integer> availableForCollection = TownyResourcesGovernmentMetaDataController.getAvailableForCollectionAsMap(nation);
		if(availableForCollection.isEmpty())
			throw new TownyException(TownyResourcesTranslation.of("msg_err_cannot_nationcollect_no_resources_available"));
		
		//Collect resources
		TownResourceCollectionController.collectAvailableNationResources(player, nation, availableForCollection);
	}

}