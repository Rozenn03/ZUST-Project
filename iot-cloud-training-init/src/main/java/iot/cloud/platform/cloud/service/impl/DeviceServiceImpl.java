package iot.cloud.platform.cloud.service.impl;

import iot.cloud.platform.cloud.entity.DeviceEntity;
import iot.cloud.platform.cloud.mapper.DeviceMapper;
import iot.cloud.platform.cloud.service.DeviceService;
import iot.cloud.platform.cloud.utils.IDUtils;
import iot.cloud.platform.cloud.vo.DeviceVo;
import iot.cloud.platform.cloud.vo.RegisterDeviceVo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DeviceServiceImpl implements DeviceService {

  @Autowired
  private DeviceMapper deviceMapper;

  @Override
  public List<DeviceEntity> getDeviceByUserId(String userId) {
    return deviceMapper.getDeviceByUserId(userId);
  }

  @Override
  public List<DeviceVo> getDevicePubInfoByUserId(String userId) {
    List<DeviceEntity> list= getDeviceByUserId(userId);
    List<DeviceVo> result=new ArrayList<>();
    if(list!=null && list.size()>0){
      DeviceVo dv=null;
      for(DeviceEntity de:list){
        dv=new DeviceVo();
        dv.setIotId(de.getIotId());
        dv.setDevName(de.getDevName());
        dv.setDevType(de.getDevType());
        dv.setStatus(de.getStatus());
        dv.setDescription(de.getDescription());
        result.add(dv);
      }
    }
    return result;
  }

  @Override
  public DeviceEntity registerDevice(RegisterDeviceVo dev,String userId) {
    DeviceVo dvo=new DeviceVo();
    dvo.setDevName(dev.getDevName());
    dvo.setDevType(dev.getDevType());
    dvo.setDescription(dev.getDescription());
    dvo.setStatus("offline");
    return createDevice(dvo,userId);
  }

  @Override
  public DeviceEntity getDevice(String userId, String iotId) {
    return deviceMapper.getDevice(userId,iotId);
  }

  @Override
  public DeviceEntity createDevice(DeviceVo dev,String userId) {
    DeviceEntity de = new DeviceEntity();
    de.setIotId(RandomStringUtils.randomAlphanumeric(16));
    de.setDevSecret(RandomStringUtils.randomAlphanumeric(16));
    de.setDevName(dev.getDevName());
    de.setDevType(dev.getDevType());
    de.setStatus(dev.getStatus());
    de.setDescription(dev.getDescription());
    de.setUserId(userId);
    de.setCreateTime(new Date());

    if (deviceMapper.createDevice(de)){
    return getDevice(userId, de.getIotId());
    }
    return null;

  }

  @Override
  public boolean updateDevice(DeviceVo dev, String userId) {
    DeviceEntity de=new DeviceEntity();
    de.setDevName(dev.getDevName());
    de.setIotId(dev.getIotId());
    de.setDevType(dev.getDevType());
    de.setStatus(dev.getStatus());
    de.setDescription(dev.getDescription());
    de.setUserId(userId);
    return deviceMapper.updateDevice(de);
  }

  @Override
  public boolean validSecret(String iotId, String secret) {
    DeviceEntity dev=deviceMapper.getDeviceByIotId(iotId);
    return dev!=null && StringUtils.equals(secret,dev.getDevSecret());
  }
}
