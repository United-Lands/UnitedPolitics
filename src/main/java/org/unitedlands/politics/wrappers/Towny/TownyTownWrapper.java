package org.unitedlands.politics.wrappers.Towny;

import java.util.UUID;

import org.unitedlands.politics.wrappers.interfaces.IEconomyAccountWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.IRegionWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;

import com.palmergames.bukkit.towny.object.Town;

public class TownyTownWrapper implements ITownWrapper {

    private final Town town;

    public TownyTownWrapper(Town town) {
        if (town == null)
            throw new NullPointerException();
        this.town = town;
    }

    @Override
    public UUID getUUID() {
        return town.getUUID();
    }

    @Override
    public String getName() {
        return town.getName();
    }

    @Override
    public IRegionWrapper getRegion() {
        // Towny does not support regions
        return null;
    }

    @Override
    public INationWrapper getNation() {
        try {
            return new TownyNationWrapper(town.getNationOrNull());
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    public IEconomyAccountWrapper getBankAccount() {
        try {
            return new TownyEconomyAccountWrapper(town.getAccount());
        } catch (NullPointerException ignore) {
            return null;
        }
    }
    

}
