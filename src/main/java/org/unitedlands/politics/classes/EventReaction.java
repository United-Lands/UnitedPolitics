package org.unitedlands.politics.classes;

public class EventReaction {
    private String eventKey;
    private String reactionReputationKey;
    private double amount;

    public EventReaction() {
    }

    public EventReaction(String eventKey, String reactionReputationKey, double amount) {
        this.eventKey = eventKey;
        this.reactionReputationKey = reactionReputationKey;
        this.amount = amount;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getReactionReputationKey() {
        return reactionReputationKey;
    }

    public void setReactionReputationKey(String reactionReputationKey) {
        this.reactionReputationKey = reactionReputationKey;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventKey == null) ? 0 : eventKey.hashCode());
        result = prime * result + ((reactionReputationKey == null) ? 0 : reactionReputationKey.hashCode());
        long temp;
        temp = Double.doubleToLongBits(amount);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        EventReaction other = (EventReaction) obj;
        if (eventKey == null) {
            if (other.eventKey != null)
                return false;
        } else if (!eventKey.equals(other.eventKey))
            return false;
        if (reactionReputationKey == null) {
            if (other.reactionReputationKey != null)
                return false;
        } else if (!reactionReputationKey.equals(other.reactionReputationKey))
            return false;
        if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount))
            return false;
        return true;
    }

}
