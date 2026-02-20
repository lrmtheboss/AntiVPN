/*
 * Copyright 2026 Dawson Hessler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.brighten.antivpn.database.sql.utils;

import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.UUID;

public class ExecutableStatement implements AutoCloseable {
    @Getter
    private final PreparedStatement statement;
    private int pos = 1;

    public ExecutableStatement(PreparedStatement statement) {
        this.statement = statement;
    }

    public int execute() throws SQLException {
        return statement.executeUpdate();
    }

    public void execute(ResultSetIterator iterator) throws SQLException {
        try(var rs = statement.executeQuery()) {
            while (rs.next()) iterator.next(rs);
        }
    }

    public int[] executeBatch() throws SQLException {
        return statement.executeBatch();
    }

    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }

    @SneakyThrows
    public ExecutableStatement append(Object obj) {
        statement.setObject(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(String obj) {
        statement.setString(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(UUID uuid) {
        if (uuid != null) statement.setString(pos++, uuid.toString().replace("-", ""));
        else statement.setString(pos++, null);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Array obj) {
        statement.setArray(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Integer obj) {
        statement.setInt(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Short obj) {
        statement.setShort(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Long obj) {
        statement.setLong(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Float obj) {
        statement.setFloat(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Double obj) {
        statement.setDouble(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Date obj) {
        statement.setDate(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Timestamp obj) {
        statement.setTimestamp(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Time obj) {
        statement.setTime(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Blob obj) {
        statement.setBlob(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(byte[] obj) {
        statement.setBytes(pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement addBatch() {
        statement.addBatch();
        return this;
    }

    @Override
    public void close() throws SQLException {
        statement.close();
    }
}
