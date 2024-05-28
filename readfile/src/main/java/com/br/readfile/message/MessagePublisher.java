package com.br.readfile.message;

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

    public void publishMessageSaveData(String processId) {
        ManagerMessage managerMessage = new ManagerMessage();
        managerMessage.setMessageId(UUID.randomUUID().toString());
        managerMessage.setMessageDate(new Date());
        managerMessage.setProcessId(processId);
        managerMessage.setMessageType("READ_FILE");
        template.convertAndSend(MQConfig.MANAGER, managerMessage);
    }
}
