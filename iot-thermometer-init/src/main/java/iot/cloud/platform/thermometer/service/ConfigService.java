package iot.cloud.platform.thermometer.service;

import java.util.Map;

public interface ConfigService {

  /**
   * 根据 k 获取 v值
   * @param k
   * @return
   */
  String getV(String k);

  /**
   * 保存到配置表（config），
   * @param map 装载&lt;k,v&gt;的键值对
   * @return
   */
  boolean saveConfigs(Map<String,String> map);
}
