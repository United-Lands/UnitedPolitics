package org.unitedlands.politics.wrappers.Towny;

import org.unitedlands.politics.wrappers.interfaces.IEconomyAccountWrapper;

import com.palmergames.bukkit.towny.object.economy.BankAccount;

public class TownyEconomyAccountWrapper implements IEconomyAccountWrapper {

    private final BankAccount bankAccount;

    public TownyEconomyAccountWrapper(BankAccount bankAccount) {
        if (bankAccount == null)
            throw new NullPointerException();
        this.bankAccount = bankAccount;
    }

    @Override
    public double getBalance() {
        return bankAccount.getCachedBalance(true);
    }

    @Override
    public void addMoney(double amount, String reason) {
        bankAccount.deposit(amount, reason);
    }

    @Override
    public void removeMoney(double amount, String reason) {
        bankAccount.withdraw(amount, reason);
    }





}
