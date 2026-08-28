package com.dcasset.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HardwareStatisticsDTO {

    /** 符合条件的服务器数量 */
    private int serverCount;

    /** CPU 核心总数 */
    private int totalCores;

    /** CPU 线程总数 */
    private int totalThreads;

    /** 内存总容量 (GB) */
    private long totalMemoryGb;

    /** 硬盘物理总容量 (TB) */
    private double totalDiskCapacityTb;

    /** 各规格硬盘物理总容量，固定顺序返回全部规格列 */
    private List<DiskSpecSummary> diskSpecSummaries = new ArrayList<>();

    @Data
    public static class DiskSpecSummary {
        private String spec;
        private int diskCount;
        private double totalCapacityTb;

        public DiskSpecSummary() {
        }

        public DiskSpecSummary(String spec, int diskCount, double totalCapacityTb) {
            this.spec = spec;
            this.diskCount = diskCount;
            this.totalCapacityTb = totalCapacityTb;
        }
    }
}
