package com.richard.demo.mock.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockServiceImpl implements MockService {

    @Value("${testng.fibonacci.limit:12}")
    private Integer envLimit;

    @Override
    public List<Integer> getFibonacci(Integer limit) {
        limit = envLimit != null ? envLimit : limit;
        List<Integer> list = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            if (i < 2) {
                list.add(1);
                continue;
            }
            list.add(list.get(i - 1) + list.get(i - 2));
        }
        return list;
    }
}
