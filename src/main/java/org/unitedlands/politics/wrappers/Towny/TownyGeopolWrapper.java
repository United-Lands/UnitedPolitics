package org.unitedlands.politics.wrappers.Towny;

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

import com.palmergames.bukkit.towny.TownyAPI;

public class TownyGeopolWrapper implements IGeopolWrapper {

    private final TownyAPI towny;

    public TownyGeopolWrapper() {
        towny = TownyAPI.getInstance();
    }

    @Override
    public Collection<INationWrapper> getNations() {
        return towny.getNations().stream().map(n -> new TownyNationWrapper(n)).collect(Collectors.toList());
    }

    @Override
    public INationWrapper getNation(UUID uuid) {
        try {
            return new TownyNationWrapper(towny.getNation(uuid));
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    @Override
    public INationWrapper getNation(String name) {
        try {
            return new TownyNationWrapper(towny.getNation(name));
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    @Override
    public Collection<IRegionWrapper> getRegions() {
        // Towny does not support regions
        return new ArrayList<>();
    }

    @Override
    public Collection<IRegionWrapper> getNationRegions(UUID nationId) {
        // Towny does not support regions
        return new ArrayList<>();
    }

    @Override
    public IRegionWrapper getRegion(UUID uuid) {
        // Towny does not support regions
        return null;
    }

    @Override
    public IRegionWrapper getRegion(String name) {
        // Towny does not support regions
        return null;
    }

    @Override
    public Collection<ITownWrapper> getTowns() {
        return towny.getTowns().stream().map(n -> new TownyTownWrapper(n)).collect(Collectors.toList());

    }

    @Override
    public Collection<ITownWrapper> getNationTowns(UUID nationId) {
        var nation = towny.getNation(nationId);
        if (nation != null)
            return nation.getTowns().stream().map(n -> new TownyTownWrapper(n)).collect(Collectors.toList());

        return new ArrayList<>();
    }

    @Override
    public Collection<ITownWrapper> getRegionTowns(UUID regionId) {
        // Towny does not support regions
        return new ArrayList<>();
    }

    @Override
    public ITownWrapper getTown(UUID uuid) {
        try {
            return new TownyTownWrapper(towny.getTown(uuid));
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    @Override
    public ITownWrapper getTown(String name) {
        try {
            return new TownyTownWrapper(towny.getTown(name));
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    @Override
    public ITownWrapper getTownAtLocation(Location location) {
        try {
            return new TownyTownWrapper(TownyAPI.getInstance().getTown(location));
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    @Override
    public ITownWrapper getTownByPlayer(Player player) {
        try {
            return new TownyTownWrapper(TownyAPI.getInstance().getTown(player));
        } catch (NullPointerException ignore) {
            return null;
        }
    }

}
