package com.br.manager.message;

import com.br.manager.script.ExecuteSqlService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.br.manager.caged.Controller.PROCESS_DIRECTORY;
import static com.br.manager.caged.Controller.PROCESS_TOTAL;

@Service
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageListener {

    ExecuteSqlService executeSqlService;

    @Transactional
    @RabbitListener(queues = MQConfig.MANAGER)
    public void listener(ManagerMessage message) {
        Jedis jedis = new Jedis("201.54.201.31", 6379);

        int total = jedis.incr(String.format("%S:%s", message.getProcessId(), message.getMessageType())).intValue();
        Integer processValue = PROCESS_TOTAL.getIfPresent(message.getProcessId());

        if (processValue != null && processValue == total) {
            finishedProcess(message, jedis);
        }
    }

    private void finishedProcess(ManagerMessage message, Jedis jedis) {
        if (message.getMessageType().equals("READ_FILE")) {
            createFK(message);
            System.out.println("Gravação dos arquivos finalizado!");
        } else {
            findData(message, jedis);
        }
        System.out.println(LocalTime.now());
    }

    private void createFK(ManagerMessage message) {
        String directory = PROCESS_DIRECTORY.getIfPresent(message.getProcessId());
        List<String> nameFileList = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory + "\\fk"), Files::isRegularFile)) {
            for (Path path : directoryStream) {
                nameFileList.add(path.getFileName().toString());
            }
        } catch (IOException ex) {
//            donothing
        }
        if (message.isStructured()) {
            try {
                for (String nameFile : nameFileList) {
                    createTable(nameFile.substring(0, nameFile.length() - 4), directory);
                }

                createAlterTable(nameFileList, message.getTableName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createAlterTable(List<String> tableNames, String tableName) {
        String query = "";
        for (String tableFk : tableNames) {
            String table = tableFk.substring(0, tableFk.length() - 4);
            query += "alter table " + tableName + " add constraint fk_" + table + " foreign key (" + table + ") references " + table + " (código);";
        }
        executeSqlService.executeSqlScript(query);
    }

    private void createTable(String tableName, String directory) throws Exception {
        File file = new File(directory + "\\fk\\" + tableName + ".CSV");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1));

        String columns = br.readLine();
        executeSqlService.executeSqlScript(String.format("create table %s ( %s );", tableName,
                createColumns(List.of(columns.split(";")))));
        String st;
        String values = "";
        while ((st = br.readLine()) != null) {
            values += convertValueInsert(st.split(";"));
        }

        String sql = String.format("insert into %s values %s;", tableName, values.substring(0, values.length() - 2));

        executeSqlService.executeSqlScript(sql);
        br.close();

    }

    private void findData(ManagerMessage message, Jedis jedis) {
        List<String> result = jedis.lrange(String.format("%s:%s", message.getProcessId(), message.getMessageType()), 0, -1);
        Gson gson = new Gson();
        List<Map<String, Integer>> valueResult = new ArrayList<>();

        for (String value : result) {
            valueResult.add(gson.fromJson(value, new TypeToken<Map<String, Integer>>() {
            }.getType()));
        }

        Map<String, Set<Integer>> codeMap = new HashMap<>();

        for (String key : valueResult.get(0).keySet()) {
            codeMap.put(key, new HashSet<>());
        }

        for (Map<String, Integer> resultFindData : valueResult) {
            for (String key : resultFindData.keySet()) {
                codeMap.get(key).add(resultFindData.get(key));
            }
        }

        Map<String, Map<Integer, String>> valueMap = new HashMap<>();

        for (String key : codeMap.keySet()) {
            String where = getWhere(codeMap.get(key));
            try {
                valueMap.put(key, executeSqlService.getCodeAndValue(key, where));
            } catch (Exception e) {
                //donothing
            }
        }

        for (Map<String, Integer> resultFindData : valueResult) {
            for (String key : resultFindData.keySet()) {
                Map<Integer, String> value = valueMap.get(key);
                if (value != null) {
                    System.out.print(key + ": " + value.get(resultFindData.get(key)) + "; ");
                } else {
                    System.out.print(key + ": " + resultFindData.get(key) + "; ");
                }
            }
            System.out.println();
        }
        System.out.println("Busca finalizada!");
    }

    String getWhere(Set<Integer> codeSet) {
        String where = "(";
        for (Integer code : codeSet) {
            where += code + ", ";
        }

        return where.substring(0, where.length() - 2) + ")";
    }

    String convertValueInsert(String[] data) {
        String values = "";
        for (int i = 0; i <= data.length - 2; i++) {
            values += "'" + data[i].replace("'", "''") + "',";
        }
        values += "'" + data[data.length - 1] + "'";

        return String.format("(%s), ", values);
    }

    String createColumns(List<String> columns) {
        String columnsTable = "";
        for (int i = 0; i <= columns.size() - 1; i++) {
            if (columns.get(i).equals("código")) {
                columnsTable += columns.get(i) + " numeric primary key, ";
            } else {
                String col = columns.get(i)
                        .replace(" ", "")
                        .replace("/", "")
                        .replace(".", "")
                        .replace("-", "");
                columnsTable += col + " varchar(150), ";
            }
        }

        return columnsTable.substring(0, columnsTable.length() - 2);
    }
}
