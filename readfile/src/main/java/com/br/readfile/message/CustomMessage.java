package com.br.readfile.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CustomMessage {

    private String messageId;
    private Date messageDate;
    private String directory;
    private String fileName;
    private String tableName;
    private String processId;

}
