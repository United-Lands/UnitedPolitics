package org.unitedlands.politics.services;

import java.util.UUID;

import org.unitedlands.politics.models.ReputationScoreEntry;

import com.j256.ormlite.dao.Dao;

public class ReputationScoreEntryService extends BaseDbService<ReputationScoreEntry> {

    public ReputationScoreEntryService(Dao<ReputationScoreEntry, UUID> dao) {
        super(dao);
    }

}
