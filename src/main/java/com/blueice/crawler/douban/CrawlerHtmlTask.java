package com.blueice.crawler.douban;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blueice.crawler.douban.constants.Tags;
import com.blueice.crawler.douban.vo.WikiBean;
import com.blueice.crawler.util.HttpUtil;

/**
* @Description: 通过豆瓣网页爬取数据
* @author blueice
* @date 2018年12月26日 下午7:44:39
*
*/
public class CrawlerHtmlTask implements Runnable
{
    private final static String SOURCEURL="https://movie.douban.com/j/new_search_subjects";
    private final static String ARRAY_SPLIT=";";
    private final Logger logger;
    private Tags tags;
    private Integer start;
    private Integer end;
    private String sort;
    private static final int reTryCount=5;//失败请求次数
    private Map<String,String> param = new HashMap<String, String>();
    private Integer sleepSecond;
    private boolean isFinish;
    private final static String HTML_URL = "http://movie.douban.com/subject/";
   
    public CrawlerHtmlTask(final Logger logger,Tags tags,Integer start,Integer end,String sort, Integer sleepSecond)
    {
        this.logger = logger;
        this.start = (start==null?0:start);
        this.end = (end==null?Integer.MAX_VALUE:end);
        this.tags = tags;
        this.sort = (sort==null?"U":sort);
        this.sleepSecond = (sleepSecond==null?7:sleepSecond);
        this.isFinish = false;
        param.put("start", this.start.intValue()+"");
        param.put("tags", this.tags.getTag());
        param.put("sort", this.sort);
    }
    
    public CrawlerHtmlTask(final Logger logger,Tags tags,Integer start, Integer end)
    {
        this(logger,tags,start,end,null,null);
    }
    
    public CrawlerHtmlTask(final Logger logger,Tags tags)
    {
        this(logger,tags,null,null,null,null);
    }

    @Override
    public void run()
    {
        JSONArray data = null;
        JSONObject subject;
        int tryCount=0;
        int wikiIndex=this.start;
        logger.debug("crawler start:tags:{};sort:{};start_index:{}",tags.getTag(),this.sort,wikiIndex);
        while(!isFinish) {
            tryCount = reTryCount;
            data = null;
            try
            {
                while(data==null&&tryCount-->0) {
                    try
                    {
                        data = JSON.parseObject(HttpUtil.get(HttpUtil.appendQueryParams(SOURCEURL,param))).getJSONArray("data");
                    }
                    catch (Exception e)
                    {
                        logger.error("web请求list失败,"+sleepSecond+"分钟后再尝试，剩余尝试次数:"+tryCount, e);
                        try
                        {
                            Thread.sleep(sleepSecond*60*1000);
                        }
                        catch (InterruptedException e1)
                        {
                        }
                    }
                }
                if(data==null) {//多次重试后无数据，即接口被限 退出
                    throw new IOException("接口请求受限:"+HttpUtil.appendQueryParams(SOURCEURL,param));
                }else if(data.size()<=0) {//已无数据可爬了，退出
                    isFinish = true;
                }else {
                    for(int i=0,len=data.size();i<len;i++) {
                        if(wikiIndex>=this.end) {//超出参数限制
                            isFinish = true;
                            break;
                        }
                        subject = data.getJSONObject(i);
                        if(subject.getString("id")==null) {
                            wikiIndex++;
                            continue;
                        }
                        WikiBean wikiBean = new WikiBean();
                        wikiBean.setCategory(this.tags.getType());
                        wikiBean.setTitle(subject.getString("title"));
                        wikiBean.setCasts(formateStrBy(subject.getJSONArray("casts"),ARRAY_SPLIT));
                        wikiBean.setDirectors(formateStrBy(subject.getJSONArray("directors"),ARRAY_SPLIT));
                        
                        if(parseHtml(subject.getString("id"),wikiBean)) {
                            //借助logger输出infile文件
                            logger.info(wikiBean.toString());
                        }
                        wikiIndex++;
                        Thread.sleep(sleepSecond*1000);
                    }
                }
            }
            catch (IOException e)
            {
                isFinish = true;
                logger.debug("error!!! tags:{};sort:{};location index:{}",tags.getTag(),this.sort,wikiIndex);
                logger.error("", e);
            }catch (InterruptedException e)
            {
                logger.error("", e);
            }finally {
                if(!isFinish) {
                    param.put("start",wikiIndex +"");
                }
            }
            
        }
        logger.debug("crawler end:tags:{};sort:{};success start index(incluse):{};end index(excluse):{}",tags.getTag(),this.sort,this.start,wikiIndex);
    }

    /**
    * <p>爬取网页</p>
    * @param string
    * @param wikiBean
    * @author blueice
     * @throws IOException 
    * @date 2018年12月29日 上午10:38:03
    */
    private boolean parseHtml(String id, WikiBean wikiBean) throws IOException
    {
        int tryCount = reTryCount;
        String html = null;
        while(html==null&&tryCount-->0) {
            try
            {
                html = HttpUtil.get(HTML_URL+id).replaceAll("\\s", "");
            }
            catch (FileNotFoundException e) {
                //该id已没有对应网页内容了
                logger.error("html请求404,subject={}对应html页面丢失",id);
                return false;
            }
            catch (Exception e)
            {
                logger.error("html请求失败,"+sleepSecond+"分钟后再尝试，剩余尝试次数:"+tryCount, e);
                try
                {
                    Thread.sleep(sleepSecond*60*1000);
                }
                catch (InterruptedException e1)
                {
                }
            }
        }
        if(html==null) {
            throw new IOException("html请求受封:"+HTML_URL);
        }
        
        wikiBean.setAka(pareseAka(html));
        wikiBean.setSummary(pareseSummary(html));
        wikiBean.setAuthors(pareseAuthors(html));
        wikiBean.setLanguages(pareseLanguages(html));
        wikiBean.setYear(pareseReleaseDate(html));
        wikiBean.setCountries(pareseCountries(html));
        wikiBean.setDuration(pareseDuration(html));
        wikiBean.setEpisodesCount(pareseEpisodesCount(html));
        
        Set<String> tagsSet = new HashSet<String>();
        wikiBean.setGenres(pareseGenres(html,tagsSet));
        wikiBean.setTags(pareseTags(html, tagsSet));
        return true;
    }



    /**
    * <p>方法描述</p>
    * @param jsonArray
    * @param split
    * @return
    * @author blueice
    * @date 2018年12月27日 下午5:16:27
    */
    private String formateStrBy(JSONArray jsonArray, String split)
    {
        if(jsonArray.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(Object temp:jsonArray) {
            sb.append(temp.toString()).append(split);
        }
        
        return sb.length()>0?sb.substring(0, sb.lastIndexOf(split)):null;
    }
   
    private static String pareseEpisodesCount(String html) {
        int start = html.indexOf("集数:</span>");
        if(start<0) {
            return null;
        }
        return html.substring(start+10,html.indexOf("<br/>",start+10));
    }
    
    private static String pareseCountries(String html) {
        int start = html.indexOf("制片国家/地区:</span>");
        if(start<0) {
            return null;
        }
        return html.substring(start+15,
                html.indexOf("<br/>",start+15)).replaceAll("/", ";");
    }
    
    private static String pareseDuration(String html) {
        //解析<script type="application/ld+json">内容
        int start = html.indexOf("\"duration\":\"PT");
        if(start<0) {
            return null;
        }
        String[] strArr = html.substring(start+14,
                html.indexOf("M",start+14)).split("H");
        if(strArr.length==2) {
            return (Integer.parseInt(strArr[0])*60+Integer.parseInt(strArr[1]))+"分钟";
        }else {
            return null;
        }
    }
    
    private static String pareseTags(String html,Set<String> tags) {
        int start = html.indexOf("class=\"tags-body\">");
        if(start>0) {
            String[] tempArr = html.substring(start+18,html.indexOf("</div>",start)).split("</a>");
            if(tempArr!=null) {
                for(String temp:tempArr) {
                    tags.add(temp.substring(temp.lastIndexOf(">")+1));
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        if(tags!=null&&tags.size()>0) {
            for(String tag:tags) {
                sb.append(tag).append(ARRAY_SPLIT);
            }
        }
        return sb.length()>0?sb.substring(0, sb.lastIndexOf(ARRAY_SPLIT)):null;
    }
    
    private static String pareseGenres(String html,Set<String> tags) {
        //解析<script type="application/ld+json">内容
        int start = html.indexOf("\"genre\":[");
        if(start<0) {
            return null;
        }
        String genresJson = html.substring(start+8,
                html.indexOf("]",start+8)+1);
        JSONArray genresArr = JSON.parseArray(genresJson);
        if(genresArr.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0,len=genresArr.size();i<len;i++) {
            if(tags!=null) {
                tags.add(genresArr.getString(i));
            }
            sb.append(genresArr.getString(i)).append(ARRAY_SPLIT);
        }
        return sb.length()>0?sb.substring(0, sb.lastIndexOf(ARRAY_SPLIT)):null;
    }
    
    private static String pareseLanguages(String html) {
        int start = html.indexOf("语言:</span>");
        if(start<0) {
            return null;
        }
        return html.substring(start+10,
                html.indexOf("<br/>",start+10)).replaceAll("/", ";");
    }
    
    private static String pareseAka(String html) {
        
        int start = html.indexOf("又名:</span>");
        if(start<0) {
            return null;
        }
        return html.substring(start+10,
                html.indexOf("<br/>",start+10)).replaceAll("/", ";");
    }
    
    private static String pareseSummary(String html) {
        int start = html.indexOf("\"description\":\"");
        if(start<0) {
            return null;
        }
        return html.substring(start+15,
                html.indexOf("\",",start+15));
    }
    
    private static String pareseReleaseDate(String html) {
        //解析<script type="application/ld+json">内容
        int start = html.indexOf("\"datePublished\":\"");
        if(start<0) {
            return null;
        }
        return html.substring(start+17,
                html.indexOf("\",",start+17));
    }
    private static String pareseAuthors(String html) {
        //解析<script type="application/ld+json">内容
        int start = html.indexOf("\"author\":");
        if(start<0) {
            return null;
        }
        String authorJson = html.substring(start+9,
                html.indexOf("]",start+9)+1).replaceAll("@", "");
        JSONArray authorArr = JSON.parseArray(authorJson);
        if(authorArr.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0,len=authorArr.size();i<len;i++) {
            sb.append(authorArr.getJSONObject(i).getString("name")).append(ARRAY_SPLIT);
        }
        
        return sb.length()>0?sb.substring(0, sb.lastIndexOf(ARRAY_SPLIT)):null;
    }
    
    public static void main(String[] args) throws Exception
    {
            try
            {
                String html = HttpUtil.get("https://movie.douban.com/subject/7916275/");
                System.out.println(html);
            }
            catch (FileNotFoundException e)
            {
                System.out.println("404");
            }
            /*WikiBean wikiBean = new WikiBean();
            wikiBean.setAka(pareseAka(html));
            wikiBean.setSummary(pareseSummary(html));
            wikiBean.setAuthors(pareseAuthors(html));
            wikiBean.setLanguages(pareseLanguages(html));
            wikiBean.setYear(pareseReleaseDate(html));
            wikiBean.setCountries(pareseCountries(html));
            wikiBean.setDuration(pareseDuration(html));
            wikiBean.setEpisodesCount(pareseEpisodesCount(html));
            
            Set<String> tags = new HashSet<String>();
            wikiBean.setGenres(pareseGenres(html,tags));
            wikiBean.setTags(pareseTags(html, tags));
            System.out.println(wikiBean.toString());*/
    }
}
