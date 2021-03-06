package com.openddal.excutor.cursor;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.openddal.command.expression.Expression;
import com.openddal.engine.Session;
import com.openddal.message.DbException;
import com.openddal.result.Row;
import com.openddal.result.SearchRow;
import com.openddal.value.DataType;
import com.openddal.value.Value;

public class ResultCursor implements Cursor {

    private Session session;
    private Row current;
    private final ResultSet rs;
    private final Expression[] cols;

    public ResultCursor(Session session, ResultSet rs) {
        this.session = session;
        this.rs = rs;
        this.cols = Expression.getExpressionColumns(session, rs);
    }

    @Override
    public Row get() {
        return current;
    }

    @Override
    public SearchRow getSearchRow() {
        return current;
    }

    @Override
    public boolean next() {
        try {
            boolean result = rs.next();
            if (!result) {
                rs.close();
                current = null;
                return false;
            }
        } catch (SQLException e) {
            throw DbException.convert(e);
        }
        current = createRow();
        for (int i = 0; i < current.getColumnCount(); i++) {
            Value v = DataType.readValue(session, rs, i + 1, cols[i].getType());
            current.setValue(i, v);
        }
        return true;
    }

    @Override
    public boolean previous() {
        return false;
    }

    public Expression[] getExpressionColumns() {
        return cols;
    }

    private Row createRow() {
        return new Row(new Value[cols.length], Row.MEMORY_CALCULATE);
    }

}
