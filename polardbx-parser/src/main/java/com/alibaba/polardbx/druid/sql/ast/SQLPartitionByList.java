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
package com.alibaba.polardbx.druid.sql.ast;

import com.alibaba.polardbx.druid.sql.visitor.SQLASTVisitor;

public class SQLPartitionByList extends SQLPartitionBy {

    protected boolean isColumns = false;

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, columns);
            acceptChild(visitor, partitionsCount);
            acceptChild(visitor, getPartitions());
            acceptChild(visitor, subPartitionBy);
        }
        visitor.endVisit(this);
    }

    public SQLPartitionByList clone() {
        SQLPartitionByList x = new SQLPartitionByList();

        this.cloneTo(x);

        for (SQLExpr column : columns) {
            SQLExpr c2 = column.clone();
            c2.setParent(x);
            x.columns.add(c2);
        }

        x.setColumns(this.isColumns);

        return x;
    }

    public void cloneTo(SQLPartitionByList x) {
        super.cloneTo(x);
    }

    public boolean isColumns() {
        return isColumns;
    }

    public void setColumns(boolean columns) {
        isColumns = columns;
    }
}
