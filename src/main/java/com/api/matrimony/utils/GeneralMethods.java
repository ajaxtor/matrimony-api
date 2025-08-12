package com.api.matrimony.utils;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class GeneralMethods {

	public static <T> Page<T> paginateList(List<T> fullList, int page, int size) {
	    int total = fullList.size();
	    int start = Math.min(page * size, total);
	    int end = Math.min(start + size, total);

	    List<T> sublist = fullList.subList(start, end);
	    Pageable pageable = PageRequest.of(page, size);

	    return new PageImpl<>(sublist, pageable, total);
	}
	
}
