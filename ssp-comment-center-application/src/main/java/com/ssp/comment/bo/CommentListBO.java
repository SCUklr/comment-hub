package com.ssp.comment.bo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentListBO {

    private long total;
    private int page;
    private int pageSize;
    private List<CommentItemBO> list;

    @Data
    public static class CommentItemBO {
        private Long id;
        private Long commentObjectId;
        private Integer commentType;
        private String content;
        private String images;
        private Integer userId;
        private Integer sort;
        private Integer replyCount;
        private Integer likeCount;
        private Boolean liked;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private List<ReplyItemBO> topReplies;
    }

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
        private Boolean liked;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }
}
