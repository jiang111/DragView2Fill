package com.jyuesong.dragview2fill;

import java.io.Serializable;

/**
 * Created by jiang on 29/01/2018.
 */
public class FastModel implements Serializable{

    private int type;
    private int count;
    private boolean isAdding;

    public FastModel() {
    }

    public FastModel(int type, int count) {
        this.type = type;
        this.count = count;
    }

    public FastModel(boolean isAdding) {
        this.isAdding = isAdding;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isAdding() {
        return isAdding;
    }

    public void setAdding(boolean adding) {
        isAdding = adding;
    }
}
