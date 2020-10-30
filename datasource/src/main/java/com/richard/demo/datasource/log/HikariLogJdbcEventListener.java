package com.richard.demo.datasource.log;

import com.p6spy.engine.common.PreparedStatementInformation;
import com.p6spy.engine.common.ResultSetInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.SimpleJdbcEventListener;
import com.richard.demo.basic.util.StringUtil;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.IntStream;

public class HikariLogJdbcEventListener extends SimpleJdbcEventListener {

    public void onAfterAnyExecute(StatementInformation statementInformation, long timeElapsedNanos, SQLException e) {
        String sql = statementInformation.getSqlWithValues();
        if (!StringUtils.hasLength(sql)) {
            return;
        }
        SqlLogInfo logInfo = getSqlLogInfo(statementInformation, sql).orElse(null);
        if (logInfo != null) {
            return;
        }
        logInfo = new SqlLogInfo();
        logInfo.setSql(sql);
        logInfo.setConnectionElapsedNanos(statementInformation.getConnectionInformation().getTimeToGetConnectionNs());
        logInfo.setSqlElapsedNanos(timeElapsedNanos);
        if (e != null) {
            logInfo.setError(Boolean.TRUE);
            logInfo.setErrorMsg(String.format("SQLState: %s; ErrorCode: %d; Message: %s", e.getSQLState(), e.getErrorCode(), e.getMessage()));
        }
        getSqlConnection(statementInformation).setSqlLogInfo(hashOf(sql), logInfo);
    }

    private HikariLogConnection getSqlConnection(StatementInformation statementInformation) {
        return (HikariLogConnection) statementInformation.getConnectionInformation().getConnection();
    }

    private Optional<SqlLogInfo> getSqlLogInfo(StatementInformation statementInformation, String sql) {
        return getSqlConnection(statementInformation).getSqlLogInfo(hashOf(sql));
    }

    private Optional<SqlLogInfo> getSqlLogInfo(StatementInformation statementInformation) {
        return getSqlLogInfo(statementInformation, statementInformation.getSqlWithValues());
    }

    private String hashOf(String sql) {
        return StringUtil.toHashString(sql);
    }

//    public void onAfterAnyAddBatch(StatementInformation statementInformation, long timeElapsedNanos, SQLException e) {
//    }

    //No need to override
//    public void onAfterExecute(PreparedStatementInformation statementInformation, long timeElapsedNanos, SQLException e) {
//        this.onAfterAnyExecute(statementInformation, timeElapsedNanos, e);
//    }
//
//    public void onAfterExecute(StatementInformation statementInformation, long timeElapsedNanos, String sql, SQLException e) {
//        this.onAfterAnyExecute(statementInformation, timeElapsedNanos, e);
//    }

    public void onAfterExecuteBatch(StatementInformation statementInformation, long timeElapsedNanos, int[] updateCounts, SQLException e) {
        this.onAfterAnyExecute(statementInformation, timeElapsedNanos, e);
        int sum = IntStream.of(updateCounts).sum();
        getSqlLogInfo(statementInformation).ifPresent(l -> {
            l.setBatch(Boolean.TRUE);
            l.setUpdateRows(sum);
        });
    }

    public void onAfterExecuteUpdate(PreparedStatementInformation statementInformation, long timeElapsedNanos, int rowCount, SQLException e) {
        this.onAfterAnyExecute(statementInformation, timeElapsedNanos, e);
        getSqlLogInfo(statementInformation).ifPresent(l -> l.setUpdateRows(rowCount));
    }

    public void onAfterExecuteUpdate(StatementInformation statementInformation, long timeElapsedNanos, String sql, int rowCount, SQLException e) {
        this.onAfterAnyExecute(statementInformation, timeElapsedNanos, e);
        getSqlLogInfo(statementInformation).ifPresent(l -> l.setUpdateRows(rowCount));
    }

    public void onAfterResultSetClose(ResultSetInformation resultSetInformation, SQLException e) {
        getSqlLogInfo(resultSetInformation.getStatementInformation()).ifPresent(l -> l.setUpdateRows(resultSetInformation.getCurrRow() + 1));
    }

    //No need to override
//    public void onAfterExecuteQuery(PreparedStatementInformation statementInformation, long timeElapsedNanos, SQLException e) {
//        this.onAfterAnyExecute(statementInformation, timeElapsedNanos, e);
//    }
//
//    public void onAfterExecuteQuery(StatementInformation statementInformation, long timeElapsedNanos, String sql, SQLException e) {
//        this.onAfterAnyExecute(statementInformation, timeElapsedNanos, e);
//    }

//    public void onAfterAddBatch(PreparedStatementInformation statementInformation, long timeElapsedNanos, SQLException e) {
//        this.onAfterAnyAddBatch(statementInformation, timeElapsedNanos, e);
//    }
//
//    public void onAfterAddBatch(StatementInformation statementInformation, long timeElapsedNanos, String sql, SQLException e) {
//        this.onAfterAnyAddBatch(statementInformation, timeElapsedNanos, e);
//    }
}
