package org.unitedlands.politics.wrappers.interfaces;

import java.util.Collection;

public interface IRegionWrapper extends IGeopolObjectWrapper {
    INationWrapper getNation();
    Collection<ITownWrapper> getTowns();

    boolean equals(Object obj);
}
