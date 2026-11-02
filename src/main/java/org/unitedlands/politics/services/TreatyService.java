package org.unitedlands.politics.services;

import java.util.UUID;

import org.unitedlands.libs.ormlite.dao.Dao;
import org.unitedlands.politics.models.Treaty;

public class TreatyService extends BaseDbService<Treaty> {

    public TreatyService(Dao<Treaty, UUID> dao) {
        super(dao);
    }

}
