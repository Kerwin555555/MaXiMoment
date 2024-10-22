package com.moment.app.images.bean;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.List;
@Keep
public class MediaDirectory {
    public String id;
    public String coverPath;
    public String name;
    public long dateAdded;
    public List<MediaFile> files = new ArrayList<>();


    public List<String> getPhotoPaths() {
        List paths = new ArrayList<String>(files.size());

        for (MediaFile file : files) {
            if (file.isVideo()){
                paths.add(file.thumbnail);
            }else{
                paths.add(file.path);
            }

        }
        return paths;
    }


    public List<MediaFile> getFiles() {
        return files;
    }

    public int getFilesLength(){
        if (files == null || files.size() == 0) return 0;
        else return files.size();
    }

    public void addPhoto(long id, String path,String lowPath, String mimeType, long size, int width, int height,long date) {
        MediaFile file = new MediaFile();
        file.fileId = id;
        file.path = path;
        file.lowPath = lowPath;
        file.mimeType = mimeType;
        file.size = size;
        file.width = width;
        file.height = height;
        file.date = date;
        files.add(file);
    }

    public void addVideo(long id, String path,String thumbnail,String mimeType, long size, int width, int height,long date,long duration){

//        if (duration < 1000) return;

        MediaFile file = new MediaFile();
        file.fileId = id;
        file.path = path;
        file.thumbnail = thumbnail;
        file.lowPath = thumbnail;
        file.mimeType = mimeType;
        file.size = size;
        file.width = width;
        file.height = height;
        file.date = date;
        file.duration = duration;
        files.add(file);
    }

}
