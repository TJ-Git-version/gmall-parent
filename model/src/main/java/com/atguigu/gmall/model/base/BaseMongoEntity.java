package com.atguigu.gmall.model.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class BaseMongoEntity implements Serializable {

    @ApiModelProperty(value = "id")
    @Id // mongodb主键
    private String id;

    @ApiModelProperty(value = "创建时间")
    @CreatedDate // mongodb自动添加创建时间
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @LastModifiedDate // mongodb自动添加更新时间
    private Date updateTime;

}
