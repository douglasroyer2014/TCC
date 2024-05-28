package com.br.manager.message;

import com.br.manager.script.ExecuteSqlService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.br.manager.caged.Controller.PROCESS_TOTAL;

@Service
@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageListener {

    ExecuteSqlService executeSqlService;

    @Transactional
    @RabbitListener(queues = MQConfig.MANAGER)
    public void listener(ManagerMessage message) {

        Jedis jedis = new Jedis("localhost");
        int total = jedis.incr(String.format("%S:%s", message.getProcessId(), message.getMessageType())).intValue();

        Integer processValue = PROCESS_TOTAL.getIfPresent(message.getProcessId());

        if (processValue != null && processValue == total) {
            System.out.println("Gravação dos arquivos finalizado com sucesso!");

            List<String> result = jedis.lrange(String.format("%s:%s", message.getProcessId(), message.getMessageType()), 0, -1);

            Gson gson = new Gson();
            List<Map<String, Integer>> valueResult = new ArrayList<>();
            for (String value : result) {
                valueResult.add(gson.fromJson(value, new TypeToken<Map<String, Integer>>() {
                }.getType()));
            }

            System.out.println();
            for (Map<String, Integer> resultFindData : valueResult) {

                for (String key : resultFindData.keySet()) {
                    String description;
                    try {
                        description = executeSqlService.getDescription(key, resultFindData.get(key));
                    } catch (Exception e) {
                        description = "" + resultFindData.get(key);
                    }

                    System.out.print(key + ": " + description + "; ");
                }

                System.out.println();
            }

        }
    }
}
