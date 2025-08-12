package com.freemix.freemix.enetiy;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class childGoals {
    String message;
    boolean finish;
    @JsonFormat(timezone = "YYYY-MM-DD")
    Date finishDate;
}
