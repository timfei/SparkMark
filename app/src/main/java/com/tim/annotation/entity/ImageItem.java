package com.tim.annotation.entity;

import java.io.Serializable;

/**
 * Created by TimFei on 16/9/7.
 */
public class ImageItem implements Serializable {

    public String name;       //图片的名字

    public String path;       //图片的路径

    public long size;         //图片的大小

    public int width;         //图片的宽度

    public int height;        //图片的高度

    public String mimeType;   //图片的类型

    public long addTime;      //图片的创建时间

    @Override
    public boolean equals(Object o) {
        try {
            ImageItem other = (ImageItem) o;
            return this.path.equalsIgnoreCase(other.path) && this.addTime == other.addTime;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }

}
