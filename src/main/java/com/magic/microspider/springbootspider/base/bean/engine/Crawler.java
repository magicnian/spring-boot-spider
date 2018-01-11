package com.magic.microspider.springbootspider.base.bean.engine;

/**
 * Interface for identifying different tasks.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @see us.codecraft.webmagic.scheduler.Scheduler
 * @see us.codecraft.webmagic.pipeline.Pipeline
 * @since 0.1.0
 */
public interface Crawler {

    /**
     * unique id for a task.
     *
     * @return uuid
     */
    public String getUUID();

    /**
     * site of a task
     *
     * @return site
     */
    public Site getSite();

}
