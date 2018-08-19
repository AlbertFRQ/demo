package com.richard.demo.controller;

import com.richard.demo.entity.Demo;
import com.richard.demo.service.DemoService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * localhost:8080/swagger-ui.html
 */
@RestController
public class DemoApi {
    @Autowired
    private DemoService demoService;

    @ApiOperation(value = "batch save test trying to solve injection issue of class.", tags = "Demo")
    @RequestMapping(value = "/api/batch/save", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Integer batchSave() {
        List<Demo> list = new ArrayList<>();
        Demo demo = new Demo();
        Demo demo2 = new Demo();
        list.add(demo);
        list.add(demo2);
        return demoService.batchSave(list);
    }
}
