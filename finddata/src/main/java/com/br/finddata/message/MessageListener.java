package com.br.finddata.message;

import com.br.finddata.entity.FindService;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

@Service
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageListener {

    FindService findService;
    MessagePublisher messagePublisher;

    @Transactional
    @RabbitListener(queues = MQConfig.FIND_DATA)
    public void listener(CustomMessage message) {
        List<Map<String, Integer>> findList = findService
                .findData(message.getTableName(), message.getDefaultSearch(), message.getValueSearchDefault(), message.getFieldSearch(), message.getValueSearch());
        Jedis jedis = new Jedis("201.54.201.31", 6379);

        Gson gson = new Gson();
        for (Map<String, Integer> user : findList) {
            String json = gson.toJson(user);
            jedis.rpush(String.format("%s:%s", message.getProcessId(), "FIND_DATA"), json);
        }

        messagePublisher.publishMessageFindDataManager(message.getProcessId());
    }
}
