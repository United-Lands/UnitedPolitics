package org.unitedlands.politics.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.unitedlands.libs.ormlite.field.DataType;
import org.unitedlands.libs.ormlite.field.DatabaseField;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.unitedlands.classes.db.Identifiable;
import org.unitedlands.unitedlands.classes.Citizen;
import org.unitedlands.unitedlands.classes.Country;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.unitedlands.utils.SerializationUtils;

public class DiplomacyRecord implements Identifiable {

    @DatabaseField(id = true, width = 36, canBeNull = false)
    private UUID id;


    @DatabaseField(dataType = DataType.LONG_STRING, columnName = "embargoes_serialized")
    private String embargoesSerialized;

    @DatabaseField(dataType = DataType.LONG_STRING, columnName = "ambassadors_serialized")
    private String ambassadorsSerialized;

    private transient Set<Country> embargoes;
    private transient Map<Citizen, Country> ambassadors;

    public DiplomacyRecord() {
    }

    public DiplomacyRecord(UUID id) {
        this.id = id;
    }

    @Override 
    public UUID getUuid() {
        return id;
    }

    @Override 
    public void setUuid(UUID id) {
        this.id = id;
    }


    public String getEmbargoesSerialized() {
        return embargoesSerialized;
    }

    public void setEmbargoesSerialized(String embargoesSerialized) {
        this.embargoesSerialized = embargoesSerialized;
    }

    public Set<Country> getEmbargoes() {
        if (embargoes == null) {
            embargoes = SerializationUtils.deserializeUuidListToSet(embargoesSerialized, 
                UnitedLandsDataManager.instance()::getCountry);
        }
        return embargoes;
    }

    public void setEmbargoes(Set<Country> embargoes) {
        this.embargoes = embargoes;
        this.embargoesSerialized = SerializationUtils.serializeIdentifiableList(embargoes);
    }

    public boolean isOnEmbargo(Country country) {
        return getEmbargoes().contains(country);
    }

    public void addEmbargo(Country country) {
        var e = new HashSet<>(getEmbargoes());
        e.add(country);
        setEmbargoes(e);
    }

    public void removeEmbargo(Country country) {
        var e = new HashSet<>(getEmbargoes());
        e.remove(country);
        setEmbargoes(e);
    }

    public String getAmbassadorsSerialized() {
        return ambassadorsSerialized;
    }

    public void setAmbassadorsSerialized(String ambassadorsSerialized) {
        this.ambassadorsSerialized = ambassadorsSerialized;
    }
    
    public Map<Citizen, Country> getAmbassadors() {
        if (ambassadors == null) {
            ambassadors = SerializationUtils.deserializeUuidMap(ambassadorsSerialized, 
                UnitedLandsDataManager.instance()::getCitizen,
                UnitedLandsDataManager.instance()::getCountry);
        }
        return ambassadors;
    }

    public void setAmbassadors(Map<Citizen, Country> ambassadors) {
        this.ambassadors = ambassadors;
        this.ambassadorsSerialized = SerializationUtils.serializeIdentifiableMap(ambassadors);
    }

    public boolean isAmbassador(Citizen citizen) {
        return getAmbassadors().containsKey(citizen);
    }

    public boolean hasAmbassadorIn(Country targeCountry) {
        return getAmbassadors().values().stream().anyMatch(c -> c.equals(targeCountry));
    }

    public Country getAmbassdorCountry(Citizen citizen) {
        return getAmbassadors().get(citizen);
    }

    public void addAmbassador(Citizen citizen, Country country) {
        var a = new HashMap<>(getAmbassadors());
        a.put(citizen, country);
        setAmbassadors(a);
    }

    public void removeAmbassador(Citizen citizen) {
        var a = new HashMap<>(getAmbassadors());
        a.remove(citizen);
        setAmbassadors(a);
    }

    public void save() {
        UnitedPolitics.instance().getDatabaseManager().getDiplomacyRecordService().createOrUpdateAsync(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiplomacyRecord other = (DiplomacyRecord) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }


}
