package com.ssp.comment.vo.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MyCommentListRespVO {

    private long total;
    private int page;
    private int pageSize;
    private List<MyCommentItemVO> list;

    @Data
    public static class MyCommentItemVO {
        private Long commentObjectId;
        private Integer commentType;
        private Integer interactionType;
        private String latestContent;
        private LocalDateTime latestTime;
        private Integer interactionCount;
    }
}
