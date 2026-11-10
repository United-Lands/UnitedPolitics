package org.unitedlands.politics.commands.admin.ambassador;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand (
    parent = CmdAdminAmbassador.class,
    name = "set",
    usage = "/upa ambassador set <country> <citizen>",
    catchAll = true
)
public class CmdAdminAmbassadorSet implements UnitedCommandExecutor {

    
    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        switch (args.length) 
        {
            case 1:
                return UnitedLandsDataManager.instance().getCountryNames();
            case 2: 
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            default:
                break;
        };
        return null;
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        
        if (args.length != 2) {
            sendUsage(sender);
            return;
        }

        var targetCountry = UnitedLandsDataManager.instance().getCountry(args[0]);
        if (targetCountry == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

        var citizen = UnitedLandsDataManager.instance().getCitizen(args[1]);
        if (citizen == null) {
            United.messenger().send(sender, "errors.general.citizen-not-found", args[1]);
            return;
        }

        if (citizen.getCountry() == null) {
            United.messenger().send(sender, "errors.ambassador.citizen-not-in-country", targetCountry.getName());
            return;
        }


        if (citizen.getCountry().equals(targetCountry)) {
            United.messenger().send(sender, "errors.ambassador.citizen-in-target-country", targetCountry.getName());
            return;
        }

        if (DiplomacyManager.instance().isAmbassador(citizen)) {
            United.messenger().send(sender, "errors.ambassador.citizen-is-ambassador", citizen.getName());
            return;
        }

        if (DiplomacyManager.instance().hasAmbassador(citizen.getCountry(), targetCountry)) {
            United.messenger().send(sender, "errors.ambassador.already-has-ambassador", citizen.getCountry().getName(), targetCountry.getName());
            return;
        }

        var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(citizen.getCountry());
        record.addAmbassador(citizen, targetCountry);
        record.save();

        var entry = ReputationManager.instance().getOrCreateReputationScoreEntry(targetCountry.getUuid(), citizen.getCountry().getUuid(), "ambassador");
        entry.setModifier(0.25);
        ReputationManager.instance().addOrUpdateReputationScoreEntry(entry);

        United.messenger().send(sender, "success.ambassador.set", citizen.getName(), citizen.getCountry().getName(), targetCountry.getDisplayName());
    }

}
