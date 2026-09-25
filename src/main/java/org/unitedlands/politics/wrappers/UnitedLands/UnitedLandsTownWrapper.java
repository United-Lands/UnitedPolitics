package org.unitedlands.politics.wrappers.UnitedLands;

import java.util.UUID;

import org.unitedlands.politics.wrappers.interfaces.IEconomyAccountWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.IRegionWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.unitedlands.classes.Settlement;

public class UnitedLandsTownWrapper implements ITownWrapper {

    private final Settlement settlement;

    public UnitedLandsTownWrapper(Settlement settlement) {
        this.settlement = settlement;
    }

    @Override
    public UUID getUUID() {
        return settlement.getUuid();
    }

    @Override
    public String getName() {
        return settlement.getName();
    }

    @Override
    public String getCleanName() {
        return settlement.getCleanName();
    }

    @Override
    public IEconomyAccountWrapper getBankAccount() {
        return new UnitedLandsEconomyAccountWrapper(settlement.getUuid());
    }

    @Override
    public IRegionWrapper getRegion() {
        if (settlement.hasRegion())
            return new UnitedLandsRegionWrapper(settlement.getRegion());
        return null;
    }

    @Override
    public INationWrapper getNation() {
        if (settlement.hasCountry())
            return new UnitedLandsNationWrapper(settlement.getCountry());
        return null;
    }

}
