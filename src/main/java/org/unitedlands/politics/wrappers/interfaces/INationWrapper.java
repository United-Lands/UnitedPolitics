package org.unitedlands.politics.wrappers.interfaces;

import java.util.Collection;

public interface INationWrapper extends IGeopolObjectWrapper {
    Collection<IRegionWrapper> getRegions();
    Collection<ITownWrapper> getTowns();
}
