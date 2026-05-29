package iot.cloud.platform.thermometer.controller;

import iot.cloud.platform.thermometer.entity.EmojiEntity;
import iot.cloud.platform.thermometer.service.EmojiService;
import iot.cloud.platform.thermometer.vo.ResMsg;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;

@Controller
public class EmojiController {

  @Autowired
  private EmojiService emojiService;

  /**
   * 更新表情符号
   * @param emoji
   * @return
   */
  public ResMsg updateEmojiFace(@Valid EmojiEntity emoji){
    ResMsg result=new ResMsg();
    if(emojiService.updateEmojiFace(emoji.getName(),emoji.getFace())){
      result.setErrmsg("表情更新成功");
      result.setErrcode("0");
    }else{
      result.setErrmsg("表情更新失败");
      result.setErrcode("8191");
    }
    return result;
  }
}
