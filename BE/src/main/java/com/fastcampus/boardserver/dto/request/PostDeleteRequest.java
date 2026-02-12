package com.fastcampus.boardserver.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PostDeleteRequest {
    private int id;
    private int accountId;
}
