package com.br.finddata.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ManagerMessage {

    private String messageId;
    private Date messageDate;
    private String processId;
    private String messageType;
}
