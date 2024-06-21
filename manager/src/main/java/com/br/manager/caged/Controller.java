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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Controller {

    public static final Cache<String, Integer> PROCESS_TOTAL = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build();
    public static final Cache<String, String> PROCESS_DIRECTORY = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build();
    MessagePublisher messagePublisher;
    ExecuteSqlService executeSqlService;

    @PostMapping("/savedata")
    public String sendMessage(@RequestBody CagedDirectory file) {
        System.out.println("Iniciado o processo de gravação de arquivos");
        System.out.println(LocalTime.now());
        Path directoryPath = Paths.get(file.getDirectory());
        List<String> nameFileList = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directoryPath, Files::isRegularFile)) {
            for (Path path : directoryStream) {
                nameFileList.add(path.getFileName().toString());
            }
        } catch (IOException ex) {
//            donothing
        }

        getColumnAndCreateTable(file.getDirectory(), file.getTableName(), file.isStructured());
        String processId = UUID.randomUUID().toString();
        PROCESS_TOTAL.put(processId, nameFileList.size());
        PROCESS_DIRECTORY.put(processId, file.getDirectory());

        for (String nameFile : nameFileList) {
            messagePublisher.publishMessageSaveData(file.getDirectory(), nameFile, file.getTableName(), processId, file.isStructured());
        }
        return "Iniciado o processo de gravação dos arquivos!";
    }

    @PostMapping("/findData")
    public String findData(@RequestBody CagedEntity entity) {
        System.out.println("Iniciado o processo de Busca");
        System.out.println(LocalTime.now());
        String processId = UUID.randomUUID().toString();
        List<Integer> codeSearch = executeSqlService.getAllCode(entity.getDefaultSearch());
        Integer valueSearch = executeSqlService.getCode(entity.getFieldSearch(), entity.valueSearch);

        PROCESS_TOTAL.put(processId, codeSearch.size());

//        for (Integer code : codeSearch) {
        messagePublisher.publishMessageFIndData(entity.getTableName(), entity.getDefaultSearch(), 11, entity.fieldSearch, valueSearch, processId);
//        }
        return "Iniciado o processo de busca!";
    }

    void getColumnAndCreateTable(String directory, String tableName, boolean isStructured) {
        BufferedReader br = null;
        try {
            File file = new File(directory + "\\layout\\layout.csv");

            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1));
            List<String> columns = new ArrayList<>();

            String line;

            while ((line = br.readLine()) != null) {
                String[] valor = line.split(";");

                if (valor[0] != null && !valor[0].isEmpty()) {
                    columns.add(valor[0]);
                }
            }

            br.close();

            createTable(columns, tableName, isStructured);

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

    String createColumns(List<String> columns) {
        String columnsTable = "";
        for (int i = 0; i <= columns.size() - 1; i++) {
            String col = columns.get(i)
                    .replace(" ", "")
                    .replace("/", "")
                    .replace(".", "")
                    .replace("-", "");
            columnsTable += col + " varchar(150), ";
        }

        return columnsTable.substring(0, columnsTable.length() - 2);
    }

    void createTable(List<String> columns, String tableName, boolean isStructured) throws Exception {
        executeSqlService.executeSqlScript(String.format("create table %s ( %s );", tableName,
                (isStructured) ?
                        createColumnsNumeric(columns) : createColumns(columns)));
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
