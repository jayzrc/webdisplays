/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.utilities;

import com.google.gson.annotations.SerializedName;

public class TypeData {
    public enum Action {
        @SerializedName("i")
        INVALID,

        @SerializedName("p")
        PRESS,

        @SerializedName("r")
        RELEASE,

        @SerializedName("t")
        TYPE
    }

    private final Action a;
    private final int k;
    private final int m;

    public TypeData() {
        a = Action.INVALID;
        k = 0;
        m = 0;
    }

    public TypeData(Action action, int code, int modifier) {
        a = action;
        k = code;
        m = modifier;
    }

    public Action getAction() {
        return a;
    }

    public int getKeyCode() {
        return k;
    }

    public int getModifier() {
        return m;
    }
}
