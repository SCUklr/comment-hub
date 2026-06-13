package com.ssp.comment.vo.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReplyListRespVO {

    private long total;
    private int page;
    private int pageSize;
    private List<ReplyItemVO> list;

    @Data
    public static class ReplyItemVO {
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
