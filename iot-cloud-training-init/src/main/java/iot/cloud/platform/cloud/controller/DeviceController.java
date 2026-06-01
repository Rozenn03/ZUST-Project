package iot.cloud.platform.cloud.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import iot.cloud.platform.cloud.constants.Const;
import iot.cloud.platform.cloud.entity.DeviceEntity;
import iot.cloud.platform.cloud.entity.TokenEntity;
import iot.cloud.platform.cloud.service.DeviceService;
import iot.cloud.platform.cloud.service.TokenService;
import iot.cloud.platform.cloud.vo.DeviceVo;
import iot.cloud.platform.cloud.vo.RedirectVo;
import iot.cloud.platform.cloud.vo.RegisterDeviceVo;
import iot.cloud.platform.cloud.vo.ResMsg;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
public class DeviceController {

  @Autowired
  public HttpSession session;

  @Autowired
  private DeviceService deviceService;

  @Autowired
  private TokenService tokenService;

  /**
   * 获取用户所有设备信息（开放API）
   * @param token 用户令牌
   * @return
   */
  @GetMapping("/device_all")
  @ResponseBody
  @ApiOperation("获取用户所有设备")
  @ApiImplicitParams({
          @ApiImplicitParam(name = "token",value = "用户Token",required = true,paramType = "query",dataType = "string"),
  })
  public Object listDevice(@RequestParam("token")String token){
    ResMsg resMsg=new ResMsg();
    TokenEntity te=tokenService.getToken(token);
    String userId=te.getUserId();
    if(userId!=null) {
      List<DeviceVo> devList = deviceService.getDevicePubInfoByUserId(userId);
      resMsg.setErrcode("0");
      resMsg.setErrmsg("Success");
      resMsg.setData(devList);
    }else{
      resMsg.setErrcode("5001");
      resMsg.setErrmsg("令牌无效");
    }
    return resMsg;
  }

  /**
   * 注册新设备（开放API）
   * @param token 用户令牌
   * @param dev 设备信息
   * @return
   */
  @PostMapping("/device_register")
  @ResponseBody
  @ApiOperation("注册新设备")
  @ApiImplicitParams({
          @ApiImplicitParam(name = "token",value = "用户Token",required = true,paramType = "query",dataType = "string"),
  })
  public Object registerDevice(@RequestParam("token")String token,@Valid @RequestBody @ApiParam(name = "dev",value = "设备信息",required = true) RegisterDeviceVo dev){
    ResMsg resMsg=new ResMsg();
    TokenEntity te=tokenService.getToken(token);
    String userId=te.getUserId();
    if(userId!=null) {
      DeviceEntity deviceEntity = deviceService.registerDevice(dev, userId);
      if (deviceEntity != null) {
        resMsg.setErrcode("0");
        resMsg.setErrmsg("设备注册成功");
        resMsg.setData(deviceEntity);
      } else {
        resMsg.setErrcode("30001");
        resMsg.setErrmsg("设备注册失败");
      }
    }else{
      resMsg.setErrcode("5001");
      resMsg.setErrmsg("令牌无效");
    }

    return resMsg;
  }

  /**
   * 获取单台设备信息（开放API）
   * @param token 用户令牌
   * @param iotId 物联网云平台设备IoT ID
   * @return
   */
  @GetMapping("/device_get")
  @ResponseBody
  @ApiOperation("获取单台设备信息")
  @ApiImplicitParams({
          @ApiImplicitParam(name = "token",value = "用户Token",required = true,paramType = "query",dataType = "string"),
          @ApiImplicitParam(name = "iotId",value = "设备物联网平台ID",required = true,paramType = "query",dataType = "string"),
  })
  public Object getDevice(@RequestParam("token")String token,@RequestParam("iotId") String iotId){
    ResMsg resMsg=new ResMsg();
    TokenEntity te=tokenService.getToken(token);
    String userId=te.getUserId();
    if(userId!=null) {
      DeviceEntity deviceEntity = deviceService.getDevice(userId,iotId);
      resMsg.setErrcode("0");
      resMsg.setErrmsg("Success");
      resMsg.setData(deviceEntity);
    }else{
      resMsg.setErrcode("5001");
      resMsg.setErrmsg("令牌无效");
    }
    return resMsg;
  }

  /**
   * 新建设备
   * @param dev 设备信息
   * @return
   */
  @RequestMapping("/device_create")
  @ResponseBody
  public Object createDevice(@Valid @RequestBody DeviceVo dev){
    String userId=(String)session.getAttribute(Const.SESSION_KEY_USERID);
    ResMsg resMsg=new ResMsg();
    try{
      DeviceEntity de=deviceService.createDevice(dev,userId);
      if(de!=null){
        resMsg.setErrcode("0");
        resMsg.setErrmsg("设备创建成功");
        java.util.Map<String,Object> data=new java.util.HashMap<>();
        data.put("url","device_list");
        resMsg.setData(data);
      }else{
        resMsg.setErrcode("30001");
        resMsg.setErrmsg("设备创建失败");
      }
    }catch (Exception e){
      e.printStackTrace();
      resMsg.setErrcode("30001");
      resMsg.setErrmsg("设备创建失败");
    }
    return resMsg;
  }

  /**
   * 更新设备
   * @param dev 设备信息
   * @return
   */
  @RequestMapping("/device_update")
  @ResponseBody
  public Object updateDevice(@Valid @RequestBody DeviceVo dev){
    String userId=(String)session.getAttribute(Const.SESSION_KEY_USERID);
    ResMsg resMsg=new ResMsg();
    if(deviceService.updateDevice(dev,userId)){
      resMsg.setErrcode("0");
      resMsg.setErrmsg("设备更新成功");
      resMsg.setData(new RedirectVo("/device_list?random="+ RandomStringUtils.randomAlphanumeric(10)));
    }else{
      resMsg.setErrcode("30002");
      resMsg.setErrmsg("设备更新失败");
    }
    return resMsg;
  }

  /**
   * 查看设备列表
   * @param mp
   * @return
   */
  @RequestMapping("/device_list")
  public String getDeviceList(ModelMap mp){
    String userId=(String)session.getAttribute(Const.SESSION_KEY_USERID);
    List<DeviceEntity> devList=deviceService.getDeviceByUserId(userId);
    mp.addAttribute("devList",devList);
    return "device_list";
  }

  /**
   * 新增设备页面
   * @param mp
   * @return
   */
  @RequestMapping("/device_new")
  public String getDeviceNew(ModelMap mp){
    return "device_new";
  }

  /**
   * 查看单台设备信息
   * @param mp
   * @param iotId
   * @return
   */
  @RequestMapping("/device/{iotId}")
  public String getDevice(ModelMap mp,@PathVariable("iotId") String iotId){
    String userId=(String)session.getAttribute(Const.SESSION_KEY_USERID);
    DeviceEntity dev=deviceService.getDevice(userId,iotId);
    if(dev==null){
      return "404";
    }else{
      mp.addAttribute("dev",dev);
      return "device_edit";
    }
  }
}
