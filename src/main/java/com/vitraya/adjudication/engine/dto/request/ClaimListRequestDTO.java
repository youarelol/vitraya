package com.vitraya.adjudication.engine.dto.request;

import com.vitraya.adjudication.engine.dto.enums.DataSortEnum;
import com.vitraya.adjudication.engine.utils.DateUtil;
import lombok.Builder;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Builder
public class ClaimListRequestDTO {
    // Below are the attributes which are always used to filter the claim data.
    private int pageNo = 1;
    private int pageSize = 20;
    private String startDate = null;
    private String endDate = null;
    private String insurerId = null;
    private DataSortEnum dataSortBy = DataSortEnum.DATE_UPDATED;

    // Optional attributes which are used to filter the claim data.
    private String attributeName = null;
    private String attributeValue = null;

    // If the startDate and endDate is null then we need to set the 30 days back date from current date.
    public void checkAndUpdateDateRange() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (startDate == null) {
            startDate = formatter.format(DateUtil.getPastOrFutureDate(new Date(), -30));
        }
        if (endDate == null) {
            endDate = formatter.format(new Date(System.currentTimeMillis()));
        }
    }
}
