package com.example.navimap;

public class Journal_list_item {
    private static int id = 0;
    private int item_index;
    private String imageName;
    private String title;

    public Journal_list_item(){
        this.item_index = getId()+1;
        this.imageName = null;
        this.title = null;
        id += 1;
    }
    public Journal_list_item(int item_index, String imageName, String title){
        this.item_index = getId()+1;
        this.imageName = null;
        this.title = null;
        id += 1;
    }
    public Journal_list_item(String imageName, String title){
        this.item_index = getId()+1;
        this.imageName = imageName;
        this.title = title;
        id += 1;
    }
    private int getId(){
        return this.id;
    }
    public int getItem_index(){
        return this.item_index;
    }
    public String getImageName(){
        return this.imageName;
    }
    public String getTitle(){
        return this.title;
    }

    public void setId(){
        id -= 1;
    }
    public void setItem_index(){
        this.item_index -= 1;
    }
    public void setImageName(String imageName){
        this.imageName = imageName;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setAll(String imageName, String title){
        this.imageName = imageName;
        this.title = title;
    }


}
