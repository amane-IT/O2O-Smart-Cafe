package com.ssafy.cafe.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.cafe.model.dao.FavoriteDao;
import com.ssafy.cafe.model.dto.Favorite;

@Service
public class FavoriteServiceImpl implements FavoriteService{

	@Autowired
	FavoriteDao fDao;
	
	
	@Override
	@Transactional
	public void addFavorite(Favorite favorite) {
		fDao.insert(favorite);
		
	}

	@Override
	@Transactional
	public void removeFavorite(Integer id) {
		fDao.delete(id);
		
	}

	@Override
	public List<Favorite> selectByUser(String userId) {
		return fDao.selectByUser(userId);
	}
	
	@Override
	public Favorite select(Favorite favorite) {
		return fDao.select(favorite);
	}

}
