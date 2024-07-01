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
public class FindDataMessage {

    private String messageId;
    private Date messageDate;
    private String tableName;
    private String defaultSearch;
    private String valueSearchDefault;
    private String fieldSearch;
    private String valueSearch;
    private String processId;
    private boolean structured;
}
