package com.br.finddata.message;

import com.br.finddata.entity.FindService;
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

    FindService findService;

    @Transactional
    @RabbitListener(queues = MQConfig.FIND_DATA)
    public void listener(CustomMessage message) {
        findService.findData(message.getTableName(), message.getDefaultSearch(), message.getDefaultValue(), message.getFieldSearch(), Integer.valueOf(message.getValueSearch()));
    }
}
