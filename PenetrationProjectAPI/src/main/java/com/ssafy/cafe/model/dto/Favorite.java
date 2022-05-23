package com.ssafy.cafe.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Favorite {

	private Integer id;
	private String userId;
	private Integer productId;
	
	@Builder
	public Favorite(Integer id, String userId, Integer productId) {
		super();
		this.id = id;
		this.userId = userId;
		this.productId = productId;
	}

	public Favorite(String userId, Integer productId) {
		this.userId = userId;
		this.productId = productId;
	}
}
