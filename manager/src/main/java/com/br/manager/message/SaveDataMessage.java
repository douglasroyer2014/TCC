package com.br.manager.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SaveDataMessage {

    private String messageId;
    private Date messageDate;
    private String directory;
    private String fileName;
    private String tableName;
    private String processId;
    private boolean isStructured;

}
