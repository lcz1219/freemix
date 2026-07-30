package com.freemix.freemix.enetiy;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.beans.Transient;
import java.util.Date;
import java.util.List;

@Data
public class childGoals {
    String _id;
    String message;

    boolean finish;

    @JsonFormat(timezone = "YYYY-MM-DD")
    Date finishDate;
//    String description;
    private List<childGoals> childGoals;
    
    // 存储子目标相关的文件列表
    List fileList;
    @JsonIgnore
    String excelParentTitle;

    // 完成地点信息（地图功能）
    String locationName;     // 地点名称，如"北京市朝阳区望京SOHO"
    List<Double> locationCoord; // [经度, 纬度]



}
