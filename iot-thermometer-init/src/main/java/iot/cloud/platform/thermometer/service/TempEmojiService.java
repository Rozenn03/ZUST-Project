package iot.cloud.platform.thermometer.service;

import iot.cloud.platform.thermometer.vo.TempEmojiVo;

public interface TempEmojiService {
  /**
   * 根据温度获取表情符号
   * @param temp 温度
   * @return
   */
  String getFaceByTemp(int temp);

}
