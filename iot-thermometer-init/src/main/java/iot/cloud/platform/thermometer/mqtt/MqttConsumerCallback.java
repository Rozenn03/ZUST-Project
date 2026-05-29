package iot.cloud.platform.thermometer.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iot.cloud.platform.thermometer.config.Const;
import iot.cloud.platform.thermometer.controller.EmojiController;
import iot.cloud.platform.thermometer.entity.EmojiEntity;
import iot.cloud.platform.thermometer.service.ConfigService;
import iot.cloud.platform.thermometer.utils.ExcptUtil;
import iot.cloud.platform.thermometer.vo.MqttMsg;
import iot.cloud.platform.thermometer.vo.ResMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * mqtt回调处理类
 */

@Component
@Slf4j
public class MqttConsumerCallback implements MqttCallbackExtended {

    @Autowired
    private MqttClient client;
    @Autowired
    private MqttConnectOptions options;

    @Autowired
    @Qualifier("mqttSubTopics")
    private Map<String,Integer> mqttSubTopics;

    @Autowired
    @Qualifier("mqttTopicReplyMap")
    private Map<String,String> mqttTopicReplyMap;

    @Autowired
    private ConfigService configService;

    @Autowired
    private EmojiController emojiController;
    /**
     * 断开重连
     */
    @Override
    public void connectionLost(Throwable cause) {
        log.info("MQTT连接断开",cause);
        int tryTimes=3,count=0;
        while(!client.isConnected()) {
            try {
                client.reconnect();
                count=0;
            } catch (Exception e) {
                count++;
                log.error(ExcptUtil.filterStack(e));
            }
            log.info("尝试重新连接"+count+"次");
            if(count>=tryTimes){
                break;
            }
        }

    }

    /**
     * 消息处理
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String msg = new String(message.getPayload());
        log.info("收到主题[" + topic + "]消息 ->" + msg);

        ResMsg returnVal = new ResMsg();
        returnVal.setErrcode("8199");
        returnVal.setErrmsg("消息处理异常");

        String eventId = "";
        String eventName = "";
        long eventTime = System.currentTimeMillis();

        try {
            ObjectMapper objMapper = new ObjectMapper();
            MqttMsg<HashMap> resvMsg = objMapper.readValue(msg, MqttMsg.class);

            if (resvMsg == null) {
                returnVal.setErrcode("8194");
                returnVal.setErrmsg("消息解析失败");
                log.error("消息解析失败：resvMsg 为 null");
            } else {
                eventId = resvMsg.getEventId() != null ? resvMsg.getEventId() : "";
                eventName = resvMsg.getEventName() != null ? resvMsg.getEventName() : "";
                eventTime = resvMsg.getEventTime();

                String eventNameVal = resvMsg.getEventName();
                HashMap<String, Object> data = resvMsg.getData();

                if ("updateEmojiFace".equals(eventNameVal)) {
                    if (data == null) {
                        returnVal.setErrcode("8195");
                        returnVal.setErrmsg("消息数据为空");
                    } else {
                        Object nameObj = data.get("name");
                        Object faceObj = data.get("face");

                        if (nameObj == null || faceObj == null) {
                            returnVal.setErrcode("8192");
                            returnVal.setErrmsg("表情名称或表情字符不能为空");
                        } else {
                            String name = nameObj.toString();
                            String face = faceObj.toString();

                            if (StringUtils.isBlank(name) || StringUtils.isBlank(face)) {
                                returnVal.setErrcode("8192");
                                returnVal.setErrmsg("表情名称或表情字符不能为空");
                            } else {
                                EmojiEntity emoji = new EmojiEntity();
                                emoji.setName(name.trim());
                                emoji.setFace(face.trim());

                                returnVal = emojiController.updateEmojiFace(emoji);
                            }
                        }
                    }
                } else {
                    returnVal.setErrcode("8193");
                    returnVal.setErrmsg("不支持的事件类型: " + eventNameVal);
                    log.warn("收到不支持的事件类型: " + eventNameVal);
                }
            }
        } catch (JsonProcessingException e) {
            returnVal.setErrcode("8194");
            returnVal.setErrmsg("消息JSON解析失败");
            log.error("消息JSON解析失败: " + ExcptUtil.filterStack(e));
        } catch (Exception e) {
            returnVal.setErrcode("8199");
            returnVal.setErrmsg("消息处理异常: " + e.getMessage());
            log.error("消息处理异常: " + ExcptUtil.filterStack(e));
        }

        try {
            Map<String, Object> replyJson = new HashMap<>();
            replyJson.put("eventId", eventId);
            replyJson.put("eventName", eventName);
            replyJson.put("eventTime", eventTime);
            replyJson.put("resMsg", returnVal);

            ObjectMapper objMapper = new ObjectMapper();
            String receiveReplyMsg = objMapper.writeValueAsString(replyJson);
            log.info("回复MQTT消息：" + receiveReplyMsg);

            String sendTopic = mqttTopicReplyMap.get(topic);
            if (StringUtils.isBlank(sendTopic)) {
                String iotId = configService.getV(Const.CONFIG_K_IOTID);
                if (StringUtils.isNotBlank(iotId)) {
                    String devRecvTopic = "/iot/cloud/" + iotId + "/receive";
                    String devRecvReplyTopic = "/iot/cloud/" + iotId + "/receive_reply";
                    if (devRecvTopic.equals(topic)) {
                        sendTopic = devRecvReplyTopic;
                    }
                }
            }
            if (StringUtils.isNotBlank(sendTopic)) {
                log.info("send topic :{} -> {}", sendTopic, receiveReplyMsg);
                MqttMessage replyMqttMessage = new MqttMessage(receiveReplyMsg.getBytes(StandardCharsets.UTF_8));
                replyMqttMessage.setQos(1);
                client.publish(sendTopic, replyMqttMessage);
            } else {
                log.warn("未找到主题[" + topic + "]对应的回复主题");
            }
        } catch (Exception e) {
            log.error("回复消息发送失败: " + ExcptUtil.filterStack(e));
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    @Override
    public void connectComplete(boolean b, String s) {
        mqttSubTopics.forEach((k,v) ->{
            try {
                client.subscribe(k,v);
                log.info("订阅主题:"+k);
            } catch (MqttException e) {
                log.error(ExcptUtil.filterStack(e));
            }
        });
    }
}