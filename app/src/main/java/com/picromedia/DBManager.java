package com.picromedia;

import com.picromedia.models.SqlType;

import java.lang.reflect.Field;
import java.sql.*;
public class DBManager<T> {
    private static final Connection conn;

    static {
        try {
            // Access keeps getting denied for some reason
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/picromedia",
                    SecretsManager.getSecret("SqlUser"), SecretsManager.getSecret("SqlPass"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private final Class<T> managedClass;

    public DBManager(Class<T> managedClass) {
        this.managedClass = managedClass;
        try (Statement stmt = conn.createStatement()) {
            ResultSet table = stmt.executeQuery("SELECT * FROM information_schema.tables WHERE table_schema = 'picross' AND table_name = '" + managedClass.getName() + "' LIMIT 1;");
            if (!table.first()) {
                return;
            }
            StringBuilder query = new StringBuilder();
            query.append("CREATE TABLE ").append(managedClass.getName()).append(" (");
            Field[] fields = this.managedClass.getFields();
            boolean hasId = false;
            for (Field field : fields) {
                String name = field.getName();
                query.append(name.substring(0,1).toUpperCase()).append(name.substring(1)).append(" ");
                SqlType sqlType = field.getAnnotation(SqlType.class);
                query.append(sqlType != null ? sqlType.value() : mapTypeToSql(field.getType().getName()));
                if ("id".equals(name)) {
                    query.append(" AUTO INCREMENT");
                    hasId = true;
                }
                query.append(", ");
            }
            if (hasId) {
                query.append("PRIMARY KEY (Id)");
            }
            query.append(");");
            stmt.execute(query.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String mapTypeToSql(String typeName) {
        return switch (typeName) {
            case "int" -> "INT";
            case "long" -> "BIGINT";
            case "String" -> "VARCHAR(255)";
            case "double" -> "DOUBLE";
            case "float" -> "FLOAT";
            case "DateTime" -> "DATETIME";
            case "boolean" -> "BOOL";
            default -> "MEDIUMBLOB";
        };
    }
}
