package org.unitedlands.politics.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.models.ActorProfile;
import org.unitedlands.utils.Logger;

public class ActorProfileManager {

    private final UnitedPolitics plugin;
    private Map<UUID, ActorProfile> actorProfiles;

    public ActorProfileManager(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    public void loadActorProfiles() {

        actorProfiles = new HashMap<>();

        var service = plugin.getDatabaseManager().getActorProfileService();
        service.getAllAsync().thenAccept(entries -> {
            for (var entry : entries) {
                actorProfiles.put(entry.getId(), entry);
            }
            Logger.log("Loaded " + entries.size() + " actor profiles to memory.", "UnitedPolitics");
        });

    }

    public ActorProfile getActorProfile(UUID id) {
        return actorProfiles.get(id);
    }

    public boolean addOrUpdateActorProfile(ActorProfile profile) {

        if (!actorProfiles.containsKey(profile.getId()))
            actorProfiles.put(profile.getId(), profile);

        var service = plugin.getDatabaseManager().getActorProfileService();
        return service.createOrUpdate(profile);
    }

}
