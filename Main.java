package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        String directory = "C:\\Temp";

        String nameFile = "caged";

        File file = new File(directory + "\\" + nameFile + ".txt");

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1));

        String[] columns = br.readLine().split(";");

        System.out.println(System.currentTimeMillis());

        createTables(columns, directory, nameFile);

        System.out.println(System.currentTimeMillis());

        readFileAndInsert(br);

        System.out.println(System.currentTimeMillis());
    }

    private static void readFileAndInsert(BufferedReader br) throws Exception {
        String st;
        String sql = "insert into caged values ";
        int control = 0;
        while ((st = br.readLine()) != null) {
            control += 1;
            sql += String.format("(%s), ", convertValueInsertNumeric(st.split(";")));

            if (control == 1500) {
                try {
                    executeQuery(sql.substring(0, sql.length() - 2));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                sql = "insert into caged values ";
                control = 0;
            }

        }
    }

    static void createTables(String[] columns, String directory, String nameTable) throws Exception {
        List<String> tableFks = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            String tableName = columns[i]
                    .replace("/", "")
                    .replace(" ", "")
                    .replace("\\", "")
                    .replace(".", "")
                    .replace("-", "")
                    .toLowerCase();
            File file = new File(directory + "\\fk\\" + tableName + ".CSV");

            if (file.exists()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1));
                String[] columnsFk = br.readLine().split(";");

                createTable(columnsFk, tableName);

                String st;
                String values = "";
                while ((st = br.readLine()) != null) {
                    values += convertValueInsert(st.split(";"));
                }

                String sql = String.format("insert into %s values %s;", tableName, values.substring(0, values.length() - 2));

                executeQuery(sql);

                tableFks.add(tableName);
            }
        }

        createTableWithFk(columns, tableFks, nameTable);
    }

    private static void createTableWithFk(String[] columns, List<String> tableFks, String tableName) throws Exception {
        String createTable = String.format("create table %s ( %s );", tableName, createColumnsNumeric(columns));

        executeQuery(createTable);

        createFk(tableFks, tableName);
    }

    static void createFk(List<String> tableFks, String tableName) throws Exception {
        String query = "";
        for (String tableFk : tableFks) {
            query += "alter table " + tableName + " add constraint fk_" + tableFk + " foreign key (" + tableFk + ") references " + tableFk + " (código);";
        }

        executeQuery(query);
    }

    private static void createTable(String[] columns, String tableName) throws Exception {
        executeQuery(String.format("create table %s ( %s );", tableName, createColumns(columns)));
    }

    static String createColumnsNumeric(String[] columns) {
        String columnsTable = "";
        for (int i = 0; i <= columns.length - 1; i++) {
            columnsTable += columns[i]
                    .replace(" ", "")
                    .replace("/", "")
                    .replace(".", "")
                    .replace("-", "")
                    + " numeric, ";
        }

        return columnsTable.substring(0, columnsTable.length() - 2);
    }

    private static String createColumns(String[] columns) {
        String columnsTable = "";
        for (int i = 0; i <= columns.length - 1; i++) {
            if (columns[i].equals("código")) {
                columnsTable += columns[i] + " numeric primary key, ";
            } else {
                String col = columns[i]
                        .replace(" ", "")
                        .replace("/", "")
                        .replace(".", "")
                        .replace("-", "");
                columnsTable += col + " varchar(150), ";
            }
        }

        return columnsTable.substring(0, columnsTable.length() - 2);
    }

    static String convertValueInsertNumeric(String[] data) {
        String values = "";
        for (int i = 0; i <= data.length - 1; i++) {
            try {
                int value = Integer.valueOf(data[i]);
                values += value + ", ";

            } catch (Exception e) {
                values += "NULL, ";
            }
        }
        return values.substring(0, values.length() - 2);
    }

    static String convertValueInsert(String[] data) {
        String values = "";
        for (int i = 0; i <= data.length - 2; i++) {
            values += "'" + data[i].replace("'", "''") + "',";
        }
        values += "'" + data[data.length - 1] + "'";

        return String.format("(%s), ", values);
    }

    private static void executeQuery(String sql) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5433/postgres", "postgres", "admin");

        Statement stmt = con.createStatement();

        stmt.execute(sql);
        stmt.close();
        con.close();
    }
}
