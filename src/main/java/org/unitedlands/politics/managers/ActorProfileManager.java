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
    private Map<UUID, ActorProfile> actorProfiles = new HashMap<>();

    public ActorProfileManager(DatabaseManager databaseManager) {
        instance = this;
        this.databaseManager = databaseManager;
    }

    public void loadActorProfiles() {

        var service = databaseManager.getActorProfileService();
        try {
            var entries = service.getAllAsync().get();
            Map<UUID, ActorProfile> loadedProfiles = new HashMap<>();
            for (var entry : entries) {
                loadedProfiles.put(entry.getUuid(), entry);
            }
            actorProfiles = loadedProfiles;
            United.logger().info("Loaded " + entries.size() + " actor profiles to memory.", "UnitedPolitics");
        } catch (InterruptedException | ExecutionException e) {
            United.logger().error("Failed to load actor profiles: " + e.getMessage(), "UnitedPolitics");
        }

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
        actorProfiles.put(profile.getUuid(), profile);
        databaseManager.getActorProfileService().createAsync(profile);
        return true;
    }

    public boolean updateActorProfile(ActorProfile profile) {
        if (!actorProfiles.containsKey(profile.getUuid()))
            return false;
        actorProfiles.put(profile.getUuid(), profile);
        databaseManager.getActorProfileService().updateAsync(profile);
        return true;
    }

    public boolean removeActorProfile(ActorProfile profile) {
        var removed = actorProfiles.remove(profile.getUuid());
        if (removed == null)
            return false;
        databaseManager.getActorProfileService().deleteAsync(removed);
        return true;
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
