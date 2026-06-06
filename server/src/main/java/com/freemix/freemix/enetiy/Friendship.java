package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 好友关系实体
 * 独立于目标协作关系，代表两个用户之间的好友关系
 */
@Document(value = "friendship")
@Data
public class Friendship {
    String _id;
    String fromUser;      // 发起好友请求的用户名
    String toUser;         // 接收好友请求的用户名
    String status;         // pending(待确认) / accepted(已添加) / rejected(已拒绝)
    Date requestTime;      // 请求时间
    Date acceptTime;       // 确认时间
    Integer del;           // 软删除标记 1=已删除
}
