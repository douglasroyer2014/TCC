package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 13h pra inserir 800 mil registro.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        readFile();
    }

    private static void readFile() throws Exception {
        File file = new File("C:\\Temp\\caged.txt");

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1));

        String[] columns = br.readLine().split(";");
//        createTable(columns, "caged");
        String st = br.readLine();
        String sql = "insert into caged values ";
        int control = 0;
        while ((st = br.readLine()) != null) {
            control += 1;
            sql += insertData(st.split(";"));

            if (control == 1500) {
                executeQuery(sql.substring(0, sql.length() - 2));
                sql = "insert into caged values ";
                control = 0;
            }

        }
        ready("caged", "munic�pio", "110012");
    }

    private static void createTable(String[] columns, String tableName) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5433/postgres", "postgres", "admin");

        Statement stmt = con.createStatement();
        String createTable = String.format("create table %s ( %s );", tableName, createColumns(columns));

        stmt.execute(createTable);
        stmt.close();
        con.close();
    }

    private static String createColumns(String[] columns) {
        String columnsTable = "";
        for (int i = 0; i <= columns.length - 2; i++) {
            String col = columns[i].replace(" ", "");
            columnsTable += col + " varchar(100), ";
        }

        columnsTable += columns[columns.length - 1].replace(" ", "") + " varchar(100)";
        return columnsTable.replace("/", "").replace(".", "")
                .replace("-", "");
    }

    static String insertData(String[] data) throws Exception {
        String values = "";
        for (int i = 0; i <= data.length - 2; i++) {
            values += "'" + data[i] + "',";
        }
        values += "'" + data[data.length - 1] + "'";

        return String.format("(%s), ", values);
    }

    private static void ready(String tableName, String columnName, String condition) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5433/postgres", "postgres", "admin");

        String query = String.format("Select * from %s where \"%s\" = '%s'", tableName, columnName, condition);

        PreparedStatement preparedStatement = con.prepareStatement(query);

        ResultSet rs = preparedStatement.executeQuery();
        int count = 0;
        while (rs.next()) {
            count += 1;
            System.out.println("Adimitidos/desligados: " + rs.getString("admitidosdesligados") + " municipio: " + rs.getString("munic�pio"));
        }

        System.out.println(count);
    }

    private static void executeQuery(String sql) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5433/postgres", "postgres", "admin");

        Statement stmt = con.createStatement();

        stmt.execute(sql);
        stmt.close();
        con.close();
    }
}