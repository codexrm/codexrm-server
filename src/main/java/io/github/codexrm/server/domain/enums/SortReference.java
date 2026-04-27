package io.github.codexrm.server.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SortReference {

    idDesc("id,desc"),
    idAsc("id,asc"),
    titleDesc("title,desc"),
    titleAsc("title,asc"),
    yearAsc("year,asc"),
    yearDesc("year,desc"),
    monthAsc("month,asc"),
    monthDesc("month,desc"),
    noteDesc("note,desc"),
    noteAsc("note,asc");

    private final String description;

    SortReference(String description) {
        this.description = description;
    }

    @JsonCreator
    public static SortReference fromValue(String value) {
        for (SortReference sort : SortReference.values()) {
            if (sort.description.equalsIgnoreCase(value)) {
                return sort;
            }
        }
        throw new IllegalArgumentException("Invalid sort value: " + value);
    }

    @Override
    public String toString() {
        return description;
    }
}
