package com.pig4cloud.pig.dc.server.config;

import com.pig4cloud.pig.dc.server.entity.ServerEntity;
import com.pig4cloud.pig.dc.server.entity.ServerOperationRecordEntity;
import com.pig4cloud.pig.dc.server.entity.ServerTypeEntity;
import com.pig4cloud.pig.dc.server.entity.CpuArchitectureEntity;
import com.pig4cloud.pig.dc.server.repository.ServerOperationRecordRepository;
import com.pig4cloud.pig.dc.server.repository.ServerRepository;
import com.pig4cloud.pig.dc.server.repository.ServerTypeRepository;
import com.pig4cloud.pig.dc.server.repository.CpuArchitectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final ServerRepository serverRepository;
    private final ServerOperationRecordRepository operationRecordRepository;
    private final ServerTypeRepository serverTypeRepository;
    private final CpuArchitectureRepository cpuArchitectureRepository;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            initServerTypes();
            initCpuArchitectures();
            if (serverRepository.count() > 0) {
                return;
            }
            ServerEntity s1 = createServer("SRV-DB-001", "STORAGE", "192.168.10.101", "Rack-A-U10",
                    "华北-北京", "PowerEdge R750", "Dell", "SN-DELL-001", "RUNNING", "CentOS 7.9", "X86_64");
            ServerEntity s2 = createServer("SRV-DB-002", "STORAGE", "192.168.10.102", "Rack-A-U12",
                    "华北-北京", "PowerEdge R750", "Dell", "SN-DELL-002", "RUNNING", "CentOS 7.9", "X86_64");
            createServer("SRV-WEB-001", "COMPUTE", "192.168.10.201", "Rack-B-U05",
                    "华东-上海", "ProLiant DL380 Gen10", "HPE", "SN-HPE-001", "RUNNING", "Ubuntu 22.04", "X86_64");
            createServer("SRV-STG-001", "STORAGE", "192.168.10.301", "Rack-C-U08",
                    "华南-深圳", "ThinkSystem SR650", "Lenovo", "SN-LNV-001", "MAINTENANCE", "RHEL 8.6", "X86_64");
            createServer("SRV-CMP-001", "COMPUTE", "192.168.10.401", "Rack-D-U15",
                    "西南-成都", "FusionServer 2288H V6", "Huawei", "SN-HW-001", "RUNNING", "ESXi 8.0", "ARM64");

            createSampleRecord(s1.getId(), "新建服务器：SRV-DB-001", "初始录入",
                    LocalDateTime.now().minusDays(30));
            createSampleRecord(s1.getId(), "状态：运行中 → 维护中", "计划性维护窗口",
                    LocalDateTime.now().minusDays(7));
            createSampleRecord(s1.getId(), "状态：维护中 → 运行中", "维护完成，恢复上线",
                    LocalDateTime.now().minusDays(6));
            createSampleRecord(s2.getId(), "新建服务器：SRV-DB-002", "初始录入",
                    LocalDateTime.now().minusDays(28));
            createSampleRecord(s2.getId(), "操作系统：CentOS 7.9 → CentOS 7.9（内核升级）", "安全补丁升级",
                    LocalDateTime.now().minusDays(3));
        };
    }

    private void initServerTypes() {
        if (serverTypeRepository.count() > 0) {
            return;
        }
        createBuiltInType("COMPUTE", "计算节点", "统计 CPU 核心、线程及内存", true, false);
        createBuiltInType("STORAGE", "存储节点", "统计 CPU 核心、内存及硬盘容量", false, true);
    }

    private void createBuiltInType(String code, String name, String description,
                                   boolean trackThreads, boolean includeDiskStats) {
        ServerTypeEntity type = new ServerTypeEntity();
        type.setCode(code);
        type.setName(name);
        type.setDescription(description);
        type.setTrackThreads(trackThreads);
        type.setIncludeDiskStats(includeDiskStats);
        type.setBuiltIn(true);
        serverTypeRepository.save(type);
    }

    private void initCpuArchitectures() {
        if (cpuArchitectureRepository.count() > 0) {
            return;
        }
        createBuiltInCpuArch("X86_64", "x86_64", "Intel/AMD 64 位架构");
        createBuiltInCpuArch("ARM64", "ARM64", "ARM 64 位架构");
        createBuiltInCpuArch("IA64", "IA64", "Itanium 64 位架构");
        createBuiltInCpuArch("OTHER", "其他", "其他 CPU 架构");
    }

    private void createBuiltInCpuArch(String code, String name, String description) {
        CpuArchitectureEntity entity = new CpuArchitectureEntity();
        entity.setCode(code);
        entity.setName(name);
        entity.setDescription(description);
        entity.setBuiltIn(true);
        cpuArchitectureRepository.save(entity);
    }

    private ServerEntity createServer(String name, String serverType, String ip, String rack, String region,
                                      String model, String manufacturer, String serial,
                                      String status, String os, String cpuArchitecture) {
        ServerEntity server = new ServerEntity();
        server.setServerName(name);
        server.setServerType(serverType);
        server.setIpAddress(ip);
        server.setRackLocation(rack);
        server.setRegion(region);
        server.setServerModel(model);
        server.setManufacturer(manufacturer);
        server.setSerialNumber(serial);
        server.setStatus(status);
        server.setOsType(os);
        server.setCpuArchitecture(cpuArchitecture);
        return serverRepository.save(server);
    }

    private void createSampleRecord(Long serverId, String content, String reason, LocalDateTime time) {
        ServerOperationRecordEntity record = new ServerOperationRecordEntity();
        record.setServerId(serverId);
        record.setChangeContent(content);
        record.setChangeReason(reason);
        record.setModifiedAt(time);
        operationRecordRepository.save(record);
    }
}
