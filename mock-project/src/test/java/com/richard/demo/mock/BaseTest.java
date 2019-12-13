package com.richard.demo.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.IHookCallBack;
import org.testng.ITestResult;

@SpringBootTest(classes = BaseTest.TestConfiguration.class, properties = {"spring.profiles.active=test"})
@ContextConfiguration(classes = BaseTest.TestConfiguration.class)
@Slf4j
public class BaseTest extends AbstractTestNGSpringContextTests {

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        try {
            super.run(callBack, testResult);
        } finally {
            if (testResult != null) {
                log.info(String.format("test case: %s, %d", testResult.getName(), System.currentTimeMillis() - testResult.getStartMillis()));
            }
        }
    }

    @Configuration
    @ComponentScan(basePackages = "com.richard.demo.mock")
    static class TestConfiguration {

    }
}
