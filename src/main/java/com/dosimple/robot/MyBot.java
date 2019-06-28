package com.dosimple.robot;

import io.github.biezhi.wechat.WeChatBot;
import io.github.biezhi.wechat.api.constant.Config;

/**
 * @author baolw
 */
public class MyBot extends WeChatBot {
    public MyBot(Builder builder) {
        super(builder);
    }

    public MyBot(Config config) {
        super(config);
    }
}
