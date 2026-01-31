package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class SystemConfig {
    private String id;
    private String macDownloadUrl;
    private String winDownloadUrl;
    // 单例配置，可以用一个固定的ID或者type来标识
    private String type; // 例如 "DOWNLOAD_CONFIG"
}
