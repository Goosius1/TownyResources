package io.github.townyadvanced.townyresources.objects;

import io.github.townyadvanced.townyresources.enums.UpdateItemsScanStatus;

import java.util.List;

public class UpdateItemsScan {
    private final String name;
    private final UpdateItemsScanStatus status;
    private final List<UpdateItemsMaterialConversion> materialConversionsList;
    
    public UpdateItemsScan(String name, UpdateItemsScanStatus status, List<UpdateItemsMaterialConversion> materialConversionsList) {
        this.name = name;
        this.status = status;
        this.materialConversionsList = materialConversionsList;
    }
    
}
