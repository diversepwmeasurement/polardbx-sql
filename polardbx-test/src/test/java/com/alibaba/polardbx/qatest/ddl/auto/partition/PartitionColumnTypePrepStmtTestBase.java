package com.alibaba.polardbx.qatest.ddl.auto.partition;

import com.alibaba.polardbx.common.exception.NotSupportException;
import com.alibaba.polardbx.qatest.util.JdbcUtil;
import com.alibaba.polardbx.qatest.validator.DataValidator;
import com.alibaba.polardbx.server.util.StringUtil;
import com.googlecode.protobuf.format.util.HexUtils;
import org.junit.Assert;
import org.junit.Ignore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenghui.lch
 */
@Ignore

public class PartitionColumnTypePrepStmtTestBase extends PartitionTestBase {

    public PartitionColumnTypePrepStmtTestBase.TestParameter parameter;
    protected boolean testQueryByPrepStmt = false;
    protected boolean testInsertByPrepStmt = false;
    protected String testDbName;

    public PartitionColumnTypePrepStmtTestBase(PartitionColumnTypePrepStmtTestBase.TestParameter parameter) {
        this.parameter = parameter;
        this.testDbName = "part_col_type_db";
    }

    @Ignore
    public void testInsertAndSelect() throws SQLException {
        try {
            String dbName = testDbName;
            String createDbPolarx = String.format("create database if not exists %s mode='auto'", dbName);
            String createDbMysql = String.format("create database if not exists %s", dbName);

            String tblName = parameter.tblName;
            String dropTbl = String.format("drop table if exists %s", tblName);

            String colDefs = "";
            Boolean[] binaryFlag = new Boolean[parameter.partCols.length];
            for (int i = 0; i < parameter.partCols.length; i++) {
                if (i > 0) {
                    colDefs += ",";
                }
                String col = parameter.partCols[i];
                String dataType = parameter.partColDataTypes[i];
                colDefs += col + " " + dataType;
                if (dataType.toLowerCase().contains("binary")) {
                    binaryFlag[i] = true;
                } else {
                    binaryFlag[i] = false;
                }
            }
            String partDefs = "partition by ";

            String cols = String.join(",", parameter.partCols);
            String partColDefs = cols;
            if (this.parameter.partFuns != null && this.parameter.partFuns.length > 0) {
                partColDefs = String.format("%s(%s)", this.parameter.partFuns[0], cols);
            }
            partDefs += String.format("%s(%s) ", parameter.strategy, partColDefs);
            if (parameter.strategy.equalsIgnoreCase("key") || parameter.strategy.equalsIgnoreCase("hash")) {
                partDefs += String.format("partitions %s", parameter.bndVals[0]);
            } else if (parameter.strategy.equalsIgnoreCase("range") || parameter.strategy
                .equalsIgnoreCase("range columns")) {
                partDefs += "(";
                for (int i = 0; i < parameter.bndVals.length; i++) {
                    String bndVal = parameter.bndVals[i];
                    if (i > 0) {
                        partDefs += ",";
                    }
                    partDefs += String.format("partition p%s values less than %s", i, bndVal);
                }
                partDefs += ")";

            } else {
                partDefs += "(";
                for (int i = 0; i < parameter.bndVals.length; i++) {
                    String bndVal = parameter.bndVals[i];
                    if (i > 0) {
                        partDefs += ",";
                    }
                    partDefs += String.format("partition p%s values in %s", i, bndVal);
                }
                partDefs += ")";
            }

            String createTbl =
                String.format("create table if not exists %s (%s) %s", tblName, colDefs, partDefs);

            if (parameter.partCols.length > 1) {
                throw new NotSupportException("not support do the insert by prepare stmt for multi cols");
            }
            String valuesStr = String.join(",", parameter.insertValues);
            String insertSql = String.format("insert into %s(%s) values %s", tblName, cols, valuesStr);
            String[] dynamicVals = new String[parameter.insertValues.length];
            for (int i = 0; i < parameter.insertValues.length; i++) {
                dynamicVals[i] = "(?)";
            }
            String dynamicValsStr = String.join(",", dynamicVals);
            String insertPrepSql = String.format("insert into %s(%s) values %s", tblName, cols, dynamicValsStr);

            String partPredStr = "";
            String[] pointSelects = new String[parameter.selectValues.length];
            List<String> rngSelects = new ArrayList<>();
            for (int j = 0; j < parameter.selectValues.length; j++) {
                boolean isNullQuery = false;
                if (j > 0) {
                    partPredStr += "OR ";
                }
                String val = parameter.selectValues[j];
                String pred = String.format("((%s) = %s)", cols, val);
                if (parameter.partCols.length == 1 && val.contains("null")) {
                    pred = String.format("((%s) is null)", cols, val);
                    isNullQuery = true;
                }
                pointSelects[j] =
                    String.format("select %s from %s where %s order by %s", cols, tblName, pred, cols);

                if (!isNullQuery) {
                    String rngSelectSql = null;
                    String rngPred = null;

                    // range query for ">"
                    rngPred = String.format("((%s) > %s)", cols, val);
                    rngSelectSql =
                        String.format("select %s from %s where %s order by %s", cols, tblName, rngPred, cols);
                    rngSelects.add(rngSelectSql);

                    // range query for ">="
                    rngPred = String.format("((%s) >= %s)", cols, val);
                    rngSelectSql =
                        String.format("select %s from %s where %s order by %s", cols, tblName, rngPred, cols);
                    rngSelects.add(rngSelectSql);

                    // range query for "<"
                    rngPred = String.format("((%s) < %s)", cols, val);
                    rngSelectSql =
                        String.format("select %s from %s where %s order by %s", cols, tblName, rngPred, cols);
                    rngSelects.add(rngSelectSql);

                    // range query for "<="
                    rngPred = String.format("((%s) <= %s)", cols, val);
                    rngSelectSql =
                        String.format("select %s from %s where %s order by %s", cols, tblName, rngPred, cols);
                    rngSelects.add(rngSelectSql);
                }

                partPredStr += pred;
            }

            String pointSelectPattern = "";
            String pointSelectPrepSql = "";
            boolean isMultiCols = parameter.partCols.length > 1;
            if (isMultiCols) {
                String valRexExpr = "(";
                for (int i = 0; i < parameter.partCols.length; i++) {
                    if (i > 0) {
                        valRexExpr += ",";
                    }
                    valRexExpr += "?";
                }
                valRexExpr += ")";
                pointSelectPattern =
                    String.format("select %s from %s where (%s)=%s order by %s", cols, tblName, cols, valRexExpr, cols);
            } else {
                pointSelectPattern =
                    String.format("select %s from %s where %s=? order by %s", cols, tblName, cols, cols);
            }
            pointSelectPrepSql = pointSelectPattern;

            String selectSql =
                String.format("select %s from %s where %s order by %s", cols, tblName, partPredStr, cols);

            String fullScanSql =
                String.format("select %s from %s order by %s", cols, tblName, cols);

            if (parameter.rngQuerySortedValues != null && parameter.rngQuerySortedValues.length > 1) {
                for (int i = 0; i < parameter.rngQuerySortedValues.length - 1; i++) {
                    String rngSelectSql = null;
                    String rngPred = null;
                    String v1 = parameter.rngQuerySortedValues[i];
                    String v2 = parameter.rngQuerySortedValues[i + 1];
                    rngPred = String.format("((%s) >= %s) AND ((%s) < %s)", cols, v1, cols, v2);
                    rngSelectSql =
                        String.format("select %s from %s where %s order by %s", cols, tblName, rngPred, cols);
                    rngSelects.add(rngSelectSql);
                }
            }

            String[] prepareStmts = this.parameter.prepareStmts;
            String ddlPrepStmt = "";
            String insertPrepStmt = "";
            String selectPrepStmt = "";
            if (prepareStmts != null) {
                ddlPrepStmt = prepareStmts[0];
                insertPrepStmt = prepareStmts[1];
                selectPrepStmt = prepareStmts[2];
            }

            String castStr = this.parameter.toString();

            logSql(castStr, "createDb", createDbPolarx);
            JdbcUtil.executeUpdateSuccess(tddlConnection, createDbPolarx);
            JdbcUtil.executeUpdateSuccess(mysqlConnection, createDbMysql);

            Connection polarConn = getPolardbxConnection(testDbName);
            Connection mysqlConn = getMysqlConnection(testDbName);

            logSql(castStr, "drop", dropTbl);
            JdbcUtil.executeUpdateSuccess(polarConn, dropTbl);
            JdbcUtil.executeUpdateSuccess(mysqlConn, dropTbl);

            if (!StringUtil.isEmpty(ddlPrepStmt)) {
                logSql(castStr, "ddlPrepStmt", ddlPrepStmt);
                execPrepStmts(polarConn, ddlPrepStmt);
                execPrepStmts(mysqlConn, ddlPrepStmt);
            }

            logSql(castStr, "create", createTbl);
            JdbcUtil.executeUpdateSuccess(polarConn, createTbl);
            JdbcUtil.executeUpdateSuccess(mysqlConn, createTbl);

            if (!StringUtil.isEmpty(insertPrepStmt)) {
                logSql(castStr, "insertPrepStmt", insertPrepStmt);
                execPrepStmts(polarConn, insertPrepStmt);
                execPrepStmts(mysqlConn, insertPrepStmt);
            }

            logSql(castStr, "insert", insertSql);
            if (!testInsertByPrepStmt) {
                JdbcUtil.executeUpdateSuccess(polarConn, insertSql);
                JdbcUtil.executeUpdateSuccess(mysqlConn, insertSql);
            } else {
                List<Object> params = new ArrayList<>();
                List<String> rawValStr = new ArrayList<>();
                for (int i = 0; i < parameter.insertValues.length; i++) {
                    String rawInsertVal = parameter.insertValues[i];
                    boolean isNull = false;
                    if (rawInsertVal.contains("null")) {
                        isNull = true;
                    }
                    String insertVal = rawInsertVal.replace("(", "").replace(")", "").replace("'", "");
                    insertVal = insertVal.trim();
                    rawValStr.add(insertVal);
                    if (!isNull) {
                        if (binaryFlag[0]) {
                            byte[] bytes = insertVal.getBytes();
                            params.add(bytes);
                        } else {
                            params.add(insertVal);
                        }
                    } else {
                        params.add(null);
                    }

                }
                logSql(castStr, "prep-insert", insertPrepSql + ", param: " + String.join(",", rawValStr));
                execInsertPrepStmtsByParams(insertPrepSql, params, mysqlConn, polarConn);
            }

            if (!StringUtil.isEmpty(selectPrepStmt)) {
                logSql(castStr, "selectPrepStmt", selectPrepStmt);
                execPrepStmts(polarConn, selectPrepStmt);
                execPrepStmts(mysqlConn, selectPrepStmt);
            }

            try {
                logSql(castStr, "full-scan-select", fullScanSql);
                execPrepStmtsByParams(fullScanSql, new ArrayList<>(), mysqlConn, polarConn);
            } catch (Throwable ex) {
                Assert.fail(String.format("table data is diff:%s", fullScanSql));
                throw ex;
            }

            if (!testQueryByPrepStmt) {
                logSql(castStr, "select", selectSql);
                DataValidator validator = new DataValidator();
                validator.selectContentSameAssert(selectSql, new ArrayList<>(), mysqlConn, polarConn);

                for (int i = 0; i < pointSelects.length; i++) {
                    logSql(castStr, "point-select", pointSelects[i]);
                    validator
                        .selectContentSameAssert(pointSelects[i], new ArrayList<>(), mysqlConn, polarConn, true);
                }

                for (int i = 0; i < rngSelects.size(); i++) {
                    logSql(castStr, "range-select", rngSelects.get(i));
                    validator
                        .selectContentSameAssert(rngSelects.get(i), new ArrayList<>(), mysqlConn, polarConn,
                            true);
                }

            } else {
                for (int i = 0; i < parameter.selectValues.length; i++) {
                    String rawPointVal = parameter.selectValues[i];
                    boolean isNull = false;
                    if (rawPointVal.contains("null")) {
                        isNull = true;
                    }
                    String pointVal = rawPointVal.replace("(", "").replace(")", "").replace("'", "");
                    pointVal = pointVal.trim();

                    List<Object> params = new ArrayList<>();
                    if (!isNull) {
                        if (binaryFlag[0]) {
                            byte[] bytes = pointVal.getBytes();
                            params.add(bytes);
                        } else {
                            params.add(pointVal);
                        }
                    } else {
                        params.add(null);
                    }

                    logSql(castStr, "prep-point-select", pointSelectPrepSql + ", param: " + pointVal);
                    execPrepStmtsByParams(pointSelectPrepSql, params, mysqlConn, polarConn);
                }
            }

        } catch (Throwable ex) {
            Assert.fail(ex.getMessage());
        }
    }

    protected static void execPrepStmts(Connection conn, String prepStmts) {
        String[] prepStmtsArr = prepStmts.split(";");
        for (int i = 0; i < prepStmtsArr.length; i++) {
            if (StringUtil.isEmpty(prepStmtsArr[i])) {
                continue;
            }
            JdbcUtil.executeSuccess(conn, prepStmtsArr[i]);
        }
    }

    protected void execPrepStmtsByParams(String sql, List<Object> params, Connection mysqlConn, Connection polarxConn) {
        DataValidator validator = new DataValidator();
        validator.selectContentSameAssert(sql, params, mysqlConn, polarxConn, true);
    }

    protected void execInsertPrepStmtsByParams(String sql, List<Object> params, Connection mysqlConn,
                                               Connection polarxConn) {
        try {
            PreparedStatement stmt = JdbcUtil.preparedStatementSet(sql, params, mysqlConn);
            stmt.executeUpdate();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        try {
            PreparedStatement stmt = JdbcUtil.preparedStatementSet(sql, params, polarxConn);
            stmt.executeUpdate();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

    }

    protected static void logSql(String caseStr, String sqlType, String sql) {
        log.info(String.format("case=[%s], sqlType=[%s], sql=[\n%s\n]\n", caseStr, sqlType, sql));
    }

    protected static class TestParameter {
        public String tblName;
        public String columns;

        public String[] partCols;
        public String[] partFuns;
        public String[] partColDataTypes;

        public String[] prepareStmts;

        public String[] bndVals;
        public String strategy;

        /**
         * the values of insert
         */
        public String[] insertValues;
        /**
         * the values of select
         */
        public String[] selectValues;

        /**
         * the range query values
         */
        public String[] rngQuerySortedValues;

        public TestParameter(String tblName,
                             String[] partCols,
                             String[] partColDataTypes,
                             String strategy,
                             String[] bndVals,
                             String[] insertValues) {
            this(tblName, partCols, null, partColDataTypes, null, strategy, bndVals, insertValues, insertValues, null);
        }

        public TestParameter(String tblName,
                             String[] partCols,
                             String[] partFuns,
                             String[] partColDataTypes,
                             String strategy,
                             String[] bndVals,
                             String[] insertValues) {
            this(tblName, partCols, partFuns, partColDataTypes, null, strategy, bndVals, insertValues, insertValues,
                null);
        }

        public TestParameter(String tblName,
                             String[] partCols,
                             String[] partColDataTypes,
                             String strategy,
                             String[] bndVals,
                             String[] insertValues,
                             String[] selectValues) {
            this(tblName, partCols, null, partColDataTypes, null, strategy, bndVals, insertValues, selectValues, null);
        }

        public TestParameter(String tblName,
                             String[] partCols,
                             String[] partColDataTypes,
                             String[] prepareStmts,
                             String strategy,
                             String[] bndVals,
                             String[] insertValues,
                             String[] selectValues,
                             String[] rngQuerySortedValues) {
            this(tblName, partCols, null, partColDataTypes, prepareStmts, strategy, bndVals, insertValues, selectValues,
                rngQuerySortedValues);
        }

        public TestParameter(String tblName,
                             String[] partCols,
                             String[] partFuns,
                             String[] partColDataTypes,
                             String[] prepareStmts,
                             String strategy,
                             String[] bndVals,
                             String[] insertValues,
                             String[] selectValues,
                             String[] rngQuerySortedValues) {
            this.tblName = tblName;
            this.partCols = partCols;
            this.partFuns = partFuns;
            this.partColDataTypes = partColDataTypes;
            this.prepareStmts = prepareStmts;
            this.strategy = strategy;
            this.bndVals = bndVals;
            this.insertValues = insertValues;
            this.selectValues = selectValues;
            this.rngQuerySortedValues = rngQuerySortedValues;
        }

        @Override
        public String toString() {
            String str = String.format("[%s][%s][%s][%s][%s][%s]",
                this.tblName,
                String.join(",", this.strategy),
                String.join(",", this.partCols),
                this.partFuns == null ? "" : String.join(",", this.partFuns),
                String.join(",", this.partColDataTypes),
                String.join(",", this.bndVals));
            return str;
        }
    }

    protected static String genListStringHexBinaryByCharset(String charset, String[] values) {
        StringBuilder sb = new StringBuilder("");
        sb.append("(");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(genStringHexBinaryByCharset(values[i], charset, true, true, false));
        }
        sb.append(")");
        return sb.toString();
    }

    protected static String genStringHexBinaryByCharset(String value, String charset) {
        return genStringHexBinaryByCharset(value, charset, true, false, false);
    }

    protected static String genStringHexBinaryByCharset(String value, String charset, boolean prefixCharset,
                                                        boolean isForList) {
        return genStringHexBinaryByCharset(value, charset, prefixCharset, isForList, false);
    }

    protected static String genStringHexBinaryByCharset(String value, String charset,
                                                        boolean prefixCharset,
                                                        boolean isForList,
                                                        boolean use0xFormat) {
        StringBuilder sb = new StringBuilder("");
        byte[] byteArr = null;
        try {
            byteArr = value.getBytes(charset);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        if (!isForList) {
            sb.append("(");
        }
        if (!use0xFormat) {
            if (prefixCharset) {
                sb.append(" _");
                sb.append(charset);
                sb.append(" ");
            }
            sb.append("x'");
        } else {
            sb.append("0x");
        }

        for (int i = 0; i < byteArr.length; i++) {
            sb.append(HexUtils.getHexString(byteArr[i], 1));
        }

        if (!use0xFormat) {
            sb.append("'");
        }

        if (!isForList) {
            sb.append(")");
        }

        return sb.toString();
    }
}
