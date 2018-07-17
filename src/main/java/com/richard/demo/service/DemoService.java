package com.richard.demo.service;

import com.richard.demo.entity.Demo;

import java.util.List;

public interface DemoService {
    Integer batchSave(List<Demo> list);

    Integer batchUpdate(List<Demo> list);
}
