package com.load.entity;

import com.load.enums.Status;
import lombok.Data;

@Data
public class User {
    private Integer id;
    private String name;
    private Status status;
}
