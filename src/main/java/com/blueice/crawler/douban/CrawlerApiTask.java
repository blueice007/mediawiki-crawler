package com.blueice.crawler.douban;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blueice.crawler.douban.constants.Tags;
import com.blueice.crawler.douban.vo.WikiBean;
import com.blueice.crawler.util.HttpUtil;

/**
* @Description: 通过豆瓣 开放api和web请求爬取数据
* @author blueice
* @date 2018年12月26日 下午7:44:39
*
*/
public class CrawlerApiTask implements Runnable
{
    private final static String sourceUrl="https://movie.douban.com/j/new_search_subjects";
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
    private final static String WEB_URL = "https://movie.douban.com/j/subject_abstract?subject_id=";
    private final static String OPENAPI_URL = "https://api.douban.com/v2/movie/subject/";
   
    public CrawlerApiTask(final Logger logger,Tags tags,Integer start,Integer end,String sort, Integer sleepSecond)
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
    
    public CrawlerApiTask(final Logger logger,Tags tags,Integer start, Integer end)
    {
        this(logger,tags,start,end,null,null);
    }
    
    public CrawlerApiTask(final Logger logger,Tags tags)
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
                        data = JSON.parseObject(HttpUtil.get(HttpUtil.appendQueryParams(sourceUrl,param))).getJSONArray("data");
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
                    throw new IOException("web-list接口请求受限:"+HttpUtil.appendQueryParams(sourceUrl,param));
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
                        
                        parseOpenApiUrl(subject.getString("id"),wikiBean);
                        parseWebURL(subject.getString("id"),wikiBean);
                        
                        //借助logger输出infile文件
                        logger.info(wikiBean.toString());
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
                    //this.start+=20;
                    //param.put("start", this.start.intValue()+"");
                    param.put("start",wikiIndex +"");
                }
            }
            
        }
        logger.debug("crawler end:tags:{};sort:{};success start index(incluse):{};end index(excluse):{}",tags.getTag(),this.sort,this.start,wikiIndex);
    }

    /**
    * <p>填充episodes_count title genres countries等信息</p>
    * @param id
    * @param wikiBean
    * @author blueice
     * @throws InterruptedException 
     * @throws IOException 
    * @date 2018年12月27日 下午5:00:34
    */
    private void parseOpenApiUrl(String id, WikiBean wikiBean) throws IOException
    {
        
        JSONObject detail = null;
        int tryCount = reTryCount;
        while(detail==null&&tryCount-->0) {
            try
            {
                detail = JSON.parseObject(HttpUtil.get(OPENAPI_URL+id));
            }
            catch (IOException e)
            {
                logger.error("open-api请求失败,"+sleepSecond+"分钟后再尝试，剩余尝试次数:"+tryCount, e);
                try
                {
                    Thread.sleep(sleepSecond*60*1000);
                }
                catch (InterruptedException e1)
                {
                }
                sleepSecond++;
            }
        }
        if(detail==null) {
            throw new IOException("openApi接口请求受封:"+OPENAPI_URL);
        }
        
        if(detail.containsKey("episodes_count"))
            wikiBean.setEpisodesCount(detail.getString("episodes_count"));
        /*if(detail.containsKey("title"))
            wikiBean.setTitle(detail.getString("title"));*/
        
        if(detail.containsKey("genres")) {
            wikiBean.setGenres(formateStrBy(detail.getJSONArray("genres"),ARRAY_SPLIT));
        }
        if(detail.containsKey("countries")) {
            wikiBean.setCountries(formateStrBy(detail.getJSONArray("countries"),ARRAY_SPLIT));
        }
        if(detail.containsKey("year")) {
            wikiBean.setYear(detail.getString("year"));
        }
        if(detail.containsKey("aka")) {
            wikiBean.setAka(formateStrBy(detail.getJSONArray("aka"),ARRAY_SPLIT));
        }
        if(detail.containsKey("summary")) {
            String summary = detail.getString("summary");
            if(summary.indexOf("\n")>0) {
                summary = summary.replaceAll("\n", " ");
            }
            wikiBean.setSummary(summary);
        }
    }

    /**
    * <p>填充directors actors duration信息</p>
    * @param id
    * @param wikiBean
    * @author blueice
     * @throws InterruptedException 
     * @throws IOException 
    * @date 2018年12月27日 下午5:00:32
    */
    private void parseWebURL(String id, WikiBean wikiBean) throws InterruptedException, IOException
    {
        JSONObject detail = null;
        int tryCount = reTryCount;
        while(detail==null&&tryCount-->0) {
            try
            {
                detail = JSON.parseObject(HttpUtil.get(WEB_URL+id)).getJSONObject("subject");
            }
            catch (Exception e)
            {
                logger.error("web请求失败,"+sleepSecond+"分钟后再尝试，剩余尝试次数:"+tryCount, e);
                try
                {
                    Thread.sleep(sleepSecond*60*1000);
                }
                catch (InterruptedException e1)
                {
                }
            }
        }
        if(detail==null) {
            throw new IOException("web接口请求受封:"+WEB_URL);
        }
        
        /*if(detail.containsKey("directors")) {
            wikiBean.setDirectors(formateStrBy(detail.getJSONArray("directors"),ARRAY_SPLIT));
        }
        if(detail.containsKey("actors")) {
            wikiBean.setCasts(formateStrBy(detail.getJSONArray("actors"),ARRAY_SPLIT));
        }*/
        if(detail.containsKey("duration")) {
            wikiBean.setDuration(detail.getString("duration"));
        }
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
    public static void main(String[] args) throws IOException
    {
        System.out.println(HttpUtil.get("https://api.douban.com/v2/movie/subject/3074503"));
    }
}
