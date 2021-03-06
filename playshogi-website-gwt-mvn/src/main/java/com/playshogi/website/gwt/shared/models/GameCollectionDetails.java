package com.playshogi.website.gwt.shared.models;

import java.io.Serializable;

public class GameCollectionDetails implements Serializable {
    private String id;
    private String name;
    private String description;
    private String visibility;

    public GameCollectionDetails() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(final String visibility) {
        this.visibility = visibility;
    }

    @Override
    public String toString() {
        return "GameCollectionDetails{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", visibility='" + visibility + '\'' +
                '}';
    }
}
