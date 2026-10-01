package org.unitedlands.politics.classes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
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

    private transient Set<INationWrapper> members;

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

    public Set<INationWrapper> getMembers() {
        if (members == null) {
            members = SerializationUtils.deserializeUuidListToSet(members_serialized,
                    UnitedPolitics.instance().getGeopolWrapper()::getNation);
        }
        return members;
    }

    public boolean hasMember(INationWrapper nation) {
        return members.contains(nation);
    }

    public void setMembers(Set<INationWrapper> members) {
        this.members = members;
        this.members_serialized = members.stream()
                .map(c -> c.getUUID().toString())
                .collect(Collectors.joining("#"));
    }

    public void addMember(INationWrapper nation) {
        var t = new HashSet<>(getMembers());
        t.add(nation);
        setMembers(t);
    }

    public void removeMember(INationWrapper nation) {
        var t = new HashSet<>(getMembers());
        t.remove(nation);
        setMembers(t);
    }

}
