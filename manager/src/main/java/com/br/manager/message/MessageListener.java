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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

            List<String> result = jedis.lrange(String.format("%s:%s", message.getProcessId(), message.getMessageType()), 0, -1);

            Gson gson = new Gson();
            List<Map<String, Integer>> valueResult = new ArrayList<>();
            for (String value : result) {
                valueResult.add(gson.fromJson(value, new TypeToken<Map<String, Integer>>() {
                }.getType()));
            }

            Map<String, Set<Integer>> codeMap = new HashMap<>();

            for (String key : valueResult.get(0).keySet()) {
                codeMap.put(key, new HashSet<>());
            }

            for (Map<String, Integer> resultFindData : valueResult) {
                for (String key : resultFindData.keySet()) {
                    codeMap.get(key).add(resultFindData.get(key));
                }
            }

            Map<String, Map<Integer, String>> valueMap = new HashMap<>();

            for (String key : codeMap.keySet()) {
                String where = getWhere(codeMap.get(key));
                try {
                    valueMap.put(key, executeSqlService.getCodeAndValue(key, where));
                } catch (Exception e) {
                    //donothing
                }
            }

            for (Map<String, Integer> resultFindData : valueResult) {
                for (String key : resultFindData.keySet()) {
                    Map<Integer, String> value = valueMap.get(key);
                    if (value != null) {
                        System.out.print(key + ": " + value.get(resultFindData.get(key)) + "; ");
                    } else {
                        System.out.print(key + ": " + resultFindData.get(key) + "; ");
                    }
                }
                System.out.println();
            }
        }
    }

    String getWhere(Set<Integer> codeSet) {
        String where = "(";
        for (Integer code : codeSet) {
            where += code + ", ";
        }

        return where.substring(0, where.length() - 2) + ")";
    }
}
