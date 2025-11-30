package org.unitedlands.politics.wrappers.interfaces;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IGeopolWrapper {
    Collection<INationWrapper> getNations();
    INationWrapper getNation(UUID uuid);
    INationWrapper getNation(String name);

    Collection<IRegionWrapper> getRegions();
    Collection<IRegionWrapper> getNationRegions(UUID nationId);
    IRegionWrapper getRegion(UUID uuid);
    IRegionWrapper getRegion(String name);

    Collection<ITownWrapper> getTowns();
    Collection<ITownWrapper> getNationTowns(UUID nationId);
    Collection<ITownWrapper> getRegionTowns(UUID regionId);
    ITownWrapper getTown(UUID uuid);
    ITownWrapper getTown(String name);

    ITownWrapper getTownAtLocation(Location location);
    ITownWrapper getTownByPlayer(Player player);
}
