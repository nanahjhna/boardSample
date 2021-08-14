﻿package com.sp.bbs;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.sp.common.MyUtil;

@Controller("bbs.boardController")
@RequestMapping("/bbs/*")
public class BoardController {
	@Autowired
	private BoardService service;
	
	@Autowired
	private MyUtil myUtil;

	@RequestMapping(value="created", method=RequestMethod.GET)
	public String createdForm(Model model) throws Exception {
		model.addAttribute("mode", "created");
		return "bbs/created";
    }

	@RequestMapping(value="created", method=RequestMethod.POST)
	public String createdSubmit(Board dto, HttpServletRequest req) throws Exception {
		try {
			dto.setIpAddr(req.getRemoteAddr());
			service.insertBoard(dto);
		} catch (Exception e) {
		}
    	
		return "redirect:/bbs/list";
    }
	
	@RequestMapping(value="list")
	public String list(
				@RequestParam(value="page", defaultValue="1") int current_page,
				@RequestParam(defaultValue="all") String condition,
				@RequestParam(defaultValue="") String keyword,
				HttpServletRequest req,
				Model model
			) throws Exception {

   	    String cp = req.getContextPath();
   	    
		int rows = 10;  // 한 화면에 보여주는 게시물 수
		int total_page = 0;
		int dataCount = 0;
   	    
		if(req.getMethod().equalsIgnoreCase("GET")) { // GET 방식 일 때
			keyword = URLDecoder.decode(keyword, "utf-8");
		}

        // 전체 페이지 수
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("condition", condition);
        map.put("keyword", keyword);

        dataCount = service.dataCount(map);
        if(dataCount != 0)
            total_page = myUtil.pageCount(rows,  dataCount) ;

        if(total_page < current_page) 
            current_page = total_page;

        // 리스트에 출력할 데이터를 가져오기
        int offset=(current_page-1)*rows;
		if(offset<0) offset=0;  
		map.put("offset", offset);
		map.put("rows", rows);

		List<Board> list = service.listBoard(map);

		if(list != null) {
	        // 리스트의 번호
	        int listNum, n = 0;
	        for(Board dto : list) {
	            listNum = dataCount - (offset + n);
	            dto.setListNum(listNum);
	            n++;
	        }
		}


        String query = "";
        String listUrl;
        String articleUrl;
        if(keyword.length()!=0) {
        	query = "condition=" +condition + 
        	             "&keyword=" + URLEncoder.encode(keyword, "utf-8");	
        }
        
    	listUrl = cp+"/bbs/list";
        articleUrl = cp+"/bbs/article?page=" + current_page;
        if(query.length()!=0) {
        	listUrl = listUrl + "?" + query;
            articleUrl = articleUrl + "&"+ query;
        }
        
        String paging = myUtil.paging(current_page, total_page, listUrl);

        model.addAttribute("list", list);
        model.addAttribute("articleUrl", articleUrl);
        model.addAttribute("page", current_page);
        model.addAttribute("total_page", total_page);
        model.addAttribute("dataCount", dataCount);
        model.addAttribute("paging", paging);
		
		model.addAttribute("condition", condition);
		model.addAttribute("keyword", keyword);

        return "bbs/list";
	}

	@RequestMapping(value="article", method=RequestMethod.GET)
	public String article(
				@RequestParam int num,
				@RequestParam String page,
				@RequestParam(defaultValue="all") String condition,
				@RequestParam(defaultValue="") String keyword,
				Model model
			) throws Exception {
		
		keyword = URLDecoder.decode(keyword, "utf-8");
		
		String query="page="+page;
		if(keyword.length()!=0) {
			query+="&condition="+condition+"&keyword="+URLEncoder.encode(keyword, "UTF-8");
		}

		// 조회수 증가 및 해당 레코드 가져 오기
		service.updateHitCount(num);

		Board dto = service.readBoard(num);
		if(dto==null)
			return "redirect:/bbs/list?"+query;

		// 스타일로 처리하는 경우 : style="white-space:pre;"
        dto.setContent(dto.getContent().replaceAll("\n", "<br>"));

		// 이전 글, 다음 글
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("condition", condition);
		map.put("keyword", keyword);
		map.put("num", num);

		Board preReadDto = service.preReadBoard(map);
        Board nextReadDto = service.nextReadBoard(map);

		model.addAttribute("dto", dto);
		model.addAttribute("preReadDto", preReadDto);
		model.addAttribute("nextReadDto", nextReadDto);

		model.addAttribute("page", page);
		model.addAttribute("query", query);
		
        return "bbs/article";
	}

	@RequestMapping(value="delete")
	public String delete(
				@RequestParam int num,
				@RequestParam String page,
				@RequestParam(defaultValue="all") String condition,
				@RequestParam(defaultValue="") String keyword
			) throws Exception {

		keyword = URLDecoder.decode(keyword, "utf-8");
		String query="page="+page;
		if(keyword.length()!=0) {
			query+="&condition="+condition+"&keyword="+URLEncoder.encode(keyword, "UTF-8");
		}
		
		try {
			service.deleteBoard(num);
		} catch (Exception e) {
		}

		return "redirect:/bbs/list?"+query;
    }
	
	@RequestMapping(value="update", method=RequestMethod.GET)
	public String updateForm(
				@RequestParam int num,
				@RequestParam String page,
				Model model
			) throws Exception {

		Board dto = service.readBoard(num);
		if(dto==null) {
			return "redirect:/bbs/list?page="+page;
		}

		model.addAttribute("mode", "update");
		model.addAttribute("page", page);
		model.addAttribute("dto", dto);

		return "bbs/created";
	}
	
	@RequestMapping(value="update", method=RequestMethod.POST)
	public String updateSubmit(
				Board dto,
				@RequestParam String page		
			) throws Exception {
		
		try {
			service.updateBoard(dto);
		} catch (Exception e) {
		}
    	
		return "redirect:/bbs/list?page="+page;
    }
}
