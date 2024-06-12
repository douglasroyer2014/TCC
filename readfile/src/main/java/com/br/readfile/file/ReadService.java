package com.br.readfile.file;

import com.br.readfile.scriptsql.ExecuteSqlService;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadService {

    ExecuteSqlService executeSqlService;

    public void readyFileAndSave(String directory, String nameFile, String nameTable) {

        try {
            File file = new File(directory + "\\" + nameFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1));

            String st = br.readLine();
            String sql = String.format("insert into %s values ", nameTable);
            int control = 0;
            while ((st = br.readLine()) != null) {
                control += 1;
                sql += String.format("(%s), ", convertValueInsertNumeric(st.split(";")));

                if (control == 100000) {
                    try {
                        executeSqlService.executeSqlScript(sql.substring(0, sql.length() - 2));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    sql = "insert into caged values ";
                    control = 0;
                }

            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    String convertValueInsertNumeric(String[] data) {
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
}
