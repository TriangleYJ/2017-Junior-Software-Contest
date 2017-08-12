package com.yjprojects.jsctest2.recycler;

import java.util.Date;

/**
 * Created by jyj on 2017-05-21.
 */

public class BaseListClass {
    public String title;
    public Date date;
    public String id;


    public BaseListClass(String title, Date date, String id) {
        this.title = title;
        this.date = date;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
