package org.unitedlands.politics.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.unitedlands.politics.classes.EventReaction;
import org.unitedlands.politics.classes.Identifiable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class ActorProfile implements Identifiable {

    @DatabaseField(id = true, width = 36, canBeNull = false)
    private UUID id;

    @DatabaseField(canBeNull = false)
    private Long timestamp;

    @DatabaseField
    private Boolean enabled = true;

    // -----------------------------------------------------------
    // Events that may influence this actor's opinion
    // -----------------------------------------------------------

    @DatabaseField
    private Boolean watchEvents = false;

    @DatabaseField(canBeNull = true, dataType = DataType.LONG_STRING)
    private String eventReactionsSerialized;

    private transient List<EventReaction> eventReactions;

    // -----------------------------------------------------------
    // Rivals and partners that might impact this actor's opinion
    // -----------------------------------------------------------

    // Will this actor react to players interacting with their rivals?
    @DatabaseField
    private Boolean watchRivals = false;

    // Will this actor react to players interacting with their partners?
    @DatabaseField
    private Boolean watchPartners = false;

    @DatabaseField(canBeNull = true, dataType = DataType.LONG_STRING)
    private String rivalsSerialized;
    @DatabaseField(canBeNull = true, dataType = DataType.LONG_STRING)
    private String partnersSerialized;

    private transient Set<UUID> rivals;
    private transient Set<UUID> partners;

    // -----------------------------------------------------------
    // Optional UnitedDungeons integrations
    // -----------------------------------------------------------

    @DatabaseField(canBeNull = true, dataType = DataType.LONG_STRING)
    private String hostileDungeonsSerialized;
    @DatabaseField(canBeNull = true, dataType = DataType.LONG_STRING)
    private String friendlyDungeonsSerialized;

    private transient Set<UUID> hostileDungeons;
    private transient Set<UUID> friendlyDungeons;

    public ActorProfile() {
    }

    public ActorProfile(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventReactionsSerialized() {
        return eventReactionsSerialized;
    }

    public void setEventReactionsSerialized(String eventReactionsSerialized) {
        this.eventReactionsSerialized = eventReactionsSerialized;
        this.eventReactions = null;
    }

    public List<EventReaction> getEventReactions() {
        if (eventReactions == null && eventReactionsSerialized != null && eventReactionsSerialized != "") {
            eventReactions = new ArrayList<>();
            var reactions = eventReactionsSerialized.split(";");
            for (var reaction : reactions) {
                var values = reaction.split("#");
                if (values.length != 3)
                    continue;
                eventReactions.add(new EventReaction(values[0], values[1], Double.parseDouble(values[2])));
            }

        }
        return eventReactions;
    }

    public void setEventReactions(List<EventReaction> eventReactions) {
        this.eventReactions = eventReactions;
        if (eventReactions.isEmpty()) {
            this.eventReactionsSerialized = null;
        } else {
            List<String> reactionStrings = new ArrayList<>();
            for (var reaction : eventReactions) {
                var reactionString = reaction.getEventKey() + "#" + reaction.getReactionReputationKey() + "#"
                        + reaction.getAmount();
                reactionStrings.add(reactionString);
            }
            this.eventReactionsSerialized = String.join(";", reactionStrings);
        }
    }

    public String getRivalsSerialized() {
        return rivalsSerialized;
    }

    public void setRivalsSerialized(String rivalsSerialized) {
        this.rivalsSerialized = rivalsSerialized;
        this.rivals = null;
    }

    public String getPartnersSerialized() {
        return partnersSerialized;
    }

    public void setPartnersSerialized(String partnersSerialized) {
        this.partnersSerialized = partnersSerialized;
        this.partners = null;
    }

    public Set<UUID> getRivals() {
        if (rivals == null && rivalsSerialized != null && rivalsSerialized != "") {
            rivals = Arrays.stream(rivalsSerialized.split("#"))
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
        }
        return rivals;
    }

    public void setRivals(Set<UUID> rivals) {
        this.rivals = rivals;
        if (rivals.isEmpty())
            this.rivalsSerialized = null;
        else
            this.rivalsSerialized = rivals.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining("#"));
    }

    public Set<UUID> getPartners() {
        if (partners == null && partnersSerialized != null && partnersSerialized != "") {
            partners = Arrays.stream(partnersSerialized.split("#"))
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
        }
        return partners;
    }

    public void setPartners(Set<UUID> partners) {
        this.partners = partners;
        if (partners.isEmpty())
            this.partnersSerialized = null;
        else
            this.partnersSerialized = partners.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining("#"));
    }

    public String getHostileDungeonsSerialized() {
        return hostileDungeonsSerialized;
    }

    public void setHostileDungeonsSerialized(String hostileDungeonsSerialized) {
        this.hostileDungeonsSerialized = hostileDungeonsSerialized;
        this.hostileDungeons = null;
    }

    public String getFriendlyDungeonsSerialized() {
        return friendlyDungeonsSerialized;
    }

    public void setFriendlyDungeonsSerialized(String friendlyDungeonsSerialized) {
        this.friendlyDungeonsSerialized = friendlyDungeonsSerialized;
        this.friendlyDungeons = null;
    }

    public Set<UUID> getHostileDungeons() {
        if (hostileDungeons == null && hostileDungeonsSerialized != null && hostileDungeonsSerialized != "") {
            hostileDungeons = Arrays.stream(hostileDungeonsSerialized.split("#"))
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
        }
        return hostileDungeons;
    }

    public void setHostileDungeons(Set<UUID> hostileDungeons) {
        this.hostileDungeons = hostileDungeons;
        if (hostileDungeons.isEmpty())
            this.hostileDungeonsSerialized = null;
        else
            this.hostileDungeonsSerialized = hostileDungeons.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining("#"));
    }

    public Set<UUID> getFriendlyDungeons() {
        if (friendlyDungeons == null && friendlyDungeonsSerialized != null && friendlyDungeonsSerialized != "") {
            friendlyDungeons = Arrays.stream(friendlyDungeonsSerialized.split("#"))
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
        }
        return friendlyDungeons;
    }

    public void setFriendlyDungeons(Set<UUID> friendlyDungeons) {
        this.friendlyDungeons = friendlyDungeons;
        if (friendlyDungeons.isEmpty())
            this.friendlyDungeonsSerialized = null;
        else
            this.friendlyDungeonsSerialized = friendlyDungeons.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining("#"));
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
        ActorProfile other = (ActorProfile) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
