
package com.connection.device.controller;

import com.connection.device.model.DeviceDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** . */
@AllArgsConstructor
@Getter
public class DevicesListResponse {
    private final List<DeviceDto> devices;
    private final PaginationInfo pagination;

    /** . */
    @AllArgsConstructor
    @Getter
    public static class PaginationInfo {
        private final int offset;
        private final int limit;
        private final int totalCount;
        private final boolean hasMore;
    }
}