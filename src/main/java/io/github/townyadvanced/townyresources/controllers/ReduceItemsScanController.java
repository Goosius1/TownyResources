package io.github.townyadvanced.townyresources.controllers;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import io.github.townyadvanced.townyresources.objects.ResourceExtractionCategory;
import io.github.townyadvanced.townyresources.settings.TownyResourcesSettings;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReduceItemsScanController {

	public static boolean countdownInProgress;
	public static long scanStartEta;

    public static void stopScan(CommandSender sender) throws TownyException {
        //This will stop both the scan itself  OR the countdown
    }

	public static void startScan(CommandSender sender) throws TownyException {
	
		//Check if scan is already running
		
		//Check if the countdown to scan has already started
		
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
	}
}
