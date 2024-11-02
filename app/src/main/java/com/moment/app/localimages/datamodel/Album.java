package com.moment.app.localimages.datamodel;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.List;
@Keep
public class Album {
    public String id;
    public String coverPath;
    public String name;
    public long dateAdded;
    public List<AlbumItemFile> files = new ArrayList<>();


    public List<String> getPhotoPaths() {
        List paths = new ArrayList<String>(files.size());

        for (AlbumItemFile file : files) {
            if (file.isVideo()){
                paths.add(file.thumbnail);
            }else{
                paths.add(file.path);
            }

        }
        return paths;
    }


    public List<AlbumItemFile> getFiles() {
        return files;
    }

    public int getFilesLength(){
        if (files == null || files.size() == 0) return 0;
        else return files.size();
    }

    public void addPhoto(long id, String path,String lowPath, String mimeType, long size, int width, int height,long date) {
        AlbumItemFile file = new AlbumItemFile();
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

        AlbumItemFile file = new AlbumItemFile();
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
