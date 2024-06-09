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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FindService {

    public List<Map<String, Integer>> findData(String tableName, String defaultSearch, int defaultValue, String fieldSearch, int value) {
        List<Map<String, Integer>> valueList = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://201.54.201.31/postgres", "postgres", "aluno");

            String sql = "Select * from " + tableName + " where " + defaultSearch + " = ? and " + fieldSearch + " = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, defaultValue);
            preparedStatement.setInt(2, value);

            ResultSet result = preparedStatement.executeQuery();

            ResultSetMetaData metaData = result.getMetaData();

            while (result.next()) {

                Map<String, Integer> valueResult = new HashMap<>();

                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    valueResult.put(metaData.getColumnName(i), result.getInt(i));
                }

                valueList.add(valueResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return valueList;
    }
}
