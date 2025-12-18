package org.unitedlands.politics.services;

import java.util.UUID;

import org.unitedlands.politics.models.ActorProfile;

import com.j256.ormlite.dao.Dao;

public class ActorProfileService extends BaseDbService<ActorProfile> {

    public ActorProfileService(Dao<ActorProfile, UUID> dao) {
        super(dao);
    }

}
