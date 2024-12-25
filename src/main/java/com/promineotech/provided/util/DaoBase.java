package com.promineotech.provided.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DaoBase {

    /**
     * Starts a database transaction by disabling auto-commit mode.
     *
     * @param conn The database connection.
     * @throws SQLException If an SQL error occurs.
     */
    protected void startTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
        }
    }

    /**
     * Commits the current transaction.
     *
     * @param conn The database connection.
     * @throws SQLException If an SQL error occurs.
     */
    protected void commitTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
        }
    }

    /**
     * Rolls back the current transaction.
     *
     * @param conn The database connection.
     * @throws SQLException If an SQL error occurs.
     */
    protected void rollbackTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.rollback();
        }
    }

    /**
     * Sets a parameter on a prepared statement, handling null values correctly.
     *
     * @param stmt   The prepared statement.
     * @param index  The parameter index (1-based).
     * @param value  The value to set (may be null).
     * @param type   The class type of the value.
     * @throws SQLException If an SQL error occurs.
     */
    protected <T> void setParameter(PreparedStatement stmt, int index, T value, Class<T> type) throws SQLException {
        if (value == null) {
            stmt.setNull(index, java.sql.Types.NULL);
        } else if (type == String.class) {
            stmt.setString(index, (String) value);
        } else if (type == Integer.class) {
            stmt.setInt(index, (Integer) value);
        } else if (type == BigDecimal.class) {
            stmt.setBigDecimal(index, (BigDecimal) value);
        } else {
            throw new SQLException("Unsupported parameter type: " + type.getName());
        }
    }

    /**
     * Retrieves the ID of the last inserted row for a specific table.
     *
     * @param conn      The database connection.
     * @param tableName The table name.
     * @return The last inserted ID.
     * @throws SQLException If an SQL error occurs.
     */
    protected Integer getLastInsertId(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT LAST_INSERT_ID() AS id";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Unable to retrieve last insert ID for table: " + tableName);
            }
        }
    }
}
