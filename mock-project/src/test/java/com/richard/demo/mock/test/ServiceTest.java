package com.richard.demo.mock.test;

import com.richard.demo.mock.BaseTest;
import com.richard.demo.mock.service.MockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class ServiceTest extends BaseTest {

    @Autowired
    private MockService mockService;

    @Test
    public void testFib() {
        System.out.println(mockService.getFibonacci(5));
    }
}
