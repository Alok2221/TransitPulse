package com.transitpulse.dto;

import com.transitpulse.domain.TransportMode;

public class ModeOptionDto {

    private TransportMode mode;
    private String label;
    private double costPln;
    private double durationMinutes;
    private double co2Kg;
    private double score;
    private int rank;
    private boolean recommended;
    private boolean feasible;
    private String note;

    public TransportMode getMode() {
        return mode;
    }

    public void setMode(TransportMode mode) {
        this.mode = mode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getCostPln() {
        return costPln;
    }

    public void setCostPln(double costPln) {
        this.costPln = costPln;
    }

    public double getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(double durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getCo2Kg() {
        return co2Kg;
    }

    public void setCo2Kg(double co2Kg) {
        this.co2Kg = co2Kg;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public void setFeasible(boolean feasible) {
        this.feasible = feasible;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
