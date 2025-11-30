package org.unitedlands.politics.utils;

import java.util.UUID;

import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;

public class GeopolUtils {

    public static IGeopolObjectWrapper findGeopolObject(UUID id) {
        var geopolWrapper = UnitedPolitics.getInstance().getGeopolWrapper();
        var nation = geopolWrapper.getNation(id);
        if (nation != null)
            return (IGeopolObjectWrapper) nation;
        var region = geopolWrapper.getRegion(id);
        if (region != null)
            return (IGeopolObjectWrapper) region;
        var town = geopolWrapper.getTown(id);
        if (town != null)
            return (IGeopolObjectWrapper) town;
        return null;
    }

    public static IGeopolObjectWrapper findGeopolObject(String name) {
        var geopolWrapper = UnitedPolitics.getInstance().getGeopolWrapper();
        var nation = geopolWrapper.getNation(name);
        if (nation != null)
            return (IGeopolObjectWrapper) nation;
        var region = geopolWrapper.getRegion(name);
        if (region != null)
            return (IGeopolObjectWrapper) region;
        var town = geopolWrapper.getTown(name);
        if (town != null)
            return (IGeopolObjectWrapper) town;
        return null;
    }

}
