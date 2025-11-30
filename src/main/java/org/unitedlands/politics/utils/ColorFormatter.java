package org.unitedlands.politics.utils;

import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.IRegionWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;

public class ColorFormatter {
    public static String getAmountColored(double amount) {
        String amountStr;
        if (amount < 0)
            amountStr = "<red>" + String.format("%.2f", amount) + "</red>";
        else if (amount > 0)
            amountStr = "<green>+" + String.format("%.2f", amount) + "</green>";
        else
            amountStr = "<gold>" + String.format("%.2f", amount) + "</gold>";
        return amountStr;
    }

    public static String getGeopolPrefixColored(IGeopolObjectWrapper geopolObject) {
        var prefixStr = "";
        if (geopolObject instanceof INationWrapper)
            prefixStr = "<green>[N]</green>";
        else if (geopolObject instanceof IRegionWrapper)
            prefixStr = "<yellow>[R]</yellow>";
        else if (geopolObject instanceof ITownWrapper)
            prefixStr = "<aqua>[T]</aqua>";
        return prefixStr;
    }
}
