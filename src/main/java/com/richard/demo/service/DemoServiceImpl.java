package com.richard.demo.service;

import com.richard.demo.entity.Demo;
import com.richard.demo.repository.BaseDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemoServiceImpl implements DemoService {
    @Autowired
    private BaseDaoImpl<Demo> baseDao;

    @Override
    public Integer batchSave(List<Demo> list) {
        return baseDao.batchSave(list);
    }

    @Override
    public Integer batchUpdate(List<Demo> list) {
        return baseDao.batchSave(list);
    }
}
