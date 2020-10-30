package com.richard.demo.jpa.repository;

import java.util.Collection;

public interface RealBatchInsertRepository<T, ID> {

    void batchInsert(Collection<T> entities);
}
