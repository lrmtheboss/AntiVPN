package dev.brighten.antivpn.database.sql.utils;

import lombok.Getter;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.SQLException;

public class Query {
    @Getter
    private static Connection conn;

    public static void use(Connection conn) {
        Query.conn = conn;
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    public static ExecutableStatement prepare(@Language("SQL") String sql) throws SQLException {
        return new ExecutableStatement(conn.prepareStatement(sql));
    }


}
