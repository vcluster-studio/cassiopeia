package xyz.vcluster.cassiopeia.common.utils.id;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import xyz.vcluster.cassiopeia.common.utils.MacTools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SnowFlake配置
 *
 * @author cassiopeia
 */
@Configuration
public class IdWorkerConfiguration {

    @Bean(name = "idWorker")
    @Primary
    public SnowflakeIdWorker idWorker() throws Exception {
        List<String> localMacs = MacTools.getLocalMacList();
        if (localMacs.size() <= 0) {
            Exception error = new Exception("系统异常，无法获取当前服务器mac地址，请检查是服务器的网络和网卡配置！");
            throw error;
        }
        Long workerId = null;
        Long datacenterId = null;
        Map<String, String> map4Roster = new HashMap<>();

        map4Roster.put(localMacs.get(0).toLowerCase(), "1,1");

        for (String macId : localMacs) {
            String ref_mac = macId.toLowerCase().trim();
            if (map4Roster.containsKey(ref_mac)) {
                String combineStr = map4Roster.get(ref_mac);
                if (StringUtils.isEmpty(combineStr)) {
                    continue;
                }
                String[] numbers = combineStr.split(",");
                if (numbers.length < 2) {
                    continue;
                }
                workerId = Long.parseLong(numbers[0]);
                datacenterId = Long.parseLong(numbers[1]);
                if (workerId > 31 || datacenterId > 31 || workerId < 0 || datacenterId < 0) {
                    Exception error = new Exception("工作id或数据中心id异常，只能在0到31之间！");
                    throw error;
                }
            } else {
                continue;
            }
        }

        if (workerId == null || datacenterId == null) {
            Exception error = new Exception("抱歉，当前服务器mac地址尚未注册，请在配置中心进行注册。");
            throw error;
        }

        SnowflakeIdWorker idWorker = new SnowflakeIdWorker(workerId, datacenterId);
        return idWorker;
    }
}
