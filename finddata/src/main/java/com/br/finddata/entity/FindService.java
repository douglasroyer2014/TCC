package com.br.finddata.entity;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FindService {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://localhost:5433/postgres", "postgres", "admin");
    }

    public List<Map<String, Object>> findData(String tableName, String defaultSearch, String defaultValue, String fieldSearch, String value, boolean structured) {
        List<Map<String, Object>> valueList = new ArrayList<>();
        try {
            Connection con = getConnection();
            String sql = "Select * from " + tableName + " where " + defaultSearch + " = ? and " + fieldSearch + " = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            if (structured) {
                preparedStatement.setInt(1, Integer.valueOf(defaultValue));
                preparedStatement.setInt(2, Integer.valueOf(value));
            } else {
                preparedStatement.setString(1, defaultValue);
                preparedStatement.setString(2, value);
            }
            ResultSet result = preparedStatement.executeQuery();

            ResultSetMetaData metaData = result.getMetaData();

            while (result.next()) {

                Map<String, Object> valueResult = new HashMap<>();

                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    valueResult.put(metaData.getColumnName(i), (result.getObject(i) != null) ? result.getObject(i) : "NULL");
                }

                valueList.add(valueResult);
            }
            preparedStatement.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return valueList;
    }
}
