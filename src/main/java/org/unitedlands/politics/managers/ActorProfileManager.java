package org.unitedlands.politics.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.unitedlands.politics.models.ActorProfile;
import org.unitedlands.utils.United;

public class ActorProfileManager {

    private static ActorProfileManager instance;
    
    public static ActorProfileManager instance() {
        return instance;
    }

    private final DatabaseManager databaseManager;
    private Map<UUID, ActorProfile> actorProfiles;

    public ActorProfileManager(DatabaseManager databaseManager) {
        instance = this;
        this.databaseManager = databaseManager;
    }

    public void loadActorProfiles() {

        actorProfiles = new HashMap<>();

        var service = databaseManager.getActorProfileService();
        service.getAllAsync().thenAccept(entries -> {
            for (var entry : entries) {
                actorProfiles.put(entry.getId(), entry);
            }
            United.logger().info("Loaded " + entries.size() + " actor profiles to memory.", "UnitedPolitics");
        });

    }
    public Collection<ActorProfile> getActorProfiles() {
        return actorProfiles.values();
    }

    public ActorProfile getActorProfile(UUID id) {
        return actorProfiles.get(id);
    }

    public boolean addOrUpdateActorProfile(ActorProfile profile) {

        if (!actorProfiles.containsKey(profile.getId()))
            actorProfiles.put(profile.getId(), profile);

        var service = databaseManager.getActorProfileService();
        return service.createOrUpdate(profile);
    }

}
