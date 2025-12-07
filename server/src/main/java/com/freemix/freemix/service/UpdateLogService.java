package com.freemix.freemix.service;

import com.freemix.freemix.enetiy.UpdateLog;
import java.util.List;

public interface UpdateLogService {
    UpdateLog createUpdateLog(UpdateLog updateLog);
    List<UpdateLog> getAllUpdateLogs(int page, int size);
    UpdateLog getLatestUpdateLog();
    void deleteUpdateLog(String id);
    UpdateLog publishUpdateLog(String id);
}
