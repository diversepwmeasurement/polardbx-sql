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

import com.alibaba.polardbx.druid.sql.ast.SQLExpr;
import com.alibaba.polardbx.druid.sql.ast.SQLName;
import com.alibaba.polardbx.druid.sql.ast.SQLReplaceable;
import com.alibaba.polardbx.druid.sql.ast.SQLStatementImpl;
import com.alibaba.polardbx.druid.sql.visitor.SQLASTVisitor;

public class SQLShowTablesStatement extends SQLStatementImpl implements SQLShowStatement, SQLReplaceable {
    private boolean extended = false;
    protected SQLName database;
    protected SQLExpr like;

    // for mysql
    protected boolean full;
    protected SQLExpr where;

    public SQLName getDatabase() {
        return database;
    }

    public SQLName getFrom() {
        return database;
    }

    public void setDatabase(SQLName database) {
        if (database != null) {
            database.setParent(this);
        }

        this.database = database;
    }

    public SQLExpr getLike() {
        return like;
    }

    public void setLike(SQLExpr like) {
        if (like != null) {
            like.setParent(this);
        }

        this.like = like;
    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public SQLExpr getWhere() {
        return where;
    }

    public void setWhere(SQLExpr where) {
        this.where = where;
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, database);
            acceptChild(visitor, like);
        }
        visitor.endVisit(this);
    }

    @Override
    public boolean replace(SQLExpr expr, SQLExpr target) {
        if (database == expr) {
            setDatabase((SQLName) target);
            return true;
        }

        if (like == expr) {
            setLike(target);
            return true;
        }

        if (where == expr) {
            setWhere(target);
            return true;
        }

        return false;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }
}
