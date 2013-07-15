package com.example.imagesharing;

import android.widget.BaseAdapter;

public class BaseListElement {
	private String url;
	private String name;
	private String status;
	String fId;
	private BaseAdapter adapter;
	public BaseListElement(String name,String url,String status,String fId) {
	   this.fId=fId;
	    this.url = url;
	    this.name = name;
	    this.status=status;
	}
	public String getUrl(){
		return url;
	}
	public String getStatus(){
		return status;
	}
	public String getFId(){
		return fId;
	}
	public String getName(){
		return name;
	}
	public void setAdapter(BaseAdapter adapter) {
	    this.adapter = adapter;
	}
	
}
