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
package com.alibaba.polardbx.druid.sql.dialect.mysql.ast.statement;

import com.alibaba.polardbx.druid.DbType;
import com.alibaba.polardbx.druid.sql.ast.SQLName;
import com.alibaba.polardbx.druid.sql.ast.statement.SQLAlterStatement;
import com.alibaba.polardbx.druid.sql.dialect.mysql.visitor.MySqlASTVisitor;

import java.util.HashMap;
import java.util.Map;

public class MySqlCreateExternalCatalogStatement extends MySqlStatementImpl implements SQLAlterStatement {
    private SQLName name;

    private boolean ifNotExists;

    private Map<SQLName, SQLName> properties = new HashMap<SQLName, SQLName>();
    private SQLName comment;

    public MySqlCreateExternalCatalogStatement() {
        setDbType(DbType.mysql);
    }

    public SQLName getName() {
        return name;
    }

    public void setName(SQLName name) {
        if (name != null) {
            name.setParent(this);
        }
        this.name = name;
    }

    public SQLName getComment() {
        return comment;
    }

    public void setComment(SQLName comment) {
        if (comment != null) {
            comment.setParent(this);
        }
        this.comment = comment;
    }

    public Map<SQLName, SQLName> getProperties() {
        return properties;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public void accept0(MySqlASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, name);
            acceptChild(visitor, comment);
        }
        visitor.endVisit(this);
    }
}
