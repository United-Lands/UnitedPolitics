package org.unitedlands.politics.utils;

import org.unitedlands.unitedlands.classes.Country;
import org.unitedlands.unitedlands.classes.GeopolObject;
import org.unitedlands.unitedlands.classes.Region;
import org.unitedlands.unitedlands.classes.Settlement;

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

    public static String getGeopolPrefixColored(GeopolObject geopolObject) {
        var prefixStr = "";
        if (geopolObject instanceof Country)
            prefixStr = "<green>[N]</green>";
        else if (geopolObject instanceof Region)
            prefixStr = "<yellow>[R]</yellow>";
        else if (geopolObject instanceof Settlement)
            prefixStr = "<aqua>[T]</aqua>";
        return prefixStr;
    }
}
