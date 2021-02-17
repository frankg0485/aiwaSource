package com.airoha.android.lib.peq;

import java.util.HashMap;
import java.util.Map;

public enum Rate {
    R32((short) 0),
    R441((short) 1),
    R48((short) 2),
    R16((short) 3),
    R8((short) 4);

    private short value;
    private static Map sMap = new HashMap();

    private Rate(short value){
        this.value = value;
    }

    static {
        for(Rate rate : Rate.values()) {
            sMap.put(rate.value, rate);
        }
    }

    public static Rate valueOf(short key) {
        return (Rate) sMap.get(key);
    }

    public short getValue() {
        return this.value;
    }
}
