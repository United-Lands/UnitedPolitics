package org.unitedlands.politics.wrappers.interfaces;

public interface ITownWrapper extends IGeopolObjectWrapper {
    IRegionWrapper getRegion();
    INationWrapper getNation();

    boolean equals(Object obj);
}
