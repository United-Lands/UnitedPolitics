package org.unitedlands.politics.integrations.UnitedWar.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;


public class UnitedWarUtils {

    public boolean isActorInWar(IGeopolObjectWrapper actor) {

        // if (actor instanceof INationWrapper nation)
        //     return instance.getWarManager().isTownInWar(nation.getCapital().getUUID());
        // else if (actor instanceof ITownWrapper town)
        //     return instance.getWarManager().isTownInWar(town.getUUID());

        return false;
    }

    public Set<UUID> getOpponents(IGeopolObjectWrapper actor) {

        Set<UUID> result = new HashSet<>();
        // Map<War, WarSide> wars = null;
        // if (actor instanceof INationWrapper nation)
        //     wars = instance.getWarManager().getAllTownWars(nation.getCapital().getUUID());
        // else if (actor instanceof ITownWrapper town)
        //     wars = instance.getWarManager().getAllTownWars(town.getUUID());

        // if (wars == null || wars.isEmpty())
        //     return result;

        // for (var warSet : wars.entrySet()) {
        //     if (warSet.getValue() == WarSide.ATTACKER)
        //         result.addAll(warSet.getKey().getDefending_towns());
        //     else if (warSet.getValue() == WarSide.DEFENDER)
        //         result.addAll(warSet.getKey().getAttacking_towns());
        // }

        return result;
    }

    public Set<UUID> getAllies(IGeopolObjectWrapper actor) {

        Set<UUID> result = new HashSet<>();
        // Map<War, WarSide> wars = null;
        // if (actor instanceof INationWrapper nation)
        //     wars = instance.getWarManager().getAllTownWars(nation.getCapital().getUUID());
        // else if (actor instanceof ITownWrapper town)
        //     wars = instance.getWarManager().getAllTownWars(town.getUUID());

        // if (wars == null || wars.isEmpty())
        //     return result;

        // for (var warSet : wars.entrySet()) {
        //     if (warSet.getValue() == WarSide.ATTACKER)
        //         result.addAll(warSet.getKey().getAttacking_towns());
        //     else if (warSet.getValue() == WarSide.DEFENDER)
        //         result.addAll(warSet.getKey().getDefending_towns());
        // }

        return result;
    }
}
