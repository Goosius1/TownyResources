package io.github.townyadvanced.townyresources.commands;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;
import io.github.townyadvanced.townyresources.TownyResources;
import io.github.townyadvanced.townyresources.controllers.ReduceItemsScanController;
import io.github.townyadvanced.townyresources.enums.TownyResourcesPermissionNodes;
import io.github.townyadvanced.townyresources.settings.TownyResourcesSettings;
import io.github.townyadvanced.townyresources.settings.TownyResourcesTranslation;
import io.github.townyadvanced.townyresources.util.TownyResourcesMessagingUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TownyResourcesAdminCommand implements CommandExecutor, TabCompleter {

	private static final List<String> tabCompletes = Arrays.asList("reload", "reduceitems");
	private static final List<String> reduceItemsTabCompletes = Arrays.asList("start", "stop");

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 0)
			return Collections.emptyList();
		
		if (args.length == 1)
			return NameUtil.filterByStart(tabCompletes, args[0]);

		switch (args[0].toLowerCase()) {
			case "reduceitems":	
				if (args.length == 2)
					return NameUtil.filterByStart(reduceItemsTabCompletes, args[1]);
				else
					return Collections.emptyList();								
			default:
				return Collections.emptyList();								
		}
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
				case "reduceitems":
					parseReduceItemsCommand(sender, StringMgmt.remFirstArg(args));
					break;
				/*
				 * Show help if no command found.
				 */
				default:
					showHelp(sender);
			}		 	
		} catch (TownyException e) {
			TownyResourcesMessagingUtil.sendErrorMsg(sender, e.getMessage());						
		} catch (Exception e) {
			TownyResourcesMessagingUtil.sendErrorMsg(sender, e.getMessage());		
			e.printStackTrace();
		}
	}
	
	private void showHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/townyresourcesadmin"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/tra", "reload", TownyResourcesTranslation.of("admin_help_reload")));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/tra", "reduceitems", TownyResourcesTranslation.of("admin_help_reduceitems")));
	}

	private void parseReloadCommand(CommandSender sender) {
		if (TownyResources.getPlugin().reloadAll()) {
			TownyResourcesMessagingUtil.sendMsg(sender, TownyResourcesTranslation.of("townyresources_reloaded_successfully"));
			return;
		}
		TownyResourcesMessagingUtil.sendErrorMsg(sender, TownyResourcesTranslation.of("townyresources_failed_to_reload"));
	}

	private void parseReduceItemsCommand(CommandSender sender, String[] args) throws TownyException {		
		//Validations of instruction
		if(args[0].length() == 0) {
			showHelp(sender);
			return;
		}

		if(!TownyResourcesSettings.isReduceItemsScanEnabled())
			throw new TownyException("Not enabled");

		String instruction = args[0].toLowerCase();		
		switch (instruction.toLowerCase()) {
			case "start":
				ReduceItemsScanController.startScan(sender);
				return;
			case "stop":
				ReduceItemsScanController.stopScan(sender);
				return;
			default:
				showHelp(sender);
				return;			
		}
	}

}

