package com.ssp.comment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShardRoute {

    private Integer dbIndex;
    private Integer tableIndex;
    private String physicalTable;
}
