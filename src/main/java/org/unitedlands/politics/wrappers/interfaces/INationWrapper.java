package org.unitedlands.politics.wrappers.interfaces;

import java.util.Collection;

public interface INationWrapper extends IGeopolObjectWrapper {
    ITownWrapper getCapital();
    Collection<IRegionWrapper> getRegions();
    Collection<ITownWrapper> getTowns();

    boolean equals(Object obj);
}
