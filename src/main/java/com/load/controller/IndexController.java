package com.load.controller;

import com.load.entity.User;
import com.load.mapper.UserMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URL;
import java.util.List;

@RestController
public class IndexController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("list")
    public List<User> list(){
        return userMapper.list();
    }

    @GetMapping("index")
    public JsonRes index(){
        JsonRes jsonRes = new JsonRes();
        jsonRes.setCode(200);
        jsonRes.setData(true);
        jsonRes.setMsg("mac");
        return jsonRes;
    }


    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 1000; i++) {
            read(i);
        }
    }

    private static void read(int i) throws IOException {
        URL url = new URL("http://img.zcool.cn/community/018299554245910000019ae998f74d.jpg");
        InputStream in = url.openStream();
        FileOutputStream out = new FileOutputStream("/Users/lee/test/test"+i+".jpg");
        byte[] buff = new byte[1024*8];
        int len = -1;
        while ((len=in.read(buff, 0, buff.length)) != -1){
            out.write(buff, 0, len);
        }
        in.close();
        out.close();
    }

    @Data
    private class JsonRes {
        private int code;
        private String msg;
        private Object data;
    }
}
