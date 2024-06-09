package com.br.manager.caged;

import com.br.manager.message.MessagePublisher;
import com.br.manager.script.ExecuteSqlService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Controller {

    public static final Cache<String, Integer> PROCESS_TOTAL = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build();
    MessagePublisher messagePublisher;
    ExecuteSqlService executeSqlService;

    @PostMapping("/savedata")
    public String sendMessage(@RequestBody CagedDirectory file) {
        Path directoryPath = Paths.get(file.getDirectory());
        List<String> nameFileList = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directoryPath, Files::isRegularFile)) {
            for (Path path : directoryStream) {
                nameFileList.add(path.getFileName().toString());
            }
        } catch (IOException ex) {
//            donothing
        }

        createTables(file.getDirectory(), file.getTableName());
        String processId = UUID.randomUUID().toString();
        PROCESS_TOTAL.put(processId, nameFileList.size());

        for (String nameFile : nameFileList) {
            messagePublisher.publishMessageSaveData(file.getDirectory(), nameFile, file.getTableName(), processId);
        }
        return "Iniciado o processo de gravação dos arquivos!";
    }

    @PostMapping("/findData")
    public String findData(@RequestBody CagedEntity entity) {

        String processId = UUID.randomUUID().toString();
        List<Integer> codeSearch = executeSqlService.getAllCode(entity.getDefaultSearch());
        Integer valueSearch = executeSqlService.getCode(entity.getFieldSearch(), entity.valueSearch);

        PROCESS_TOTAL.put(processId, codeSearch.size());

        for (Integer code : codeSearch) {
            messagePublisher.publishMessageFIndData(entity.getTableName(), entity.getDefaultSearch(), 11, entity.fieldSearch, valueSearch, processId);
        }
        return "Iniciado o processo de busca!";
    }

    void createTables(String directory, String tableName) {
        BufferedReader br = null;
        try {
            File file = new File(directory + "\\layout\\layout.csv");

            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1));
            List<String> columns = new ArrayList<>();

            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] valor = line.split(";");

                if (valor[0] != null && !valor[0].isEmpty()) {
                    columns.add(valor[0]);
                }
            }

            br.close();
            createTables(columns, directory, tableName);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void createTables(List<String> columns, String directory, String nameTable) throws Exception {
        List<String> tableFks = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            String tableName = columns.get(i)
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

                executeSqlService.executeSqlScript(sql);

                tableFks.add(tableName);
                br.close();
            }
        }


        createTableWithFk(columns, tableFks, nameTable);
    }

    String createColumns(String[] columns) {
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

    void createTableWithFk(List<String> columns, List<String> tableFks, String tableName) throws Exception {
        String createTable = String.format("create table %s ( %s );", tableName, createColumnsNumeric(columns));

        executeSqlService.executeSqlScript(createTable);

        createFk(tableFks, tableName);
    }

    void createFk(List<String> tableFks, String tableName) throws Exception {
        String query = "";
        for (String tableFk : tableFks) {
            query += "alter table " + tableName + " add constraint fk_" + tableFk + " foreign key (" + tableFk + ") references " + tableFk + " (código);";
        }

        executeSqlService.executeSqlScript(query);
    }

    void createTable(String[] columns, String tableName) throws Exception {
        executeSqlService.executeSqlScript(String.format("create table %s ( %s );", tableName, createColumns(columns)));
    }

    String convertValueInsert(String[] data) {
        String values = "";
        for (int i = 0; i <= data.length - 2; i++) {
            values += "'" + data[i].replace("'", "''") + "',";
        }
        values += "'" + data[data.length - 1] + "'";

        return String.format("(%s), ", values);
    }

    String createColumnsNumeric(List<String> columns) {
        String columnsTable = "";
        for (int i = 0; i <= columns.size() - 1; i++) {
            columnsTable += columns.get(i)
                    .replace(" ", "")
                    .replace("/", "")
                    .replace(".", "")
                    .replace("-", "")
                    + " numeric, ";
        }

        return columnsTable.substring(0, columnsTable.length() - 2);
    }
}
