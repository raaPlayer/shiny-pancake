package com.raa.reggie.common;

import java.util.ArrayList;
import java.util.List;

public class BaseContext {
    private static final ThreadLocal<List<Long>> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id, Long userRights){
//        threadLocal.set
        List<Long> data = new ArrayList<>();
        data.add(id);
        data.add(userRights);
        threadLocal.set(data);
    }

    public static Long getCurrentId(){
        return threadLocal.get().get(0);
    }

    public static Long getUserRights(){
        return threadLocal.get().get(1);
    }
}
