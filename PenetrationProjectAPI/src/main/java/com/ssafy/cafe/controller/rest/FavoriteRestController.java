package com.ssafy.cafe.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.cafe.model.dto.Favorite;
import com.ssafy.cafe.model.service.FavoriteService;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/rest/favorite")
@CrossOrigin(allowCredentials = "true", originPatterns = { "*" })
public class FavoriteRestController {

	@Autowired
	FavoriteService fService;
	
	@PostMapping
	@Transactional
	@ApiOperation(value = "favorite 객체를 추가한다.", response = Favorite.class)
	public Favorite insert(@RequestBody Favorite favorite) {
		fService.addFavorite(favorite);
		return fService.select(favorite);
	}
	
	@DeleteMapping("/{id}")
	@Transactional
	@ApiOperation(value = "{id}에 해당하는 즐겨찾기를 삭제한다.", response = Boolean.class)
	public Boolean delete(@PathVariable Integer id) {
		fService.removeFavorite(id);
		return true;
	}
	
	@GetMapping("/byUser")
	@ApiOperation(value = "userId에 해당하는 productId의 목록을 반환한다.", 
				response = List.class)
	public List<Favorite> getFavorites(String user_id){
		return fService.selectByUser(user_id);
	}
}
