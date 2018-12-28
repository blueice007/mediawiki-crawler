package com.blueice.crawler.douban.constants;

/**
* @Description: TODO
* @author blueice
* @date 2018年12月27日 下午4:29:47
*
*/
public enum Tags
{
    MOVIE("电影","movie"),
    TV("电视剧","tv"),
    VARIETY("综艺","tv"),
    ANIME("动漫","tv"),
    DOCUMENTARY("纪录片","tv");
    
    private String tag;
    private String type;
    private Tags(String tag,String type) {
        this.tag = tag;
        this.type = type;
    }
    /**
     * @return the tag
     */
    public String getTag()
    {
        return tag;
    }
    /**
     * @param tag the tag to set
     */
    public void setTag(String tag)
    {
        this.tag = tag;
    }
    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }
}
