package com.lacrima.lacrimademo.explore.domain;

import jakarta.persistence.*;

@Entity
@Table (name = "exploration_state")
public class ExplorationState {
    @Id
    @Column(name = "character_id")
    private Long characterId;

    // @Column(name = "map_id", nullable = false)
    // private Long mapId;

    private int ap;
    private int posX;
    private int posY;

    private Integer startX;
    private Integer startY;

    @Column(name = "open_step_acc", nullable = false)
    private int openStepAcc;

    protected ExplorationState() {}

    public ExplorationState(Long characterId, int ap, int posX, int posY) {
        this.characterId = characterId;
        this.ap = ap;
        this.posX = posX;
        this.posY = posY;
        this.startX = 0;
        this.startY = 0;
        this.openStepAcc = 3;
    }

    public Long getCharacterId() { return this.characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }
    public int getAp() { return this.ap; }
    public void setAp(int ap) { this.ap = ap; }
    public int getPosX() { return this.posX; }
    public void setPosX(int posX) { this.posX = posX; }
    public int getPosY() { return this.posY; }
    public void setPosY(int posY) { this.posY = posY; }
    public Integer getStartX() { return this.startX; }
    public void setStartX(Integer startX) { this.startX = startX; }
    public Integer getStartY() { return this.startY; }
    public void setStartY(Integer startY) { this.startY = startY; }
    public int getOpenStepAcc() { return this.openStepAcc; }
    public void setOpenStepAcc(int openStepAcc) { this.openStepAcc = openStepAcc; }
}
