package org.unitedlands.politics.services;

import java.util.UUID;

import org.unitedlands.politics.classes.Treaty;

import org.unitedlands.libs.ormlite.dao.Dao;

public class TreatyService extends BaseDbService<Treaty> {

    public TreatyService(Dao<Treaty, UUID> dao) {
        super(dao);
    }

}
