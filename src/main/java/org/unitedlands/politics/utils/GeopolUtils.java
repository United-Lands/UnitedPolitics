package org.unitedlands.politics.utils;

import java.util.UUID;

import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.utils.United;

public class GeopolUtils {

    public static IGeopolObjectWrapper findGeopolObject(UUID id) {
        var geopolWrapper = UnitedPolitics.instance().getGeopolWrapper();
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

        United.logger().debug("Searching " + name);
        
        var geopolWrapper = UnitedPolitics.instance().getGeopolWrapper();
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
