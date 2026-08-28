package com.dcasset.accessory.config;

import com.dcasset.accessory.entity.*;
import com.dcasset.accessory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("accessoryDataInitializer")
@RequiredArgsConstructor
public class DataInitializer {

    private final CpuRepository cpuRepository;
    private final MemoryRepository memoryRepository;
    private final HardDiskRepository hardDiskRepository;
    private final NetworkCardRepository networkCardRepository;
    private final HbaCardRepository hbaCardRepository;
    private final RaidSasCardRepository raidSasCardRepository;
    private final PowerModuleRepository powerModuleRepository;
    private final SparePartCategoryRepository sparePartCategoryRepository;
    private final SparePartRepository sparePartRepository;

    @Bean
    CommandLineRunner initAccessories() {
        return args -> {
            initSparePartCategories();
            if (cpuRepository.count() == 0) {
                initStorageServer1();
                initStorageServer2();
                initComputeServer3();
                initStorageServer4();
                initComputeServer5();
                initSparePartInventory();
            }
            backfillSparePartDefaults();
        };
    }

    private void backfillSparePartDefaults() {
        sparePartRepository.findAll().forEach(part -> {
            boolean dirty = false;
            if (part.getStatus() == null || part.getStatus().isBlank()) {
                part.setStatus("IN_STOCK");
                dirty = true;
            }
            if ((part.getSourceServerSerial() == null || part.getSourceServerSerial().isBlank())
                    && part.getSourceServerId() == null) {
                part.setSourceServerId(1L);
                part.setSourceServerSerial("SRV-DB-001");
                dirty = true;
            }
            if (dirty) {
                sparePartRepository.save(part);
            }
        });
    }

    private void initSparePartCategories() {
        if (sparePartCategoryRepository.findByCode("HARD_DISK").isPresent()) {
            sparePartRepository.deleteAll();
            sparePartCategoryRepository.deleteAll();
        }
        if (sparePartCategoryRepository.count() > 0) {
            return;
        }
        saveCategory("HDD_1_8T", "1.8T 硬盘", "1.8TB NVMe/SSD 硬盘备件", true);
        saveCategory("HDD_8T", "8T 硬盘", "8TB SAS/SATA 硬盘备件", true);
        saveCategory("HDD_4T", "4T 硬盘", "4TB SAS/SATA 硬盘备件", true);
        saveCategory("CPU_XEON_G6330", "Intel Xeon Gold 6330", "28核/56线程 CPU 备件", true);
        saveCategory("MEMORY_32GB_DDR4", "32GB DDR4 内存", "32GB DDR4 ECC RDIMM 内存条备件", true);
        saveCategory("NIC_10G_4PORT", "10G 四口网卡", "10Gbps 四口网卡备件", true);
        saveCategory("PSU_1400W", "1400W 电源", "1400W 服务器电源模块备件", true);
    }

    private void initSparePartInventory() {
        if (sparePartRepository.count() > 0) {
            return;
        }
        Long hdd18tId = categoryId("HDD_1_8T");
        Long hdd8tId = categoryId("HDD_8T");
        Long cpuId = categoryId("CPU_XEON_G6330");
        Long memoryId = categoryId("MEMORY_32GB_DDR4");

        saveSparePart(hdd18tId, "Samsung", "SN-PM9A3-001", "1.8TB NVMe SSD", "库房B-01", null, 1L, "SRV-DB-001");
        saveSparePart(hdd18tId, "Samsung", "SN-PM9A3-002", "1.8TB NVMe SSD", "库房B-01", null, 2L, "SRV-DB-002");
        saveSparePart(hdd18tId, "Samsung", "SN-PM9A3-003", "1.8TB NVMe SSD", "库房B-01", null, 1L, "SRV-DB-001");
        saveSparePart(hdd8tId, "Seagate", "SN-EXOS-001", "8TB SAS HDD", "库房B-02", null, 4L, "SRV-STG-001");
        saveSparePart(hdd8tId, "Seagate", "SN-EXOS-002", "8TB SAS HDD", "库房B-02", null, 4L, "SRV-STG-001");
        saveSparePart(cpuId, "Intel", "SN-CPU-001", "28核/56线程", "库房A-01", "冷备 CPU", 3L, "SRV-WEB-001");
        saveSparePart(cpuId, "Intel", "SN-CPU-002", "28核/56线程", "库房A-01", "冷备 CPU", 5L, "SRV-CMP-001");
        saveSparePart(memoryId, "Samsung", "SN-MEM-001", "3200MHz", "库房A-02", null, 3L, "SRV-WEB-001");
        saveSparePart(memoryId, "Samsung", "SN-MEM-002", "3200MHz", "库房A-02", null, 5L, "SRV-CMP-001");
    }

    private Long categoryId(String code) {
        return sparePartCategoryRepository.findByCode(code)
                .map(SparePartCategoryEntity::getId)
                .orElseThrow(() -> new IllegalStateException("备件种类不存在: " + code));
    }

    private void saveCategory(String code, String name, String description, boolean builtIn) {
        SparePartCategoryEntity entity = new SparePartCategoryEntity();
        entity.setCode(code);
        entity.setName(name);
        entity.setDescription(description);
        entity.setBuiltIn(builtIn);
        sparePartCategoryRepository.save(entity);
    }

    private void saveSparePart(Long categoryId, String manufacturer, String serialNumber,
                               String specSummary, String location, String remark,
                               Long sourceServerId, String sourceServerSerial) {
        SparePartEntity entity = new SparePartEntity();
        entity.setCategoryId(categoryId);
        entity.setManufacturer(manufacturer);
        entity.setSerialNumber(serialNumber);
        entity.setSpecSummary(specSummary);
        entity.setLocation(location);
        entity.setRemark(remark);
        entity.setSourceServerId(sourceServerId);
        entity.setSourceServerSerial(sourceServerSerial);
        entity.setStatus("IN_STOCK");
        sparePartRepository.save(entity);
    }

    private void initStorageServer1() {
        Long serverId = 1L;

        saveCpu(serverId, "Intel Xeon Gold 6330", 28, 56, 2.0, 3.1, 2);
        saveMemory(serverId, 8, 32, 3200, "DDR4 ECC RDIMM");

        saveDisk(serverId, "Samsung PM9A3 1.8T", 1843, "SSD", "NVMe");
        saveDisk(serverId, "Seagate Exos 8T", 8192, "HDD", "SAS");
        saveDisk(serverId, "WD Ultrastar 4T", 4096, "HDD", "SATA");
        saveDisk(serverId, "Intel D3-S4510 3.84T", 3840, "SSD", "SATA");

        NetworkCardEntity nic = new NetworkCardEntity();
        nic.setServerId(serverId);
        nic.setModel("Intel X710-DA4");
        nic.setPortCount(4);
        nic.setSpeedGbps(10.0);
        nic.setInterfaceType("SFP+");
        nic.setManufacturer("Intel");
        nic.setPciSlot("PCIe-3");
        networkCardRepository.save(nic);

        HbaCardEntity hba = new HbaCardEntity();
        hba.setServerId(serverId);
        hba.setModel("Emulex LPe35002");
        hba.setPortCount(2);
        hba.setSpeedGbps(32.0);
        hba.setInterfaceType("FC");
        hba.setFirmwareVersion("14.2.0");
        hba.setManufacturer("Broadcom");
        hba.setPciSlot("PCIe-5");
        hbaCardRepository.save(hba);

        RaidSasCardEntity raid = new RaidSasCardEntity();
        raid.setServerId(serverId);
        raid.setModel("PERC H755");
        raid.setRaidLevelsSupported("0,1,5,6,10,50,60");
        raid.setCacheSizeMb(8192);
        raid.setPortCount(8);
        raid.setInterfaceType("SAS/SATA");
        raid.setFirmwareVersion("52.21.0");
        raid.setBatteryBacked(true);
        raid.setManufacturer("Dell");
        raid.setPciSlot("PCIe-1");
        raidSasCardRepository.save(raid);

        savePowerModule(serverId, "Dell 1400W PSU", 1400, "PSU-1");
        savePowerModule(serverId, "Dell 1400W PSU", 1400, "PSU-2");
    }

    private void initStorageServer2() {
        Long serverId = 2L;
        saveCpu(serverId, "Intel Xeon Gold 6330", 28, 56, 2.0, 3.1, 2);
        saveMemory(serverId, 16, 64, 3200, "DDR4 ECC RDIMM");
        saveDisk(serverId, "Seagate Exos 2.4T", 2458, "HDD", "SAS");
        saveDisk(serverId, "Seagate Exos 4T", 4096, "HDD", "SAS");
        saveDisk(serverId, "Samsung PM9A3 1.8T", 1843, "SSD", "NVMe");
    }

    private void initComputeServer3() {
        Long serverId = 3L;
        saveCpu(serverId, "AMD EPYC 7763", 64, 128, 2.45, 3.5, 1);
        saveMemory(serverId, 16, 32, 3200, "DDR4 RDIMM");

        NetworkCardEntity nic = new NetworkCardEntity();
        nic.setServerId(serverId);
        nic.setModel("Mellanox ConnectX-6");
        nic.setPortCount(2);
        nic.setSpeedGbps(25.0);
        nic.setInterfaceType("SFP28");
        networkCardRepository.save(nic);
    }

    private void initStorageServer4() {
        Long serverId = 4L;
        saveCpu(serverId, "Intel Xeon Silver 4314", 16, 32, 2.4, 3.4, 2);
        saveMemory(serverId, 12, 32, 2933, "DDR4 ECC RDIMM");
        saveDisk(serverId, "Lenovo 8T SAS", 8192, "HDD", "SAS");
        saveDisk(serverId, "Lenovo 4T SAS", 4096, "HDD", "SAS");
        saveDisk(serverId, "Lenovo 3.84T SSD", 3840, "SSD", "SATA");
        saveDisk(serverId, "Lenovo 2.4T SAS", 2458, "HDD", "SAS");
    }

    private void initComputeServer5() {
        Long serverId = 5L;
        saveCpu(serverId, "Intel Xeon Gold 5318Y", 24, 48, 2.1, 3.4, 2);
        saveMemory(serverId, 8, 64, 3200, "DDR4 RDIMM");
    }

    private void saveCpu(Long serverId, String model, int cores, int threads,
                         double baseGhz, double maxGhz, int quantity) {
        CpuEntity cpu = new CpuEntity();
        cpu.setServerId(serverId);
        cpu.setModel(model);
        cpu.setCores(cores);
        cpu.setThreads(threads);
        cpu.setBaseFrequencyGhz(baseGhz);
        cpu.setMaxFrequencyGhz(maxGhz);
        cpu.setQuantity(quantity);
        cpuRepository.save(cpu);
    }

    private void saveMemory(Long serverId, int stickCount, int capacityPerStickGb,
                            int frequencyMhz, String model) {
        MemoryEntity memory = new MemoryEntity();
        memory.setServerId(serverId);
        memory.setStickCount(stickCount);
        memory.setCapacityPerStickGb(capacityPerStickGb);
        memory.setFrequencyMhz(frequencyMhz);
        memory.setModel(model);
        memoryRepository.save(memory);
    }

    private void saveDisk(Long serverId, String model, int capacityGb, String diskType, String interfaceType) {
        HardDiskEntity disk = new HardDiskEntity();
        disk.setServerId(serverId);
        disk.setModel(model);
        disk.setCapacityGb(capacityGb);
        disk.setDiskType(diskType);
        disk.setInterfaceType(interfaceType);
        disk.setHealthStatus("HEALTHY");
        hardDiskRepository.save(disk);
    }

    private void savePowerModule(Long serverId, String model, int watt, String slot) {
        PowerModuleEntity psu = new PowerModuleEntity();
        psu.setServerId(serverId);
        psu.setModel(model);
        psu.setRatedPowerWatt(watt);
        psu.setEfficiencyRating("80Plus Platinum");
        psu.setInputVoltage("200-240V AC");
        psu.setStatus("NORMAL");
        psu.setManufacturer("Dell");
        psu.setSlotPosition(slot);
        powerModuleRepository.save(psu);
    }
}
