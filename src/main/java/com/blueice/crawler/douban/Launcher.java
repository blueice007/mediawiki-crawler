package com.blueice.crawler.douban;

import org.slf4j.LoggerFactory;

import com.blueice.crawler.douban.constants.Tags;

/**
* @Description: TODO
* @author blueice
* @date 2018年12月27日 下午8:20:44
*
*/
public class Launcher
{
    public static void main(String[] args)
    {
        /**
         * java -jar xxx.jar [参数]
         * 参数：
         *  -t 爬取数据类型  1电影 2电视剧 3综艺  默认 1
         *  -b 开始下标 默认0
         *  -e 结束下标 默认 intMax
         *  -s 排序 按热度 U 评分S 标签数T 上映时间R 默认 U
         *  -w 爬虫执行间隔(防封杀) 默认 7 单位/秒
         *  -h 帮助
         *  样例：java -jar xxx.jar -t 1 -b 100 -e 200 -s U -w 10
         */
        Tags type = Tags.MOVIE;
        int start = 0;
        int end = Integer.MAX_VALUE;
        String sort = "U";
        int waitSecond = 7;
        String logName = "crawler.movie";
        if(args!=null) {
            for(int i=0,len=args.length;i<len;i++) {
                if(args[i].equalsIgnoreCase("-t")) {
                    switch (Integer.parseInt(args[++i]))
                    {
                        case 2:
                            type = Tags.TV;
                            logName = "crawler.tv";
                            break;
                        case 3:
                            type = Tags.VARIETY;
                            logName = "crawler.variety";
                            break;
                        default:
                            break;
                    }
                }else if(args[i].equalsIgnoreCase("-b")) {
                    start = Integer.parseInt(args[++i]);
                }else if(args[i].equalsIgnoreCase("-e")) {
                    end = Integer.parseInt(args[++i]);
                }else if(args[i].equalsIgnoreCase("-s")) {
                    sort = args[++i].toUpperCase();
                }else if(args[i].equalsIgnoreCase("-w")) {
                    waitSecond = Integer.parseInt(args[++i]);
                }else if(args[i].equalsIgnoreCase("-h")) {
                    printHelpInfo();
                    return;
                }
            }
        }
        System.out.println("crawler start ...");
        new Thread(new CrawlerTask(LoggerFactory.getLogger(logName), type,start,end,sort,waitSecond))
        .start();
    }

    /**
    * <p>方法描述</p>
    * @author blueice
    * @date 2018年12月28日 下午3:12:30
    */
    private static void printHelpInfo()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("帮助说明: java -jar xxx.jar [参数]\r\n")
        .append("   参数：\r\n")
        .append("       -t 爬取数据类型  1电影 2电视剧 3综艺  默认 1\r\n")
        .append("       -b 开始下标 默认0\r\n")
        .append("       -e 结束下标 默认 intMax\r\n")
        .append("       -s 排序 按热度 U 评分S 标签数T 上映时间R 默认 U\r\n")
        .append("       -w 爬虫执行间隔(防封杀) 默认 7 单位/秒\r\n")
        .append("       -h 帮助\r\n")
        .append("       样例：java -jar xxx.jar -t 1 -b 100 -e 200 -s U -w 10\r\n");
        System.out.println(sb.toString());
    }
}
