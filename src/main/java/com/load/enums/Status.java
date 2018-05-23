package com.load.enums;

import com.load.common.BaseEnum;

public enum Status implements BaseEnum {

    DELETE("删除"),NORMAL("正常");

    private String name;

    Status(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    @Override
    public int getValue(){
        return ordinal();
    }
}
