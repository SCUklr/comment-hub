package com.ssp.comment.bo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MyCommentListBO {

    private long total;
    private int page;
    private int pageSize;
    private List<MyCommentItemBO> list;

    @Data
    public static class MyCommentItemBO {
        private Long commentObjectId;
        private Integer commentType;
        private Integer interactionType;
        private String latestContent;
        private LocalDateTime latestTime;
        private Integer interactionCount;
    }
}
