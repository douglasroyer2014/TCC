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

    public void readyFileAndSave(String directory, String nameFile, String nameTable, boolean isStructured) throws Exception {
        BufferedReader br = null;
        try {
            File file = new File(directory + "\\" + nameFile);
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1));

            String st = br.readLine();
            String sql = String.format("insert into %s values ", nameTable);
            int control = 0;
            while ((st = br.readLine()) != null) {
                control += 1;
                String[] values;
                st = st.replace("'", "''");
                if (st.charAt(st.length() - 1) == ';') {
                    st = st + "$";
                    values = st.split(";");
                    values[values.length - 1] = "";
                } else {
                    values = st.split(";");
                }
                sql += String.format("(%s), ", convertValueInsert(values, isStructured));

                if (control == 2000) {
                    executeSqlService.executeSqlScript(sql.substring(0, sql.length() - 2));
                    sql = "insert into \"" + nameTable + "\" values ";
                    control = 0;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            br.close();
        }
    }

    String convertValueInsert(String[] data, boolean isStructured) {
        String values = "";
        for (int i = 0; i <= data.length - 1; i++) {
            if (isStructured) {
                try {
                    int value = Integer.valueOf(data[i]);
                    values += value + ", ";

                } catch (Exception e) {
                    values += "NULL, ";
                }
            } else {
                values += ("'" + data[i] + "', ").replace("\"", "");
            }
        }
        return values.substring(0, values.length() - 2);
    }
}
