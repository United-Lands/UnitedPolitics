package org.unitedlands.politics.wrappers.UnitedLands;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.unitedlands.politics.wrappers.interfaces.IEconomyAccountWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.IRegionWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.unitedlands.classes.Country;
import org.unitedlands.unitedlands.classes.GeopolAttribute;

public class UnitedLandsNationWrapper implements INationWrapper {

    private final Country country;

    public UnitedLandsNationWrapper(Country country) {
        this.country = country;
    }

    @Override
    public UUID getUUID() {
        return country.getUuid();
    }

    @Override
    public String getName() {
        return country.getName();
    }

    @Override
    public String getCleanName() {
        return country.getCleanName();
    }

    @Override
    public IEconomyAccountWrapper getBankAccount() {
        return new UnitedLandsEconomyAccountWrapper(country.getUuid());
    }

    @Override
    public ITownWrapper getCapital() {
        if (country.hasCapital())
            return new UnitedLandsTownWrapper(country.getCapital());
        return null;
    }

    @Override
    public Collection<IRegionWrapper> getRegions() {
        if (country.getRegionCount() > 0)
            return country.getRegions().stream().map(r -> new UnitedLandsRegionWrapper(r)).collect(Collectors.toList());
        return null;
    }

    @Override
    public Collection<ITownWrapper> getTowns() {
        if (country.getSettlementCount() > 0)
            return country.getSettlements().stream().map(s -> new UnitedLandsTownWrapper(s))
                    .collect(Collectors.toList());
        return null;
    }

    @Override
    public double getDiplomacyScore() {

        // Create a default diplomacy attribute if none exists
        if (!country.hasAttribute("DIPLOMACY")) {
            var diplomanyAttribute = new GeopolAttribute(100, 100, 100, 0);
            country.addAttribute("DIPLOMACY", diplomanyAttribute);
            country.save();
        }

        return country.getModifiedAttribute("DIPLOMACY").getCurrentValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((country == null) ? 0 : country.hashCode());
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
        UnitedLandsNationWrapper other = (UnitedLandsNationWrapper) obj;
        if (country == null) {
            if (other.country != null)
                return false;
        } else if (!country.equals(other.country))
            return false;
        return true;
    }

}
