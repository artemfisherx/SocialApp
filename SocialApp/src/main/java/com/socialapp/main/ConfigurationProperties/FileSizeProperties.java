package com.socialapp.main.ConfigurationProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

@ConfigurationProperties("userfile")
public class FileSizeProperties {
	
	//значения по умолчанию (в байтах)
	private Integer minsize=0;
	private Integer maxsize=Integer.MAX_VALUE;
	
	public Integer getMinsize() {
		return minsize;
	}
	public void setMinsize(Integer minsize) {
		this.minsize = minsize;
	}
	public Integer getMaxsize() {
		return maxsize;
	}
	public void setMaxsize(Integer maxsize) {
		this.maxsize = maxsize;
	}
		
}
