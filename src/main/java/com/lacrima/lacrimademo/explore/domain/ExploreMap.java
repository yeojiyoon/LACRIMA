package com.lacrima.lacrimademo.explore.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "explore_map")
public class ExploreMap {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // field01

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int width;

    @Column(nullable = false)
    private int height;

    @Column(length = 2000)
    private String explanation;

    protected ExploreMap() {}

    public ExploreMap(String code, String name, int width, int height, String explanation) {
        this.code = code;
        this.name = name;
        this.width = width;
        this.height = height;
        this.explanation = explanation;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getExplanation() { return explanation; }
}
