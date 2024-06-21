package com.br.readfile.message;

import com.br.readfile.file.ReadService;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageListener {

    ReadService readService;
    MessagePublisher messagePublisher;

    @Transactional
    @RabbitListener(queues = MQConfig.SAVE_DATA)
    public void listener(CustomMessage message) {
        try {
            readService.readyFileAndSave(message.getDirectory(), message.getFileName(), message.getTableName(), message.isStructured());
        } catch (Exception e) {
            e.printStackTrace();
        }
        messagePublisher.publishMessageSaveData(message.getProcessId(), message.isStructured(), message.getTableName());
    }
}
