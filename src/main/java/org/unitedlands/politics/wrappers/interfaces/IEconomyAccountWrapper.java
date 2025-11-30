package org.unitedlands.politics.wrappers.interfaces;

public interface IEconomyAccountWrapper {
    double getBalance();
    void addMoney(double amount, String reason);
    void removeMoney(double amount, String reason);
}
