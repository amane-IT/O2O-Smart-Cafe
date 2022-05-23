package com.ssafy.cafe.model.service;

import java.util.List;

import com.ssafy.cafe.model.dto.Comment;
import com.ssafy.cafe.model.dto.Favorite;

public interface FavoriteService {
	/**
	 * Favorite를 등록한다.
	 * @param Favorite
	 */
	void addFavorite(Favorite favorite);
	
	/**
	 * id에 해당하는 Favorite를 삭제한다.
	 * @param id
	 */
	void removeFavorite(Integer id);
	
	/**
	 * userId에 해당하는 Favorite의 목록을 반환한다.
	 * @param productId
	 * @return
	 */
	List<Favorite> selectByUser(String userId);
	
	Favorite select(Favorite favorite);
}
