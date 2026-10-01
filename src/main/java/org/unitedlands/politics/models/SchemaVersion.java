package org.unitedlands.politics.models;

import org.unitedlands.libs.ormlite.field.DatabaseField;
import org.unitedlands.libs.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "schema_version")
public class SchemaVersion {
    @DatabaseField(id = true)
    private int id = 1;

    @DatabaseField
    private int version;

    public SchemaVersion() {
    }

    public SchemaVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}