package com.tanzhou.dto;

public class GithubUser {
    private String name;
    private Long id;
    private String bi0;

    @Override
    public String toString() {
        return "GithubUser{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", bi0='" + bi0 + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBi0() {
        return bi0;
    }

    public void setBi0(String bi0) {
        this.bi0 = bi0;
    }
}
