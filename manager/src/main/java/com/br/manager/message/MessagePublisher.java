package com.br.manager.message;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessagePublisher {

    private RabbitTemplate template;

    public void publishMessageSaveData(String directory, String fileName, String tableName, String processId, boolean isStructured) {
        SaveDataMessage message = new SaveDataMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setMessageDate(new Date());
        message.setDirectory(directory);
        message.setFileName(fileName);
        message.setTableName(tableName);
        message.setProcessId(processId);
        message.setStructured(isStructured);
        template.convertAndSend(MQConfig.SAVE_DATA, message);
    }

    public void publishMessageFIndData(String tableName, String defaultSearch, String valueDefaultSearch, String fieldSearch, String valueSearch, String processId,
                                       boolean structured) {
        FindDataMessage message = new FindDataMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setMessageDate(new Date());
        message.setTableName(tableName);
        message.setDefaultSearch(defaultSearch);
        message.setValueSearchDefault(valueDefaultSearch);
        message.setFieldSearch(fieldSearch);
        message.setValueSearch(valueSearch);
        message.setProcessId(processId);
        message.setStructured(structured);
        template.convertAndSend(MQConfig.FIND_DATA, message);
    }
}
