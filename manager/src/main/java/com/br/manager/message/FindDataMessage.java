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
    private int defaultValue;
    private String fieldSearch;
    private String valueSearch;
}
