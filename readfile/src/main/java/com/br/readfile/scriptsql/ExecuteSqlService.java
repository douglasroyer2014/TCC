package com.br.readfile.scriptsql;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Service
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExecuteSqlService {

    @Async
    public void executeSqlScript(String script) {
        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://201.54.201.38/postgres", "postgres", "aluno");

            Statement stmt = con.createStatement();

            stmt.execute(script);
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
