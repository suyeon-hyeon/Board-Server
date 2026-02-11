package com.fastcampus.boardserver.service;

import com.fastcampus.boardserver.dto.PostDTO;

public interface PostService {
    void register(String id, PostDTO postDTO);
}
