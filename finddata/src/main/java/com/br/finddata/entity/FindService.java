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

@org.springframework.stereotype.Service
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FindService {

    public void findData(String tableName, String defaultSearch, int defaultValue, String fieldSearch, int value) {
        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5433/postgres", "postgres", "admin");

            String sql = "Select * from " + tableName + " where " + defaultSearch + " = ? and " + fieldSearch + " = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, defaultValue);
            preparedStatement.setInt(2, value);

            ResultSet result = preparedStatement.executeQuery();

            ResultSetMetaData metaData = result.getMetaData();

            while (result.next()) {

                String valueData = "";

                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    valueData += metaData.getColumnName(i) + ": " + result.getObject(i) + "; ";
                }

                System.out.println(valueData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
