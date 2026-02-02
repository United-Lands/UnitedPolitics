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
    public ITownWrapper getCapital() {
        return new TownyTownWrapper(nation.getCapital());
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nation == null) ? 0 : nation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TownyNationWrapper other = (TownyNationWrapper) obj;
        if (nation == null) {
            if (other.nation != null)
                return false;
        } else if (!nation.getUUID().equals(other.nation.getUUID()))
            return false;
        return true;
    }

    
}
