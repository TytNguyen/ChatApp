package com.example.chatapp.Model;

public class Group {
    private String name, image, search;
    private String description;

    public Group() {
    }

    public Group(String name, String image, String description, String search) {
        this.name = name;
        this.image = image;
        this.description = description;
        this.search = search;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
