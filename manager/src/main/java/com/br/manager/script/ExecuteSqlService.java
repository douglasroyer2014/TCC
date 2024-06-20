package com.br.manager.script;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExecuteSqlService {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://201.54.201.38/postgres", "postgres", "aluno");
    }

    public void executeSqlScript(String script) {
        try {
            Connection con = getConnection();

            Statement stmt = con.createStatement();

            stmt.execute(script);
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getAllCode(String tableName) {
        List<Integer> codeSearchList = new ArrayList<>();

        try {
            Connection con = getConnection();
            PreparedStatement preparedStatement = con.prepareStatement(String.format("Select código from %s", tableName));

            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                codeSearchList.add(result.getInt("código"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return codeSearchList;
    }

    public Integer getCode(String fieldSearch, String valueSearch) {
        Integer code = null;

        try {
            Connection con = getConnection();
            PreparedStatement preparedStatement = con.prepareStatement(String.format("Select código from %s where descrição = '%s'", fieldSearch, valueSearch));

            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                code = result.getInt("código");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    public String getDescription(String tableName, Integer code) throws Exception {
        String description = "";
        Connection con = getConnection();
        PreparedStatement preparedStatement = con.prepareStatement(String.format("Select descrição from %s where código = '%s'", tableName, code));

        ResultSet result = preparedStatement.executeQuery();
        while (result.next()) {
            description = result.getString("descrição");
        }
        return description;
    }

    public Map<Integer, String> getCodeAndValue(String tableName, String where) throws Exception {
        Map<Integer, String> value = new HashMap<>();
        Connection con = getConnection();
        PreparedStatement preparedStatement = con.prepareStatement(String.format("Select \"código\", \"descrição\" from %s where \"código\" in %s", tableName, where));

        ResultSet result = preparedStatement.executeQuery();
        while (result.next()) {
            value.put(result.getInt("código"), result.getString("descrição"));
        }
        return value;
    }
}
