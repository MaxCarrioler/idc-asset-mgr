export const ACCESSORY_CONFIG = {
  cpus: {
    title: 'CPU',
    apiPath: '/api/accessories/cpus',
    fields: [
      { key: 'model', label: '型号', required: true },
      { key: 'cores', label: '核数', type: 'number', required: true },
      { key: 'threads', label: '线程数', type: 'number', required: true },
      { key: 'baseFrequencyGhz', label: '主频(GHz)', type: 'number', step: 0.01, required: true },
      { key: 'maxFrequencyGhz', label: '最大频率(GHz)', type: 'number', step: 0.01 },
      { key: 'socket', label: '插槽' },
      { key: 'manufacturer', label: '厂商' },
      { key: 'quantity', label: '数量', type: 'number' },
      { key: 'remark', label: '备注' }
    ],
    columns: [
      { key: 'model', label: '型号' },
      { key: 'cores', label: '核数' },
      { key: 'threads', label: '线程数' },
      { key: 'baseFrequencyGhz', label: '主频(GHz)' },
      { key: 'maxFrequencyGhz', label: '最大频率(GHz)' },
      { key: 'manufacturer', label: '厂商' },
      { key: 'quantity', label: '数量' }
    ]
  },
  memories: {
    title: '内存',
    apiPath: '/api/accessories/memories',
    fields: [
      { key: 'stickCount', label: '内存条数量', type: 'number', required: true },
      { key: 'capacityPerStickGb', label: '单条容量(GB)', type: 'number', required: true },
      { key: 'frequencyMhz', label: '频率(MHz)', type: 'number', required: true },
      { key: 'model', label: '型号', required: true },
      { key: 'memoryType', label: '类型(DDR4/DDR5)' },
      { key: 'manufacturer', label: '厂商' },
      { key: 'slotInfo', label: '插槽信息' },
      { key: 'remark', label: '备注' }
    ],
    columns: [
      { key: 'stickCount', label: '条数' },
      { key: 'capacityPerStickGb', label: '单条容量(GB)' },
      { key: 'frequencyMhz', label: '频率(MHz)' },
      { key: 'model', label: '型号' },
      { key: 'memoryType', label: '类型' },
      { key: 'manufacturer', label: '厂商' }
    ]
  },
  'hard-disks': {
    title: '硬盘',
    apiPath: '/api/accessories/hard-disks',
    fields: [
      { key: 'model', label: '型号', required: true },
      { key: 'capacityGb', label: '容量(GB)', type: 'number', required: true },
      { key: 'diskType', label: '类型(SSD/HDD/NVMe)', required: true },
      { key: 'interfaceType', label: '接口(SATA/SAS/NVMe)' },
      { key: 'rpm', label: '转速(RPM)', type: 'number' },
      { key: 'formFactor', label: '规格(2.5/3.5/U.2)' },
      { key: 'slotPosition', label: '槽位' },
      { key: 'serialNumber', label: '序列号' },
      { key: 'healthStatus', label: '健康状态' },
      { key: 'manufacturer', label: '厂商' },
      { key: 'remark', label: '备注' }
    ],
    columns: [
      { key: 'model', label: '型号' },
      { key: 'capacityGb', label: '容量(GB)' },
      { key: 'diskType', label: '类型' },
      { key: 'interfaceType', label: '接口' },
      { key: 'slotPosition', label: '槽位' },
      { key: 'healthStatus', label: '健康状态' }
    ]
  },
  'network-cards': {
    title: '网卡',
    apiPath: '/api/accessories/network-cards',
    fields: [
      { key: 'model', label: '型号', required: true },
      { key: 'portCount', label: '端口数', type: 'number', required: true },
      { key: 'speedGbps', label: '速率(Gbps)', type: 'number', step: 0.1, required: true },
      { key: 'interfaceType', label: '接口类型(RJ45/SFP+)' },
      { key: 'macAddress', label: 'MAC地址' },
      { key: 'manufacturer', label: '厂商' },
      { key: 'pciSlot', label: 'PCI插槽' },
      { key: 'driverVersion', label: '驱动版本' },
      { key: 'remark', label: '备注' }
    ],
    columns: [
      { key: 'model', label: '型号' },
      { key: 'portCount', label: '端口数' },
      { key: 'speedGbps', label: '速率(Gbps)' },
      { key: 'interfaceType', label: '接口' },
      { key: 'pciSlot', label: 'PCI插槽' },
      { key: 'manufacturer', label: '厂商' }
    ]
  },
  'hba-cards': {
    title: 'HBA卡',
    apiPath: '/api/accessories/hba-cards',
    fields: [
      { key: 'model', label: '型号', required: true },
      { key: 'portCount', label: '端口数', type: 'number', required: true },
      { key: 'speedGbps', label: '速率(Gbps)', type: 'number', step: 0.1, required: true },
      { key: 'interfaceType', label: '接口类型(FC/iSCSI)' },
      { key: 'firmwareVersion', label: '固件版本' },
      { key: 'wwnn', label: 'WWNN' },
      { key: 'wwpn', label: 'WWPN' },
      { key: 'manufacturer', label: '厂商' },
      { key: 'pciSlot', label: 'PCI插槽' },
      { key: 'remark', label: '备注' }
    ],
    columns: [
      { key: 'model', label: '型号' },
      { key: 'portCount', label: '端口数' },
      { key: 'speedGbps', label: '速率(Gbps)' },
      { key: 'interfaceType', label: '接口' },
      { key: 'firmwareVersion', label: '固件' },
      { key: 'pciSlot', label: 'PCI插槽' }
    ]
  },
  'raid-sas-cards': {
    title: 'RAID/SAS卡',
    apiPath: '/api/accessories/raid-sas-cards',
    fields: [
      { key: 'model', label: '型号', required: true },
      { key: 'raidLevelsSupported', label: '支持RAID级别' },
      { key: 'cacheSizeMb', label: '缓存(MB)', type: 'number', required: true },
      { key: 'portCount', label: '端口数', type: 'number', required: true },
      { key: 'interfaceType', label: '接口类型' },
      { key: 'firmwareVersion', label: '固件版本' },
      { key: 'batteryBacked', label: '电池备份', type: 'boolean' },
      { key: 'manufacturer', label: '厂商' },
      { key: 'pciSlot', label: 'PCI插槽' },
      { key: 'remark', label: '备注' }
    ],
    columns: [
      { key: 'model', label: '型号' },
      { key: 'raidLevelsSupported', label: 'RAID级别' },
      { key: 'cacheSizeMb', label: '缓存(MB)' },
      { key: 'portCount', label: '端口数' },
      { key: 'batteryBacked', label: '电池备份', render: (v) => (v ? '是' : '否') },
      { key: 'pciSlot', label: 'PCI插槽' }
    ]
  },
  'power-modules': {
    title: '电源模块',
    apiPath: '/api/accessories/power-modules',
    fields: [
      { key: 'model', label: '型号', required: true },
      { key: 'ratedPowerWatt', label: '额定功率(W)', type: 'number', required: true },
      { key: 'efficiencyRating', label: '能效等级' },
      { key: 'inputVoltage', label: '输入电压' },
      { key: 'status', label: '状态(NORMAL/FAILED/STANDBY)' },
      { key: 'manufacturer', label: '厂商' },
      { key: 'serialNumber', label: '序列号' },
      { key: 'slotPosition', label: '槽位' },
      { key: 'remark', label: '备注' }
    ],
    columns: [
      { key: 'model', label: '型号' },
      { key: 'ratedPowerWatt', label: '功率(W)' },
      { key: 'efficiencyRating', label: '能效' },
      { key: 'status', label: '状态' },
      { key: 'slotPosition', label: '槽位' },
      { key: 'manufacturer', label: '厂商' }
    ]
  }
}

export const ACCESSORY_TYPES = Object.keys(ACCESSORY_CONFIG)
