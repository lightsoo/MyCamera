package com.mycamera.lightsoo.mycamera.Data;

import java.io.File;
import java.io.Serializable;

/**
 * Created by LG on 2016-02-27.
 */
public class Files implements Serializable{
    String title, director;
    int year;
    File file1, file2;

    //setter
    public void setTitle(String title){
        this.title = title;
    }
    public void setDirector(String director){
        this.director = director;
    }
    public void setYear(int year){
        this.year = year;
    }
    public void setFile1(File file1){
        this.file1 = file1;
    }

    //getter
    public String getTitle(){
        return title;
    }
    public String getDirector(){
        return director;
    }
    public int getYear(){
        return year;
    }
    public File getFile1(){
        return file1;
    }
}
