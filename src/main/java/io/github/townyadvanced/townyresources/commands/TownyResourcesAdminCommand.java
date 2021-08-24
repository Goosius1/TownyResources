package io.github.townyadvanced.townyresources.commands;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;
import io.github.townyadvanced.townyresources.TownyResources;
import io.github.townyadvanced.townyresources.controllers.PlayerExtractionLimitsController;
import io.github.townyadvanced.townyresources.enums.TownyResourcesPermissionNodes;
import io.github.townyadvanced.townyresources.objects.ResourceExtractionCategory;
import io.github.townyadvanced.townyresources.settings.TownyResourcesTranslation;
import io.github.townyadvanced.townyresources.util.TownyResourcesMessagingUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TownyResourcesAdminCommand implements CommandExecutor, TabCompleter {

	private static final List<String> tabCompletes = Arrays.asList("reload, removeitemstacks");

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1)
			return NameUtil.filterByStart(tabCompletes, args[0]);
		else
			return Collections.emptyList();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length > 0)
			parseAdminCommand(sender, args);
		else 
			showHelp(sender);
		return true;
	}

	private void parseAdminCommand(CommandSender sender, String[] args) {
		/*
		 * Parse Command.
		 */
	 	try {
			//This permission check handles all the perms checks
			if (sender instanceof Player && !sender.hasPermission(TownyResourcesPermissionNodes.TOWNY_RESOURCES_ADMIN_COMMAND.getNode(args[0]))) {
				TownyResourcesMessagingUtil.sendErrorMsg(sender, TownyResourcesTranslation.of("msg_err_command_disable"));
				return;
			}
			switch (args[0]) {
				case "reload":
					parseReloadCommand(sender);
				break;
				case "removeitemstacks":
					parseRemoveItemStacksCommand(sender, StringMgmt.remFirstArg(args));
				break;							
				/*
				 * Show help if no command found.
				 */
				default:
					showHelp(sender);
			}		 	
		} catch (Exception e) {
			e.printStackTrace();   //TODO- remove when release
			TownyResourcesMessagingUtil.sendErrorMsg(sender, e.getMessage());						
		}
	}
	
	private void showHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/townyresourcesadmin"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/tra", "reload", TownyResourcesTranslation.of("admin_help_reload")));
	}

	private void parseReloadCommand(CommandSender sender) {
		if (TownyResources.getPlugin().reloadAll()) {
			TownyResourcesMessagingUtil.sendMsg(sender, TownyResourcesTranslation.of("townyresources_reloaded_successfully"));
			return;
		}
		TownyResourcesMessagingUtil.sendErrorMsg(sender, TownyResourcesTranslation.of("townyresources_failed_to_reload"));
	}
	
	private void parseRemoveItemStacksCommand(CommandSender sender, String[] args) throws TownyException{
		String worldName = args[0];
		int xMin = Integer.parseInt(args[1]);	
		int yMin = Integer.parseInt(args[2]);	
		int xMax = Integer.parseInt(args[3]);	
		int yMax = Integer.parseInt(args[4]);
		String extractionCategoryName = args[5];

		//Verify the world name is ok
		World world = Bukkit.getWorld(worldName);
		if(world == null)
			throw new TownyException("Unknown world name (" + worldName + ")");			

		//Verify the resourceCategory name is ok
		ResourceExtractionCategory resourceCategory = PlayerExtractionLimitsController.getResourceExtractionCategory(extractionCategoryName.toLowerCase());		
		if(resourceCategory == null)
			throw new TownyException("Unknown resource category (" + extractionCategoryName + ") Ensure the category is included in the resource_extraction_limits.categories config");			
		List<Material> eligibleMaterials = resourceCategory.getMaterialsInCategory();

		//Find all theoretical chunk positions
		List<Integer[]> chunkPositions = new ArrayList<>();
		int x = xMin;
		int y = yMin;		
		while(y <= yMax) {
			chunkPositions.add(new Integer[]{x,y});
			x += 16;
			//If end of row, go to next one
			if(x > xMax) {
				x = xMin;
				y += 16;				
			}
		}

		//Shuffle the chunk positions
		Collections.shuffle(chunkPositions);
				
		//Cycle all chunks on the list
		Chunk chunk;
		boolean chunkWasLoadedBeforeScan;
		for(Integer[] chunkPosition: chunkPositions) {
			//Load chunk
			chunk = world.getChunkAt(chunkPosition[0], chunkPosition[1]);			
			chunk.setForceLoaded(true);
			if(chunk.isLoaded()) {
				chunkWasLoadedBeforeScan = true;
			} else {
				chunkWasLoadedBeforeScan = false;
				chunk.load();			
			}
			//Adjust contents of containers
			for(BlockState tileEntity: chunk.getTileEntities()) {
				if(tileEntity instanceof Container) {
					for(ItemStack itemStack: ((Container)tileEntity).getInventory().getContents()) {
						if(eligibleMaterials.contains(itemStack.getType())) {
							itemStack.setAmount(0);
						}
					}
				}
			}
			//Unload chunk
			chunk.setForceLoaded(false);				
			if(!chunkWasLoadedBeforeScan) {
				chunk.unload();
			}
		}
	}
}

