package org.unitedlands.politics.wrappers.interfaces;

import java.util.UUID;

public interface IGeopolObjectWrapper {
    UUID getUUID();
    String getName();
    String getCleanName();
    IEconomyAccountWrapper getBankAccount();
}
