package iot.cloud.platform.thermometer.service.impl;

import iot.cloud.platform.thermometer.entity.ConfigEntity;
import iot.cloud.platform.thermometer.mapper.ConfigMapper;
import iot.cloud.platform.thermometer.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ConfigServiceImpl implements ConfigService {

  @Autowired
  private ConfigMapper configMapper;

  @Override
  public String getV(String k) {
    ConfigEntity config=configMapper.getConfigByK(k);
    if(config!=null){
      return config.getV();
    }else{
      return null;
    }
  }

  @Override
  public boolean saveConfigs(Map<String,String> map){
    return configMapper.saveConfigs(map)>0;
  }
}
