package org.unitedlands.politics.wrappers.UnitedLands;

import java.util.Collection;
import java.util.UUID;

import org.unitedlands.politics.wrappers.interfaces.IEconomyAccountWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.IRegionWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.unitedlands.classes.Region;

public class UnitedLandsRegionWrapper implements IRegionWrapper {

    private final Region region;

    public UnitedLandsRegionWrapper(Region region) {
        this.region = region;
    }

    @Override
    public UUID getUUID() {
        return region.getUuid();
    }

    @Override
    public String getName() {
        return region.getName();
    }

    @Override
    public String getCleanName() {
        return region.getCleanName();
    }

    @Override
    public IEconomyAccountWrapper getBankAccount() {
        return null;
    }

    @Override
    public INationWrapper getNation() {
        return null;
    }

    @Override
    public Collection<ITownWrapper> getTowns() {
        return null;
    }

}
