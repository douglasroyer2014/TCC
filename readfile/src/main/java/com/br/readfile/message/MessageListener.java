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

    @Transactional
    @RabbitListener(queues = MQConfig.SAVE_DATA)
    public void listener(CustomMessage message) {
        readService.readyFileAndSave(message.getDirectory(), message.getFileName(), message.getTableName());
    }
}
