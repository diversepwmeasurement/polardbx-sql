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

package com.alibaba.polardbx.druid.sql.dialect.mysql.ast.statement;

import com.alibaba.polardbx.druid.sql.ast.SQLExpr;
import com.alibaba.polardbx.druid.sql.ast.SqlType;
import com.alibaba.polardbx.druid.sql.ast.statement.SQLAssignItem;
import com.alibaba.polardbx.druid.sql.dialect.mysql.visitor.MySqlASTVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yudong
 * @since 2023/6/20 14:27
 **/
public class MySqlSetCdcGlobalStatement extends MySqlStatementImpl {
    private SQLExpr with;
    private List<SQLAssignItem> items = new ArrayList<>();

    public MySqlSetCdcGlobalStatement() {
    }

    public List<SQLAssignItem> getItems() {
        return items;
    }

    public void setItems(List<SQLAssignItem> items) {
        this.items = items;
    }

    public SQLExpr getWith() {
        return with;
    }

    public void setWith(SQLExpr with) {
        this.with = with;
    }

    @Override
    public void accept0(MySqlASTVisitor v) {
        v.visit(this);
        v.endVisit(this);
    }

    @Override
    public SqlType getSqlType() {
        return null;
    }
}
