package org.unitedlands.politics.wrappers.UnitedLands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.unitedlands.politics.wrappers.interfaces.IGeopolWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.IRegionWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.unitedlands.UnitedLands;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

public class UnitedLandsGeopolWrapper implements IGeopolWrapper {

    public UnitedLandsGeopolWrapper() {
        United.logger().debug("Loading UnitedLands geopol wrapper");
        United.logger().debug(String.valueOf(UnitedLandsDataManager.instance()));
        United.logger().debug(String.valueOf(UnitedLands.instance()));
    }

    @Override
    public Collection<INationWrapper> getNations() {
        return UnitedLandsDataManager.instance().getCountries().stream().map(c -> new UnitedLandsNationWrapper(c))
                .collect(Collectors.toList());
    }

    @Override
    public INationWrapper getNation(UUID uuid) {
        var country = UnitedLandsDataManager.instance().getCountry(uuid);
        if (country != null) {
            return new UnitedLandsNationWrapper(country);
        }
        return null;
    }

    @Override
    public INationWrapper getNation(String name) {
        var country = UnitedLandsDataManager.instance().getCountry(name);
        if (country != null) {
            return new UnitedLandsNationWrapper(country);
        }
        return null;
    }

    @Override
    public Collection<IRegionWrapper> getRegions() {
        return UnitedLandsDataManager.instance().getRegions().stream().map(r -> new UnitedLandsRegionWrapper(r))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<IRegionWrapper> getNationRegions(UUID nationId) {
        var country = UnitedLandsDataManager.instance().getCountry(nationId);
        if (country == null || country.getRegionCount() == 0)
            return new ArrayList<>();
        return country.getRegions().stream().map(r -> new UnitedLandsRegionWrapper(r))
                .collect(Collectors.toList());
    }

    @Override
    public IRegionWrapper getRegion(UUID uuid) {
        var region = UnitedLandsDataManager.instance().getRegion(uuid);
        if (region != null) {
            return new UnitedLandsRegionWrapper(region);
        }
        return null;
    }

    @Override
    public IRegionWrapper getRegion(String name) {
        var region = UnitedLandsDataManager.instance().getRegion(name);
        if (region != null) {
            return new UnitedLandsRegionWrapper(region);
        }
        return null;
    }

    @Override
    public Collection<ITownWrapper> getTowns() {
        return UnitedLandsDataManager.instance().getSettlements().stream().map(s -> new UnitedLandsTownWrapper(s))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ITownWrapper> getNationTowns(UUID nationId) {
        var country = UnitedLandsDataManager.instance().getCountry(nationId);
        if (country == null || country.getRegionCount() == 0)
            return new ArrayList<>();
        return country.getSettlements().stream().map(s -> new UnitedLandsTownWrapper(s))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ITownWrapper> getRegionTowns(UUID regionId) {
        var region = UnitedLandsDataManager.instance().getRegion(regionId);
        if (region == null || region.getSettlements().size() == 0)
            return new ArrayList<>();
        return region.getSettlements().stream().map(s -> new UnitedLandsTownWrapper(s))
                .collect(Collectors.toList());
    }

    @Override
    public ITownWrapper getTown(UUID uuid) {
        var settlement = UnitedLandsDataManager.instance().getSettlement(uuid);
        if (settlement != null) {
            return new UnitedLandsTownWrapper(settlement);
        }
        return null;
    }

    @Override
    public ITownWrapper getTown(String name) {
        var settlement = UnitedLandsDataManager.instance().getSettlement(name);
        if (settlement != null) {
            return new UnitedLandsTownWrapper(settlement);
        }
        return null;
    }

    @Override
    public ITownWrapper getTownAtLocation(Location location) {
        try {
            return new UnitedLandsTownWrapper(UnitedLandsDataManager.instance().getSettlement(location));
        } catch (Exception ignore) {
            return null;
        }
    }

    @Override
    public ITownWrapper getTownByPlayer(Player player) {
        var citizen = UnitedLandsDataManager.instance().getCitizen(player);
        if (citizen == null || !citizen.hasSettlement())
            return null;
        return new UnitedLandsTownWrapper(citizen.getSettlement());
    }

}
