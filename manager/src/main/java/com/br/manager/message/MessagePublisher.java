package com.br.manager.message;

import com.br.manager.caged.CagedEntity;
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

    public void publishMessageSaveData(String directory, String fileName, String tableName) {
        SaveDataMessage message = new SaveDataMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setMessageDate(new Date());
        message.setDirectory(directory);
        message.setFileName(fileName);
        message.setTableName(tableName);
        template.convertAndSend(MQConfig.SAVE_DATA, message);
    }

    public void publishMessageFIndData(CagedEntity entity) {
        FindDataMessage message = new FindDataMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setMessageDate(new Date());
        message.setTableName(entity.getTableName());
        message.setDefaultSearch(entity.getDefaultSearch());
        message.setDefaultValue(entity.getDefaultValue());
        message.setFieldSearch(entity.getFieldSearch());
        message.setValueSearch(entity.getValueSearch());
        template.convertAndSend(MQConfig.FIND_DATA, message);
    }
}
