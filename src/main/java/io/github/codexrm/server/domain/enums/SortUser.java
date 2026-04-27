package io.github.codexrm.server.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SortUser {

    idDesc("id,desc"),
    idAsc("id,asc"),
    nameDesc("name,desc"),
    nameAsc("name,asc"),
    lastNameDesc("lastName,desc"),
    lastNameAsc("lastName,asc"),
    emailDesc("email,desc"),
    emailAsc("email,asc"),
    enabledDesc("enabled,desc"),
    enabledAsc("enabled,asc"),
    usernameDesc("username,desc"),
    usernameAsc("username,asc"),
    passwordDesc("password,desc"),
    passwordAsc("password,asc");

    private final String description;

    SortUser(String description) {
        this.description = description;
    }

    @JsonCreator
    public static SortUser fromValue(String value) {
        for (SortUser sort : SortUser.values()) {
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
