package com.blueice.crawler.util;

import java.util.UUID;

/**
* @Description: TODO
* @author blueice
* @date 2018年12月27日 上午10:20:58
*
*/
public class UnicodeUtil
{
    public static String create() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
