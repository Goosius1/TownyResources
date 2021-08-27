package io.github.townyadvanced.townyresources.objects;

import org.bukkit.Material;

import java.util.List;

public class UpdateItemsMaterialConversion {
    private final List<Material> sourceMaterials;
    private final Material finalMaterial;
    private final int percentageChange;
    
    public UpdateItemsMaterialConversion(List<Material> sourceMaterials, Material finalMaterial, int percentageChange) {
        this.sourceMaterials = sourceMaterials;
        this.finalMaterial = finalMaterial;
        this.percentageChange = percentageChange;
    }
}
