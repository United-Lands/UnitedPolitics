package org.unitedlands.politics.classes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.unitedlands.unitedlands.classes.Country;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.unitedlands.utils.SerializationUtils;

import org.unitedlands.libs.ormlite.field.DataType;
import org.unitedlands.libs.ormlite.field.DatabaseField;

public class Treaty implements Identifiable {

    @DatabaseField(id = true, width = 36, canBeNull = false)
    private UUID id;

    @DatabaseField(canBeNull = false)
    private Long timestamp;

    @DatabaseField(dataType = DataType.ENUM_STRING, canBeNull = false)
    private TreatyType type;

    @DatabaseField(canBeNull = false, width = 128)
    private String name;

    @DatabaseField(canBeNull = false, dataType = DataType.LONG_STRING)
    private String members_serialized;

    private transient Set<Country> members;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public TreatyType getType() {
        return type;
    }

    public void setType(TreatyType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getCleanName() {
        return name.replace("_", " ");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMembers_serialized() {
        return members_serialized;
    }

    public void setMembers_serialized(String members_serialized) {
        this.members_serialized = members_serialized;
    }

    public Set<Country> getMembers() {
        if (members == null) {
            members = SerializationUtils.deserializeUuidListToSet(members_serialized,
                    UnitedLandsDataManager.instance()::getCountry);
        }
        return members;
    }

    public boolean hasMember(Country country) {
        return getMembers().contains(country);
    }

    public void setMembers(Set<Country> members) {
        this.members = members;
        this.members_serialized = members.stream()
                .map(c -> c.getUuid().toString())
                .collect(Collectors.joining("#"));
    }

    public void addMember(Country country) {
        var t = new HashSet<>(getMembers());
        t.add(country);
        setMembers(t);
    }

    public void removeMember(Country country) {
        var t = new HashSet<>(getMembers());
        t.remove(country);
        setMembers(t);
    }

}
