package org.unitedlands.politics.classes;

import java.util.UUID;

public interface Identifiable {
    UUID getId();
    void setId(UUID id);
}