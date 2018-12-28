package com.blueice.crawler.douban.vo;

import com.blueice.crawler.util.GenUtil;

/**
* @Description: TODO
* @author blueice
* @date 2018年12月26日 下午7:49:50
*
*/
public class WikiBean
{
    
    private transient static final String PATTER="%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d";
    private String id;
    private String category;
    private String title;
    private String directors;
    private String casts;
    private String countries;
    private String genres;
    private String year;
    private String summary;
    private String duration;
    private String episodesCount;
    private String aka;
    /**
     * @return the id
     */
    public String getId()
    {
        if(id==null) {
            return formatField(GenUtil.create());
        }
        return formatField(id);
    }
    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }
    /**
     * @return the category
     */
    public String getCategory()
    {
        if(category==null) {
            return formatField("movie");
        }
        return formatField(category);
    }
    /**
     * @param category the category to set
     */
    public void setCategory(String category)
    {
        this.category = category;
    }
    /**
     * @return the title
     */
    public String getTitle()
    {
        
        if(title==null) {
            return "\\N";
        }
        return formatField(title);
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        if(title.length()>128) {
            title = title.substring(0, 125)+"...";
        }
        if(title.indexOf("\"")>0) {
            title = title.replaceAll("\"", "\\\\\"");
        }
        this.title = title;
    }
    /**
     * @return the directors
     */
    public String getDirectors()
    {
        return directors==null?"\\N":formatField(directors);
    }
    /**
     * @param directors the directors to set
     */
    public void setDirectors(String directors)
    {
        if(directors.length()>128) {
            directors = directors.substring(0, 125)+"...";
        }
        if(directors.indexOf("\"")>0) {
            directors = directors.replaceAll("\"", "\\\\\"");
        }
        this.directors = directors;
    }
    /**
     * @return the casts
     */
    public String getCasts()
    {
        return casts==null?"\\N":formatField(casts);
    }
    /**
     * @param casts the casts to set
     */
    public void setCasts(String casts)
    {
        if(casts.length()>256) {
            casts = casts.substring(0, 253)+"...";
        }
        if(casts.indexOf("\"")>0) {
            casts = casts.replaceAll("\"", "\\\\\"");
        }
        this.casts = casts;
    }
    /**
     * @return the countries
     */
    public String getCountries()
    {
        return countries==null?"\\N":formatField(countries);
    }
    /**
     * @param countries the countries to set
     */
    public void setCountries(String countries)
    {
        if(countries.length()>128) {
            countries = countries.substring(0, 125)+"...";
        }
        if(countries.indexOf("\"")>0) {
            countries = countries.replaceAll("\"", "\\\\\"");
        }
        this.countries = countries;
    }
    /**
     * @return the genres
     */
    public String getGenres()
    {
        return genres==null?"\\N":formatField(genres);
    }
    /**
     * @param genres the genres to set
     */
    public void setGenres(String genres)
    {
        if(genres.length()>256) {
            genres = genres.substring(0, 253)+"...";
        }
        if(genres.indexOf("\"")>0) {
            genres = genres.replaceAll("\"", "\\\\\"");
        }
        this.genres = genres;
    }
    /**
     * @return the year
     */
    public String getYear()
    {
        return year==null?"\\N":formatField(year);
    }
    /**
     * @param year the year to set
     */
    public void setYear(String year)
    {
        this.year = year;
    }
    /**
     * @return the summary
     */
    public String getSummary()
    {
        return summary==null?"\\N":formatField(summary);
    }
    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary)
    {
        //去豆瓣水印
        if(summary.indexOf("©豆瓣")>0) {
            summary = summary.substring(0, summary.indexOf("©豆瓣"));
        }
        if(summary.length()>512) {
            summary = summary.substring(0, 509)+"...";
        }
        if(summary.indexOf("\"")>0) {
            summary = summary.replaceAll("\"", "\\\\\"");
        }
        if(summary.indexOf("\n")>0) {
            summary = summary.replaceAll("\n", " ");
        }
        this.summary = summary;
    }
    /**
     * @return the duration
     */
    public String getDuration()
    {
        return duration==null?"\\N":formatField(duration);
    }
    /**
     * @param duration the duration to set
     */
    public void setDuration(String duration)
    {
        this.duration = duration;
    }
    /**
     * @return the episodes_count
     */
    public String getEpisodesCount()
    {
        return episodesCount==null?"\\N":formatField(episodesCount);
    }
    /**
     * @param episodes_count the episodes_count to set
     */
    public void setEpisodesCount(String episodesCount)
    {
        this.episodesCount = episodesCount;
    }
    /**
     * @return the aka
     */
    public String getAka()
    {
        return aka==null?"\\N":formatField(aka);
    }
    /**
     * @param aka the aka to set
     */
    public void setAka(String aka)
    {
        if(aka.length()>128) {
            aka = aka.substring(0, 125)+"...";
        }
        if(aka.indexOf("\"")>0) {
            aka = aka.replaceAll("\"", "\\\\\"");
        }
        this.aka = aka;
    }
    @Override
    public String toString()
    {
        //(code,name,directors,actors,category,genres,countries,duration,episode_count
        //        ,language,alias,release_date,story_author,summary,tags,thumbnails,version,website,extend,status)
        return String.format(PATTER,getId(),getTitle(),getDirectors(),getCasts(),getCategory(),
                getGenres(),getCountries(),getDuration(),getEpisodesCount(),"\\N",getAka(),getYear(),"\\N",
                getSummary(),getGenres(),"\\N","\\N","\\N","\\N",1);
    }
    
    public static String formatField(String fieldValue) {
        return "\""+fieldValue+"\"";
    }
    
    /*public static void main(String[] args)
    {
        WikiBean bean = new WikiBean();
        bean.setTitle("功夫\"之王");
        bean.setDirectors("周星驰");
        bean.setCasts("李连杰/成龙/刘亦菲/李冰冰/迈克尔·安格拉诺");
        bean.setAka("双J计划");
        bean.setCategory("movie");
        bean.setCountries("中国大陆");
        bean.setDuration("104分钟");
        bean.setEpisodesCount("1");
        bean.setGenres("动作/奇幻/冒险/武侠/古装");
        bean.setSummary("杰森（Michael Angarano 饰）是一个疯狂迷恋港台功夫片的美国少年，然而现实中的他却饱受坏孩子的欺负，不敢反抗。偶然机会，节森从某中国古董店得到一根\"如意金箍棒\"，借着金箍棒的力量，他竟然回到了几千年前的中国。\n" + 
                "此时，邪恶的玉疆战神（邹兆龙 饰）凭借降伏齐天大圣孙悟空（李连杰 饰）的威名篡夺天界，靠武力鱼肉百姓，民不聊生。杰森先后邂逅了嗜酒的游侠鲁彦（成龙 饰）、沉默寡言的默僧（李连杰 饰）以及轻盈美丽的金燕子（刘亦菲 饰）。他们为解救被困的齐天大圣朝着天界进发。。。©豆瓣");
        bean.setYear("2006");
        System.out.println(bean);
    }*/
}
