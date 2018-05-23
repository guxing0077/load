package com.load.controller;

import com.load.entity.User;
import com.load.service.UserService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private UserService userService;

    @GetMapping("list")
    public List<User> list(){
        logger.info("this is info log");
        logger.debug("this is debug log");
        logger.error("this is error log");
        if(true) throw new RuntimeException("for test");
        return userService.list();
    }

    @GetMapping("health")
    public JsonRes index(){
        JsonRes jsonRes = new JsonRes();
        jsonRes.setCode(200);
        jsonRes.setData(true);
        jsonRes.setMsg("mac");
        return jsonRes;
    }

    @Data
    private class JsonRes {
        private int code;
        private String msg;
        private Object data;
    }
}
