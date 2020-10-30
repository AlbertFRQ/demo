package com.richard.demo.mock.test;

import com.richard.demo.mock.BaseTest;
import com.richard.demo.mock.service.MockService;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Set;

public class ServiceTest extends BaseTest {

    @Autowired
    private MockService mockService;

    @Test
    public void testFib() {
        String a = "aa";
        Set<String> set = Sets.newSet(a);
        a = "aaa";
        System.out.println(set);
    }
}
