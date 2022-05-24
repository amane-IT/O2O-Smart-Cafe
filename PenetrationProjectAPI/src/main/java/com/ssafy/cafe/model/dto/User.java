package com.ssafy.cafe.model.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User {

	private String id;
	private String name;
	private String pass;
	private Integer stamps;
	private List<Stamp> stampList = new ArrayList<>();
	private String birthday;


	@Builder
	public User(String id, String name, String pass, Integer stamps, String birthday) {
		super();
		this.id = id;
		this.name = name;
		this.pass = pass;
		this.stamps = stamps;
		this.birthday = birthday;
	}

	public User(String id, String name, String pass, String birthday) {
		super();
		this.id = id;
		this.name = name;
		this.pass = pass;
		this.birthday = birthday;
	}
	
	public User(String id, String name, String pass) {
		super();
		this.id = id;
		this.name = name;
		this.pass = pass;
	}
	
	public User(String id, String name, Integer stamps) {
		super();
		this.id = id;
		this.name = name;
		this.stamps = stamps;
	}
}
