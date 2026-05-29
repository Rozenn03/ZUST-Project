package iot.cloud.platform.thermometer.service.impl;

import iot.cloud.platform.thermometer.mapper.TempEmojiMapper;
import iot.cloud.platform.thermometer.service.TempEmojiService;
import iot.cloud.platform.thermometer.vo.TempEmojiVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TempEmojiServiceImpl implements TempEmojiService {

  @Autowired
  private TempEmojiMapper tempEmojiMapper;

  @Override
  public String getFaceByTemp(int temp) {
    return tempEmojiMapper.getFaceByTemp(temp);
  }
}
