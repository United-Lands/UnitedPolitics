package org.unitedlands.politics.models;

import java.util.UUID;

import org.unitedlands.politics.classes.Identifiable;

import com.j256.ormlite.field.DatabaseField;

public class ReputationScoreEntry implements Identifiable {

    @DatabaseField(generatedId = true, width = 36, canBeNull = false)
    private UUID id;
    @DatabaseField(canBeNull = false)
    private Long timestamp;
    @DatabaseField(canBeNull = false, width = 36)
    private UUID observer;
    @DatabaseField(canBeNull = false, width = 36)
    private UUID subject;
    @DatabaseField(canBeNull = false, width = 64)
    private String key;
    @DatabaseField(width = 512, canBeNull = true)
    private String description;
    @DatabaseField(canBeNull = false)
    private double modifier;
    @DatabaseField(canBeNull = false)
    private double decayRate;
    @DatabaseField(canBeNull = false)
    private double lowCap;
    @DatabaseField(canBeNull = false)
    private double highCap;

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

    public UUID getObserver() {
        return observer;
    }

    public void setObserver(UUID subject) {
        this.observer = subject;
    }

    public UUID getSubject() {
        return subject;
    }

    public void setSubject(UUID target) {
        this.subject = target;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getModifier() {
        return modifier;
    }

    public void setModifier(double modifier) {
        this.modifier = Math.max(this.lowCap, Math.min(this.highCap, modifier));
    }

    public double getDecayRate() {
        return decayRate;
    }

    public void setDecayRate(double decayRate) {
        this.decayRate = decayRate;
    }

    public double getLowCap() {
        return lowCap;
    }

    public void setLowCap(double lowCap) {
        this.lowCap = lowCap;
    }

    public double getHighCap() {
        return highCap;
    }

    public void setHighCap(double highCap) {
        this.highCap = highCap;
    }

}
