package org.unitedlands.politics.wrappers.Towny;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.unitedlands.politics.wrappers.interfaces.IEconomyAccountWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.IRegionWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;

import com.palmergames.bukkit.towny.object.Nation;

public class TownyNationWrapper implements INationWrapper {

    private final Nation nation;

    public TownyNationWrapper(Nation nation) {
        if (nation == null)
            throw new NullPointerException();
        this.nation = nation;
    }

    @Override
    public UUID getUUID() {
        return nation.getUUID();
    }

    @Override
    public String getName() {
        return nation.getName();
    }

    @Override
    public IEconomyAccountWrapper getBankAccount() {
        try {
            return new TownyEconomyAccountWrapper(nation.getAccount());
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    @Override
    public Collection<IRegionWrapper> getRegions() {
        // Towny does not support regions
        return null;
    }

    @Override
    public Collection<ITownWrapper> getTowns() {
        return nation.getTowns().stream().map(town -> new TownyTownWrapper(town)).collect(Collectors.toList());
    }

}
