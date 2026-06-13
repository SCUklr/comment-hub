package com.ssp.comment.vo.resp;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class CommentListRespVO {

    private long total;
    private int page;
    private int pageSize;
    private List<CommentItemVO> list;

    @Data
    public static class CommentItemVO {
        private Long id;
        private Long commentObjectId;
        private Integer commentType;
        private String content;
        private String images;
        private Integer userId;
        private Integer sort;
        private Integer replyCount;
        private Integer likeCount;
        private Integer auditStatus;
        private Boolean liked;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private List<ReplyItemVO> topReplies;
    }

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
