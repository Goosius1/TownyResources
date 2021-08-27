package io.github.townyadvanced.townyresources.controllers;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import io.github.townyadvanced.townyresources.TownyResources;
import io.github.townyadvanced.townyresources.objects.UpdateItemsScan;
import io.github.townyadvanced.townyresources.settings.TownyResourcesSettings;
import io.github.townyadvanced.townyresources.util.TownyResourcesMessagingUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UpdateItemsScanController {

	private static final int SCAN_COUNTDOWN_DURATION_MILLIS = 60000; //1 minute 
	private static UpdateItemsScan pendingScan = null;
	private static boolean countdownStarted = false;
	private static boolean kickPlayers = false;
	private static boolean scanStarted = false;
	private static boolean scanStopping = false; //stop the scan or countdown
	private static double scanPlayersPercentageCompletion = 0;
	private static double scanChunksPercentageCompletion = 0;	

	/**
	 * Stops a scan
	 * 
	 * @param sender sender
	 * @throws TownyException on validation errors
	 */
	public static void stopScan(CommandSender sender) throws TownyException {
		if(!scanInProgress && !countdownInProgress) {
			throw new TownyException("There is no scan or countdown in progress.");
		}
		
		if(scanStopping) {
			throw new TownyException("The scan is stopping.");
		}
		
		if(scanInProgress) {
			scanStopping = true;  	//This requests that the scan be stopped
			TownyResourcesMessagingUtil.sendMsg(sender, "Done. The scan is now stopping.");
			return;
		}
		
		if(countdownInProgress) {
			countdownInProgress = false;   //This stops the countdown immediately
			TownyResourcesMessagingUtil.sendMsg(sender, "Done. The countdown is now stopped.");
			return;
		}
    }

	/**
	 * Starts a scan countdown
	 * 
	 * @param sender sender
	 * @throws TownyException on validation errors
	 */
	public static void startScanCountdown(CommandSender sender) throws TownyException {
		if(scanInProgress)
			throw new TownyException("A scan is already in progress");
			
		if(countdownInProgress)
			throw new TownyException("A countdown is already in progress"); 
	
		countdownInProgress = true;
		countdownEndEta = System.currentTimeMillis() + (int)(TownyResourcesSettings.getReduceItemsScanStartCountdownMinutes() * 60000);
	}

	/**
	 * This method validates and loads all scans
	 * 
	 * If a pending status is found, it will do the following:
	 * 1. Kick any non-op/non-admin players
	 * 2. Ensure those players cannot reconnect
	 * 3. Start a countdown to a scan:
	 * 
	 * When the countdown hits 0, the scan will start
	 */
	public static void loadItemScans() {
		//
	}

	/**
	 * Process update items scanning
	 * 
	 * If a scan is due to start, we start it
	 * If there is, and it reaches 0, start the scan
	 */
	public static void processUpdateItemsScanning() {
		if(!countdownInProgress)
			return;
			
		if(System.currentTimeMillis() < countdownEndEta)
			return;
			
		//Countdown has reached 0. Start scan
		countdownInProgress = false;
		scanInProgress = true;	
		scanPlayersPercentageCompletion = 0;
		scanChunksPercentageCompletion = 0;
						
		scanPlayers();
		scanLocations();

		//Scan finished
		scanInProgress = false;
			
		//Variables for convenience
		int batchSize = TownyResourcesSettings.getReduceItemsScanBatchSize();
		int pauseAfterEachBatchMillis = TownyResourcesSettings.getReduceItemsScanPauseAfterEachBatchMillis();
		int totalBatches;  //Helps with estimating progress (It is total num chunks + total num players
		
		//Variables to track scan progress
		int currentX;
		int currentY;
		int currentPlayer;
		int currentLocationName;
		int batchesAlreadyScanned;  //Helps with estimating progress
		
		
		
	}

	private static void scanPlayers() {
	
		int batchSize = TownyResourcesSettings.getReduceItemsScanBatchSize();
		int batchIndex = 0;
		int numBatchesCompleted = 0;

		Set<OfflinePlayer> players = new HashSet<>();
		players.addAll(Bukkit.getOnlinePlayers());
		players.addAll(Arrays.asList(Bukkit.getOfflinePlayers()));
		
		int totalPlayers = players.size();
		double percentageProgressPerPlayer = 100d / totalPlayers;
		double percentageProgressPerBatch = percentageProgressPerPlayer * batchSize;
		int pauseAfterEachBatchMillis = TownyResourcesSettings.getReduceItemsScanPauseAfterEachBatchMillis();
		
		//Scan every player
		for(OfflinePlayer offlinePlayer: Bukkit.getOfflinePlayers()) {
		
			try {

			UUID playerUUID = offlinePlayer.getUniqueId();
			String playerName = offlinePlayer.getName();
			CraftWorld craftWorld = (CraftWorld) Bukkit.getWorlds().get(0);
			WorldServer worldServer = craftWorld.getHandle();
			MinecraftServer minecraftServer =  ((CraftServer) Bukkit.getServer()).getServer();
			GameProfile gameProfile = new GameProfile(playerUUID, playerName);
			EntityPlayer entityPlayer = new EntityPlayer(minecraftServer, worldServer, gameProfile);
			Player player= entityPlayer.getBukkitEntity();

			System.out.println("The player name was: " + player.getName());
			System.out.println("The player had this many items in inventory: " + player.getInventory().getStorageContents().length);
			
/*
			try {
			new Player()

          Player target = null;
        GameProfile profile = new GameProfile(uuid, name);

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), profile, new PlayerInteractManager(server.getWorldServer(0)));
        entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        entity.world = ((CraftWorld) location.getWorld()).getHandle();
        target = entity == null ? null : (Player) entity.getBukkitEntity();
        if (target != null) {
            target.loadData();
            return target;
        }
			
				//Cycle inventory
				reduceInventory(player.getPlayer().getInventory());
				player.getPlayer().getInventory().getStorageContents()
				
				//Cycle enderchest
				player.getPlayer().getEnderChest()

				//If we have hit the end of the batch, update the percentage and pause
				batchIndex ++;
				if(batchIndex >= batchSize) {
					batchIndex = 0;
					scanPlayersPercentageCompletion += percentageProgressPerBatch;
					try {
						Thread.sleep(pauseAfterEachBatchMillis);
					} catch (InterruptedException e) {
						TownyResources.severe("Problem Sleeping after batch. If you see many of these your system could become overloaded.");
						e.printStackTrace();
					}
				}	
				*/	
			} catch (Exception e) {
				//Problem with a player. Display error without crashing
				String playerName = "Unknown";
				try { playerName = offlinePlayer.getName();
				} catch (Exception ignored) {}				
				TownyResources.severe("Problem scanning player " + playerName);				
				e.printStackTrace();
			}		

 		}
		
	}



	public static void scanLocations() {
		
	}
	/**
	 * If a scan is in progress, provide regular notifications
	 */
	public static void processScanNotifications() {
		if(!scanInProgress)
			return;
	
	}

	public static boolean getScanStarted() {
		return scanStarted;
	}

	public static void setScanStopping(boolean b) {
		scanStopping = true;
	}

	public static boolean getCountdownStarted() {
		return countdownStarted;
	}
	/*
		//Check if scan is already running
		
		//Check if the countdown to scan has already started
		

		int 
	
		String worldName = args[0];
		int xMin = Integer.parseInt(args[1]) / 16;	//Divide by 16 because we will be working off the grid of chunks
		int zMin = Integer.parseInt(args[2]) / 16;	
		int xMax = Integer.parseInt(args[3]) / 16;	
		int zMax = Integer.parseInt(args[4]) / 16;
		String extractionCategoryName = args[5];

		//Verify the world name is ok
		World world = Bukkit.getWorld(worldName);
		if(world == null)
			throw new TownyException("Unknown world name (" + worldName + ")");			

		//Verify the resourceCategory name is ok
		ResourceExtractionCategory resourceCategory = PlayerExtractionLimitsController.getResourceExtractionCategory(extractionCategoryName.toLowerCase());		
		if(resourceCategory == null)
;			throw new TownyException("Unknown resource category (" + extractionCategoryName + ") Ensure the category is included in the resource_extraction_limits.categories config");			
		List<Material> eligibleMaterials = resourceCategory.getMaterialsInCategory();

		//Find all theoretical chunk positions
		List<Integer[]> chunkPositions = new ArrayList<>();
		int x = xMin;
		int z = zMin;		
		while(z <= zMax) {
			chunkPositions.add(new Integer[]{x,z});
			x += 1;
			//If end of row, go to next one
			if(x > xMax) {
				x = xMin;
				z += 1;				
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
						if(itemStack != null && eligibleMaterials.contains(itemStack.getType())) {
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
			//Sleep to avoid overloading server
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("All done!");
		*/
	//}
	
	 
}
