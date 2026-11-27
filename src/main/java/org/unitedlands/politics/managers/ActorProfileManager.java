package org.unitedlands.politics.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
                actorProfiles.put(entry.getUuid(), entry);
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

    public boolean addActorProfile(ActorProfile profile) {
        if (actorProfiles.containsKey(profile.getUuid()))
            return true;
        var service = databaseManager.getActorProfileService();
        try {
            return service.createAsync(profile).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateActorProfile(ActorProfile profile) {
        if (!actorProfiles.containsKey(profile.getUuid()))
            return false;
        var service = databaseManager.getActorProfileService();
        try {
            return service.updateAsync(profile).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeActorProfile(ActorProfile profile) {
        actorProfiles.remove(profile.getUuid());
        var service = databaseManager.getActorProfileService();
        try {
            return service.deleteAsync(profile).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removeEverythingFor(UUID objectId) {

        for (var actorProfile : getActorProfiles()) {
            boolean changed = false;
            if (actorProfile.hasRival(objectId)) {
                changed = true;
                actorProfile.removeRival(objectId);
            }
            if (actorProfile.hasPartner(objectId)) {
                changed = true;
                actorProfile.removePartner(objectId);
            }
            if (changed) {
                ActorProfileManager.instance().updateActorProfile(actorProfile);
            }
        }

        var actorProfile = getActorProfile(objectId);
        if (actorProfile != null) {
            removeActorProfile(actorProfile);
        }
        
    }

}
