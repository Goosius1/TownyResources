package io.github.townyadvanced.townyresources.commands;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import io.github.townyadvanced.townyresources.TownyResources;
import io.github.townyadvanced.townyresources.controllers.UpdateItemsScanController;
import io.github.townyadvanced.townyresources.enums.TownyResourcesPermissionNodes;
import io.github.townyadvanced.townyresources.settings.TownyResourcesTranslation;
import io.github.townyadvanced.townyresources.util.TownyResourcesMessagingUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TownyResourcesAdminCommand implements CommandExecutor, TabCompleter {

	private static final List<String> tabCompletes = Arrays.asList("reload", "stopscan");

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 0)
			return Collections.emptyList();
		
		if (args.length == 1)
			return NameUtil.filterByStart(tabCompletes, args[0]);

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
				case "stopscan":
					parseStopScanCommand(sender);
					break;
					
				/*
				 * Show help if no command found.
				 */
				default:
					showHelp(sender);
			}		 	
		} catch (Exception e) {
			TownyResourcesMessagingUtil.sendErrorMsg(sender, e.getMessage());		
			e.printStackTrace();
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
	
	private void parseStopScanCommand(CommandSender sender) throws TownyException {
		if(!UpdateItemsScanController.getScanStarted() && UpdateItemsScanController.getCountdownStarted())
			throw new TownyException("There is no Update-Items scan or countdown in progress");
			
		UpdateItemsScanController.setScanStopping(true);
		TownyResourcesMessagingUtil.sendMsg(sender, "Update-Items Scan/Countdown stopping now");		
	}
}

