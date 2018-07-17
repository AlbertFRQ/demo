package com.richard.demo.repository;

import java.util.List;

public interface BaseDao<T> {
    Integer batchSave(List<T> list);

    Integer batchUpdate(List<T> list);
}
