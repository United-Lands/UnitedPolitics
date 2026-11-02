package org.unitedlands.politics.services;

import java.util.UUID;

import org.unitedlands.libs.ormlite.dao.Dao;
import org.unitedlands.politics.models.DiplomacyRecord;

public class DiplomacyRecordService extends BaseDbService<DiplomacyRecord>  {

    public DiplomacyRecordService(Dao<DiplomacyRecord, UUID> dao) {
        super(dao);
    }

}
