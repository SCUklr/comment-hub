package com.ssp.comment.bo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReplyListBO {

    private long total;
    private int page;
    private int pageSize;
    private List<ReplyItemBO> list;

    @Data
    public static class ReplyItemBO {
        private Long id;
        private Long commentId;
        private Long parentId;
        private Integer replyType;
        private String content;
        private String images;
        private Integer replyUserId;
        private Integer beRepliedUserId;
        private Integer likeCount;
        private Integer auditStatus;
        private Boolean liked;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }
}
