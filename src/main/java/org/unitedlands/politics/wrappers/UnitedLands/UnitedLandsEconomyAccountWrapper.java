package org.unitedlands.politics.wrappers.UnitedLands;

import java.math.BigDecimal;
import java.util.UUID;

import org.unitedlands.politics.wrappers.interfaces.IEconomyAccountWrapper;
import org.unitedlands.unitedlands.managers.UnitedLandsEconomyManager;

public class UnitedLandsEconomyAccountWrapper implements IEconomyAccountWrapper {

    private final UUID accountHolderUuid;

    public UnitedLandsEconomyAccountWrapper(UUID accountHolderUuid) {
        this.accountHolderUuid = accountHolderUuid;
    }

    @Override
    public double getBalance() {
        return UnitedLandsEconomyManager.instance().getBalance(accountHolderUuid).doubleValue();
    }

    @Override
    public void addMoney(double amount, String reason) {
        UnitedLandsEconomyManager.instance().deposit(accountHolderUuid, new BigDecimal(amount), reason);
    }

    @Override
    public void removeMoney(double amount, String reason) {
        UnitedLandsEconomyManager.instance().withdraw(accountHolderUuid, new BigDecimal(amount), reason);
    }

}
