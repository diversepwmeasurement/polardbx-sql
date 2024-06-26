/*
 * Copyright [2013-2021], Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.polardbx.druid.bvt.sql.mysql.createTable;

import com.alibaba.polardbx.druid.sql.MysqlTest;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.polardbx.druid.sql.dialect.mysql.parser.MySqlStatementParser;

import java.util.List;

public class MySqlCreateTableTest104 extends MysqlTest {

    public void test_0() throws Exception {
        String sql = "CREATE TABLE db1.table_0 (\n" +
            "  column_0 VARCHAR NOT NULL DEFAULT default_varchar COMMENT '',\n" +
            "  column_1 BIGINT COMMENT 'BIGINT_comment',\n" +
            "  column_2 DOUBLE NOT NULL DEFAULT default_double COMMENT '',\n" +
            "  column_3 VARCHAR delimiter \",\" COMMENT ''\n" +
            ")\n" +
            "PARTITION BY KEY (column_0) PARTITION NUM 0\n" +
            "TABLEGROUP gg\n" +
            "OPTIONS (UPDATETYPE='realtime')";

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        MySqlCreateTableStatement stmt = (MySqlCreateTableStatement) statementList.get(0);

        assertEquals(1, statementList.size());
        assertEquals(4, stmt.getTableElementList().size());

        assertEquals("CREATE TABLE db1.table_0 (\n"
            + "\tcolumn_0 VARCHAR NOT NULL DEFAULT default_varchar COMMENT '',\n"
            + "\tcolumn_1 BIGINT COMMENT 'BIGINT_comment',\n"
            + "\tcolumn_2 DOUBLE NOT NULL DEFAULT default_double COMMENT '',\n"
            + "\tcolumn_3 VARCHAR DELIMITER ',' COMMENT ''\n"
            + ")\n"
            + "OPTIONS (UPDATETYPE = 'realtime')\n"
            + "PARTITION BY KEY (column_0) PARTITION NUM 0\n"
            + "TABLEGROUP = gg", stmt.toString());
    }
}