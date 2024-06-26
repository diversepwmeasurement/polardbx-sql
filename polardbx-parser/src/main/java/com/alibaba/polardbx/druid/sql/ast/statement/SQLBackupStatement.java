/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.polardbx.druid.sql.ast.statement;

import com.alibaba.polardbx.druid.sql.ast.SQLName;
import com.alibaba.polardbx.druid.sql.ast.SQLStatementImpl;
import com.alibaba.polardbx.druid.sql.ast.SqlType;
import com.alibaba.polardbx.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.polardbx.druid.sql.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.List;

public class SQLBackupStatement extends SQLStatementImpl {
    private SQLName type;
    private SQLName action;
    private List<SQLCharExpr> properties = new ArrayList<SQLCharExpr>();

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, this.type);
            acceptChild(visitor, this.action);

            acceptChild(visitor, properties);
        }
        visitor.endVisit(this);
    }

    public SQLName getType() {
        return type;
    }

    public void setType(SQLName type) {
        this.type = type;
    }

    public SQLName getAction() {
        return action;
    }

    public void setAction(SQLName action) {
        this.action = action;
    }

    public List<SQLCharExpr> getProperties() {
        return properties;
    }

    @Override
    public SqlType getSqlType() {
        return null;
    }
}
