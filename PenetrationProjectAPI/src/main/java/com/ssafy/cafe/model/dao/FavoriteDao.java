package com.ssafy.cafe.model.dao;

import java.util.List;

import com.ssafy.cafe.model.dto.Favorite;


public interface FavoriteDao {
	
	int insert(Favorite favorite);

	int delete(Integer favoriteId);

	List<Favorite> selectByUser(String userId);
	
	Favorite select(Favorite favorite);
}
