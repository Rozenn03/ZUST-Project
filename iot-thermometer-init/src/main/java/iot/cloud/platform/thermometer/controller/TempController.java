package iot.cloud.platform.thermometer.controller;

import iot.cloud.platform.thermometer.config.Const;
import iot.cloud.platform.thermometer.service.ConfigService;
import iot.cloud.platform.thermometer.service.HttpService;
import iot.cloud.platform.thermometer.service.TempEmojiService;
import iot.cloud.platform.thermometer.vo.DeviceMsgVo;
import iot.cloud.platform.thermometer.vo.ResMsg;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class TempController {

  private HttpService httpService=HttpService.RETROFIT.create(HttpService.class);

  @Autowired
  private ConfigService configService;

  @Autowired
  private TempEmojiService tempEmojiServiceImpl;

  /**
   * 展示温度和表情
   * @param temp 温度
   * @return
   */
  @RequestMapping("/showTempEmoji")
  @ResponseBody
  public ResMsg showTempEmoji(@RequestParam("temp") String temp){
    ResMsg result=new ResMsg();
    boolean isValidInt=false;
    if(NumberUtils.isNumber(temp)){
      Integer itemp=null;
      try {
        itemp = Integer.valueOf(temp);
        isValidInt=true;
      }catch(NumberFormatException e){
      }
      if(isValidInt){ //如果温度的值是合法的
        // 1. 根据温度获取表情
        String face = tempEmojiServiceImpl.getFaceByTemp(itemp);

        // 2. 如果表情为空，设置默认表情
        if(face == null || face.trim().isEmpty()){
          face = "❓";
        }

        // 3. 封装返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("temp", itemp);
        data.put("emoji", face);

        // 4. 设置成功返回
        result.setErrcode("0");
        result.setErrmsg("更新温度成功");
        result.setData(data);
        return result;
      }
    }
    // 如果温度不合法
    result.setErrcode("-1");
    result.setErrmsg("温度格式不正确");
    return result;
  }

  /**
   * 发送设备消息到物联网云平台并保存
   * @param temp 当前温度
   * @return
   */
  @RequestMapping("/sendDeviceMsg")
  @ResponseBody
  public ResMsg sendDeviceMsg(@RequestParam("temp") String temp){
    ResMsg result=new ResMsg();
    result.setErrcode("9002");
    result.setErrmsg("发送失败");
    String iotId=configService.getV(Const.CONFIG_K_IOTID);
    String devSecret=configService.getV(Const.CONFIG_K_DEVSECRET);
    if (StringUtils.isBlank(iotId) || StringUtils.isBlank(devSecret)) {
      result.setErrcode("9003");
      result.setErrmsg("物联网云平台ID或设备密钥不能为空");
      return result;
    }
    Map<String,Object> map=new HashMap<>();
    map.put("temp",Integer.valueOf(temp));
    map.put("time",new Date().getTime());
    DeviceMsgVo msg=new DeviceMsgVo();
    msg.setIotId(iotId);
    msg.setMsg(map);
    msg.setTag("temp");
    try {
      Call<ResMsg> call = httpService.sendDeviceMsg(iotId, devSecret, msg);
      Response<ResMsg> response = call.execute();
      if (response.isSuccessful() && response.body() != null) {
        result = response.body();
      } else {
        result.setErrcode("9004");
        result.setErrmsg("调用API失败");
      }
    } catch (IOException e) {
      result.setErrcode("9001");
      result.setErrmsg("无法连接服务器");
    }
    return result;
  }

}
