package com.ssp.comment.vo.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentAuditHistoryRespVO {

    private Long targetId;
    private Integer targetType;
    private List<AuditItemVO> list;

    @Data
    public static class AuditItemVO {
        private Long id;
        private Integer auditStatus;
        private String auditContent;
        private String auditReason;
        private Long auditOperator;
        private LocalDateTime auditTime;
    }
}
