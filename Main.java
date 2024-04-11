package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) throws Exception {
        readFile();
    }

    private static void readFile() throws Exception {
        File file = new File("C:\\git\\TCC\\test\\src\\main\\java\\org\\example\\caged.txt");

        BufferedReader br
                = new BufferedReader(new FileReader(file));

        String[] columns = br.readLine().split(";");
        createTable(columns);
        String st;
        int i = 0;
        while ((st = br.readLine()) != null) {
            i += 1;
        }
    }

    private static void createTable(String[] columns) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:postgresql://localhost/postgres", "postgres", "postgres");

        Statement stmt = con.createStatement();
        String createTable =
                "create table the_answer() \n" +
                        "returns integer as $$\n" +
                        "begin \n" +
                        "   return 42;\n" +
                        "end;\n" +
                        "$$\n" +
                        "language plpgsql;";

        stmt.execute(createTable);
    }
}