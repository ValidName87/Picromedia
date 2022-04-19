package com.picromedia;

import com.picromedia.models.ForeignKey;
import com.picromedia.models.PrimaryKey;
import com.picromedia.models.SqlType;

import java.lang.reflect.Field;
import java.sql.*;
public class DBManager<T> {
    private static final Connection conn;

    static {
        // Access keeps getting denied for some reason
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/picromedia",
                    SecretsManager.getSecret("SqlUser"), SecretsManager.getSecret("SqlPass"));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private final Class<T> managedClass;

    public DBManager(Class<T> managedClass) {
        this.managedClass = managedClass;
        try (Statement stmt = conn.createStatement()) {
            ResultSet table = stmt.executeQuery("SELECT * FROM information_schema.tables WHERE table_schema = 'picross' AND table_name = '" + managedClass.getSimpleName() + "' LIMIT 1;");
            if (table.isBeforeFirst()) {
                return;
            }
            StringBuilder query = new StringBuilder();
            query.append("CREATE TABLE ").append(managedClass.getSimpleName()).append(" (");

            Field[] fields = this.managedClass.getFields();
            boolean hasId = false;
            StringBuilder foreignKeys = new StringBuilder();
            for (Field field : fields) {
                String name = field.getName();
                name = name.substring(0,1).toUpperCase() + name.substring(1);
                query.append(name).append(" ");

                SqlType sqlType = field.getAnnotation(SqlType.class);
                query.append(sqlType != null ? sqlType.value() : mapTypeToSql(field.getType().getSimpleName()));

                if (field.getAnnotation(PrimaryKey.class) != null) {
                    query.append(" AUTO_INCREMENT");
                    hasId = true;
                }

                ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
                if (foreignKey != null) {
                    foreignKeys.append("FOREIGN KEY (").append(name).append(") REFERENCES ")
                            .append(foreignKey.table()).append("(").append(foreignKey.column()).append("), ");
                }

                query.append(", ");
            }

            if (hasId) {
                query.append("PRIMARY KEY (Id), ");
            }

            query.append(foreignKeys);

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
