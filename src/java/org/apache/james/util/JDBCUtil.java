/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.james.util;

import java.sql.*;
import java.util.Locale;

/**
 * Helper class for managing common JDBC tasks.
 *
 * This class is abstract to allow implementations to 
 * take advantage of different logging capabilities/interfaces in
 * different parts of the code.
 *
 * @author Noel Bergman <noel@devtech.com>
 * @author Peter M. Goldstein <farsight@alum.mit.edu>
 *
 */
abstract public class JDBCUtil
{
    abstract protected void delegatedLog(String errorString);

    /**
     * Checks database metadata to see if a table exists.
     * Try UPPER, lower, and MixedCase, to see if the table is there.
     *
     * @param dbMetaData the database metadata to be used to look up this table
     * @param tableName the table name
     *
     * @exception SQLException thrown if an exception is encountered while accessing the database
     */
    public boolean tableExists(DatabaseMetaData dbMetaData, String tableName)
        throws SQLException {
        return ( tableExistsCaseSensitive(dbMetaData, tableName) ||
                 tableExistsCaseSensitive(dbMetaData, tableName.toUpperCase(Locale.US)) ||
                 tableExistsCaseSensitive(dbMetaData, tableName.toLowerCase(Locale.US)) );
    }

    /**
     * Checks database metadata to see if a table exists.  This method
     * is sensitive to the case of the provided table name.
     *
     * @param dbMetaData the database metadata to be used to look up this table
     * @param tableName the case sensitive table name
     *
     * @exception SQLException thrown if an exception is encountered while accessing the database
     */
    public boolean tableExistsCaseSensitive(DatabaseMetaData dbMetaData, String tableName)
        throws SQLException {
        ResultSet rsTables = dbMetaData.getTables(null, null, tableName, null);
        try {
            boolean found = rsTables.next();
            return found;
        } finally {
            closeJDBCResultSet(rsTables);
        }
    }

    /**
     * Closes database connection and logs if an error
     * is encountered
     *
     * @param conn the connection to be closed
     */
    public void closeJDBCConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception and continue
            subclassLogWrapper("Unexpected exception while closing database connection.");
        }
    }

    /**
     * Closes database statement and logs if an error
     * is encountered
     *
     * @param stmt the statement to be closed
     */
    public void closeJDBCStatement(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException sqle) {
            // Log exception and continue
            subclassLogWrapper("Unexpected exception while closing database statement.");
        }
    }

    /**
     * Closes database result set and logs if an error
     * is encountered
     *
     * @param aResultSet the result set to be closed
     */
    public void closeJDBCResultSet(ResultSet aResultSet ) {
        try {
            if (aResultSet != null) {
                aResultSet.close();
            }
        } catch (SQLException sqle) {
            // Log exception and continue
            subclassLogWrapper("Unexpected exception while closing database result set.");
        }
    }

    /**
     * Wraps the delegated call to the subclass logging method with a Throwable
     * wrapper.  All throwables generated by the subclass logging method are
     * caught and ignored.
     *
     * @param logString the raw string to be passed to the logging method implemented
     *                  by the subclass
     */
    private void subclassLogWrapper(String logString)
    {
        try {
            delegatedLog(logString);
        }
        catch(Throwable t) {
            // Throwables generated by the logging system are ignored
        }
    }

}
