package dev.brighten.antivpn.database.sql.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetIterator {
    void next(ResultSet rs) throws SQLException;
}
