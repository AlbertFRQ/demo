package com.richard.demo.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public abstract class BaseDaoImpl<T> implements BaseDao<T> {
    @Override
    public Integer batchSave(List<T> list) {
        int count = 0;
        for (; count < list.size(); count++) {
            if (count % 50 == 0) {
                System.out.println("Count: --------- " + count);
            }
        }
        return count;
    }

    @Override
    public Integer batchUpdate(List<T> list) {
        int count = 0;
        for (; count < list.size(); count++) {
            if (count % 50 == 0) {
                System.out.println("Count: --------- " + count);
            }
        }
        return count;
    }
}
