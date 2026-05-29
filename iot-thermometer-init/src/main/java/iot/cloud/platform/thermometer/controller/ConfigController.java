package iot.cloud.platform.thermometer.controller;

import iot.cloud.platform.thermometer.config.Const;
import iot.cloud.platform.thermometer.service.ConfigService;
import iot.cloud.platform.thermometer.service.HttpService;
import iot.cloud.platform.thermometer.vo.DeviceRegisterVo;
import iot.cloud.platform.thermometer.vo.ResMsg;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ConfigController {

  private HttpService httpService = HttpService.RETROFIT.create(HttpService.class);

  @Autowired
  private ConfigService configService;

  /**
   * 配置页面
   * @param model
   * @return
   */
  @RequestMapping("/config")
  public String configIndex(Model model) {
    model.addAttribute(Const.CONFIG_K_IOTID, configService.getV(Const.CONFIG_K_IOTID));
    model.addAttribute(Const.CONFIG_K_USERSECRET, configService.getV(Const.CONFIG_K_USERSECRET));
    model.addAttribute(Const.CONFIG_K_USERID, configService.getV(Const.CONFIG_K_USERID));
    model.addAttribute(Const.CONFIG_K_TOKEN, configService.getV(Const.CONFIG_K_TOKEN));
    model.addAttribute(Const.CONFIG_K_DEVSECRET, configService.getV(Const.CONFIG_K_DEVSECRET));
    model.addAttribute(Const.CONFIG_K_DEVNAME, configService.getV(Const.CONFIG_K_DEVNAME));
    model.addAttribute(Const.CONFIG_K_DEVTYPE, configService.getV(Const.CONFIG_K_DEVTYPE));
    model.addAttribute(Const.CONFIG_K_DESCRIPTION, configService.getV(Const.CONFIG_K_DESCRIPTION));
    model.addAttribute(Const.CONFIG_K_DEVTOPICPOST, configService.getV(Const.CONFIG_K_DEVTOPICPOST));
    model.addAttribute(Const.CONFIG_K_DEVTOPICPOSTREPLY, configService.getV(Const.CONFIG_K_DEVTOPICPOSTREPLY));
    model.addAttribute(Const.CONFIG_K_DEVTOPICRECEIVE, configService.getV(Const.CONFIG_K_DEVTOPICRECEIVE));
    model.addAttribute(Const.CONFIG_K_DEVTOPICRECEIVEREPLY, configService.getV(Const.CONFIG_K_DEVTOPICRECEIVEREPLY));
    return "config";
  }

  /**
   * 保存配置
   * @param map
   * @return
   */
  @RequestMapping("/saveConfig")
  @ResponseBody
  public ResMsg saveConfig(@RequestBody Map<String, String> map) {
    ResMsg msg = new ResMsg();
    if (configService.saveConfigs(map)) {
      msg.setErrcode("0");
      msg.setErrmsg("配置保存成功");
    } else {
      msg.setErrcode("9001");
      msg.setErrmsg("配置保存失败");
    }
    return msg;
  }

  /**
   * 调用物联网云平台 API 获取用户令牌
   * @param map
   * @return
   */
  @RequestMapping("/getToken")
  @ResponseBody
  public ResMsg getToken(@RequestBody Map<String, String> map) {
    ResMsg result = new ResMsg();
    result.setErrcode("8004");
    result.setErrmsg("用户ID，用户密钥不能为空");
    //从请求参数中读取 userId 和 userSecret
    String userId = map.get(Const.CONFIG_K_USERID);
    String userSecret = map.get(Const.CONFIG_K_USERSECRET);
    if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userSecret)) {
      try {
        Call<ResMsg> call = httpService.getUserToken(userId, userSecret);
        Response<ResMsg> response = call.execute();
        if (response.isSuccessful() && response.body() != null) {
          result = response.body();
          if ("0".equals(result.getErrcode())) {
            Map<String, Object> data = (Map<String, Object>) result.getData();
            String token = (String) data.get("token");
            Map<String, String> configMap = new HashMap<>();
            configMap.put(Const.CONFIG_K_TOKEN, token);
            configMap.put(Const.CONFIG_K_USERID, userId);
            configMap.put(Const.CONFIG_K_USERSECRET, userSecret);
            if (configService.saveConfigs(configMap)) {
              result.setErrcode("0");
              result.setErrmsg("获取令牌成功并保存");
            } else {
              result.setErrcode("8006");
              result.setErrmsg("令牌保存失败");
            }
          }
        } else {
          result.setErrcode("8007");
          result.setErrmsg("调用API失败");
        }
      } catch (IOException e) {
        result.setErrcode("8001");
        result.setErrmsg("无法连接服务器");
      }
    }
    return result;
  }


  /**
   * 调用物联网云平台 API 注册设备，并保存物联网云平台设备IotID和设备密钥
   * @param map
   * @return
   */
  @RequestMapping("/registerDevice")
  @ResponseBody
  public ResMsg registerDevice(@RequestBody Map<String, String> map) {
    ResMsg result = new ResMsg();
    result.setErrcode("8003");
    result.setErrmsg("设备名称，类型，描述，用户令牌不能为空");
    String devName = map.get(Const.CONFIG_K_DEVNAME);
    String devType = map.get(Const.CONFIG_K_DEVTYPE);
    String description = map.get(Const.CONFIG_K_DESCRIPTION);
    String token = map.get(Const.CONFIG_K_TOKEN);
    if (StringUtils.isNotBlank(token)
            && StringUtils.isNotBlank(devName)
            && StringUtils.isNotBlank(devType)
            && StringUtils.isNotBlank(description)
    ) {
      DeviceRegisterVo vo = new DeviceRegisterVo();
      vo.setDescription(description);
      vo.setDevName(devName);
      vo.setDevType(devType);
      try {
        Call<ResMsg> call = httpService.registerDevice(token, vo);
        Response<ResMsg> response = call.execute();
        if (response.isSuccessful() && response.body() != null) {
          ResMsg apiResult = response.body();
          if ("0".equals(apiResult.getErrcode())) {
            Map<String, Object> data = (Map<String, Object>) apiResult.getData();
            String iotId = (String) data.get("iotId");
            String devSecret = (String) data.get("devSecret");
            
            String topicPrefix = "/iot/cloud/" + iotId;
            String devTopicPost = topicPrefix + "/post";
            String devTopicPostReply = topicPrefix + "/post_reply";
            String devTopicReceive = topicPrefix + "/receive";
            String devTopicReceiveReply = topicPrefix + "/receive_reply";
            
            Map<String, String> configMap = new HashMap<>();
            configMap.put(Const.CONFIG_K_IOTID, iotId);
            configMap.put(Const.CONFIG_K_DEVSECRET, devSecret);
            configMap.put(Const.CONFIG_K_DEVTOPICPOST, devTopicPost);
            configMap.put(Const.CONFIG_K_DEVTOPICPOSTREPLY, devTopicPostReply);
            configMap.put(Const.CONFIG_K_DEVTOPICRECEIVE, devTopicReceive);
            configMap.put(Const.CONFIG_K_DEVTOPICRECEIVEREPLY, devTopicReceiveReply);
            
            if (configService.saveConfigs(configMap)) {
              result.setErrcode("0");
              result.setErrmsg("设备注册成功并保存");
              Map<String, Object> returnData = new HashMap<>();
              returnData.put("iotId", iotId);
              returnData.put("devSecret", devSecret);
              returnData.put("devTopicPost", devTopicPost);
              returnData.put("devTopicPostReply", devTopicPostReply);
              returnData.put("devTopicReceive", devTopicReceive);
              returnData.put("devTopicReceiveReply", devTopicReceiveReply);
              result.setData(returnData);
            } else {
              result.setErrcode("8006");
              result.setErrmsg("设备信息保存失败");
            }
          } else {
            result.setErrcode(apiResult.getErrcode());
            result.setErrmsg(apiResult.getErrmsg());
          }
        } else {
          result.setErrcode("8007");
          result.setErrmsg("调用注册设备API失败");
        }
      } catch (IOException e) {
        result.setErrcode("8001");
        result.setErrmsg("无法连接服务器");
      }
    }
    return result;
  }
}
