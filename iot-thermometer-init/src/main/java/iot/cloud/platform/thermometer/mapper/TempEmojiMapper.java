package iot.cloud.platform.thermometer.mapper;

import iot.cloud.platform.thermometer.vo.TempEmojiVo;

public interface TempEmojiMapper {

  String getFaceByTemp(int temp);
}
