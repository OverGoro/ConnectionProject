// DevicesListResponse.java
package com.connection.device.controller;

import com.connection.device.model.DeviceDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class DevicesListResponse {
    private final List<DeviceDTO> devices;
    private final PaginationInfo pagination;

    @AllArgsConstructor
    @Getter
    public static class PaginationInfo {
        private final int offset;
        private final int limit;
        private final int totalCount;
        private final boolean hasMore;
    }
}