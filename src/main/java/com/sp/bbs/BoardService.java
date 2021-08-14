package com.sp.bbs;

import java.util.List;
import java.util.Map;

public interface BoardService {
	public void insertBoard(Board dto) throws Exception;
	
	public List<Board> listBoard(Map<String, Object> map);
	public int dataCount(Map<String, Object> map);
	
	public Board readBoard(int num);
	public void updateHitCount(int num) throws Exception;
	public Board preReadBoard(Map<String, Object> map);
	public Board nextReadBoard(Map<String, Object> map);
	
	public void updateBoard(Board dto) throws Exception;
	public void deleteBoard(int num) throws Exception;
}
