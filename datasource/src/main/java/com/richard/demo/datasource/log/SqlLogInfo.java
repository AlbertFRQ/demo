package com.richard.demo.datasource.log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class SqlLogInfo implements java.io.Serializable {

    private static final long serialVersionUID = 7243020523511664559L;

    private String requestId;
    private String sql;
    private String dbUrl;
    private Integer updateRows;
    private Integer foundRows;
    private Boolean batch = Boolean.FALSE;
    private Boolean error = Boolean.FALSE;
    private String errorMsg;
    private Long connectionElapsedNanos = 0L;
    private Long sqlElapsedNanos = 0L;

    @JsonIgnore
    public int length() {
        return sql == null ? 0 : sql.length();
    }
}
