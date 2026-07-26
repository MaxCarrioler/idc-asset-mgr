package com.pig4cloud.pig.dc.accessory.service;

import com.pig4cloud.pig.common.security.service.PigUser;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.dc.accessory.entity.*;
import com.pig4cloud.pig.dc.accessory.repository.*;
import com.pig4cloud.pig.dc.common.dto.*;
import com.pig4cloud.pig.dc.common.result.Result;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AccessoryTransferServiceImpl implements AccessoryTransferService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_OUTBOUND = "OUTBOUND";

    public static final String SPARE_IN_STOCK = "IN_STOCK";
    public static final String SPARE_RESERVED = "RESERVED";
    public static final String SPARE_OUT = "OUT";

    private static final String PERM_APPROVE = "accessory:transfer:approve";
    private static final String PERM_OUTBOUND = "accessory:transfer:outbound";

    private final AccessoryTransferRequestRepository requestRepository;
    private final AccessoryTransferItemRepository itemRepository;
    private final AccessoryOutboundRepository outboundRepository;
    private final SparePartRepository sparePartRepository;
    private final SparePartCategoryRepository categoryRepository;
    private final RestTemplate restTemplate;

    public AccessoryTransferServiceImpl(
            AccessoryTransferRequestRepository requestRepository,
            AccessoryTransferItemRepository itemRepository,
            AccessoryOutboundRepository outboundRepository,
            SparePartRepository sparePartRepository,
            SparePartCategoryRepository categoryRepository,
            @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate) {
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.outboundRepository = outboundRepository;
        this.sparePartRepository = sparePartRepository;
        this.categoryRepository = categoryRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public AccessoryTransferRequestDTO create(AccessoryTransferRequestDTO dto) {
        PigUser user = requireUser();
        if (dto.getQuantity() == null || dto.getQuantity() < 1) {
            throw new IllegalArgumentException("数量至少为 1");
        }
        SparePartCategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("配件种类不存在: " + dto.getCategoryId()));
        ServerDTO target = fetchServer(dto.getTargetServerId());
        if (target == null) {
            throw new IllegalArgumentException("目标服务器不存在: " + dto.getTargetServerId());
        }

        AccessoryTransferRequestEntity entity = new AccessoryTransferRequestEntity();
        entity.setCategoryId(category.getId());
        entity.setSpecParams(trimToNull(dto.getSpecParams()));
        entity.setQuantity(dto.getQuantity());
        entity.setPurpose(dto.getPurpose().trim());
        entity.setTargetServerId(target.getId());
        entity.setTargetServerName(target.getServerName());
        entity.setTargetServerSerial(target.getSerialNumber());
        entity.setApplicantId(user.getId());
        entity.setApplicantName(displayName(user));
        entity.setStatus(STATUS_PENDING);
        return toRequestDto(requestRepository.save(entity), List.of());
    }

    @Override
    public List<AccessoryTransferRequestDTO> list(String status) {
        PigUser user = requireUser();
        List<AccessoryTransferRequestEntity> list;
        if (canManageAll()) {
            list = (status == null || status.isBlank())
                    ? requestRepository.findAllByOrderByCreatedAtDesc()
                    : requestRepository.findByStatusOrderByCreatedAtDesc(status.trim());
        } else {
            list = requestRepository.findByApplicantIdOrderByCreatedAtDesc(user.getId());
            if (status != null && !status.isBlank()) {
                String st = status.trim();
                list = list.stream().filter(r -> st.equals(r.getStatus())).collect(Collectors.toList());
            }
        }
        return list.stream().map(r -> toRequestDto(r, itemRepository.findByRequestId(r.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public AccessoryTransferRequestDTO getById(Long id) {
        AccessoryTransferRequestEntity entity = findRequest(id);
        assertCanView(entity);
        return toRequestDto(entity, itemRepository.findByRequestId(id));
    }

    @Override
    @Transactional
    public AccessoryTransferRequestDTO approve(Long id, AccessoryTransferApproveRequest request) {
        AccessoryTransferRequestEntity entity = findRequestForUpdate(id);
        if (!STATUS_PENDING.equals(entity.getStatus())) {
            throw new IllegalArgumentException("仅待审批申请可审批通过");
        }
        List<Long> sparePartIds = request.getSparePartIds();
        if (sparePartIds == null || sparePartIds.isEmpty()) {
            throw new IllegalArgumentException("请选择具体序列号的备件");
        }
        if (sparePartIds.size() != entity.getQuantity()) {
            throw new IllegalArgumentException("选定备件数量必须等于申请数量 " + entity.getQuantity());
        }

        PigUser user = requireUser();
        List<SparePartEntity> parts = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Long sparePartId : sparePartIds) {
            if (!seen.add(sparePartId)) {
                throw new IllegalArgumentException("备件 ID 重复: " + sparePartId);
            }
            SparePartEntity part = sparePartRepository.findByIdForUpdate(sparePartId)
                    .orElseThrow(() -> new IllegalArgumentException("备件不存在: " + sparePartId));
            if (!Objects.equals(part.getCategoryId(), entity.getCategoryId())) {
                throw new IllegalArgumentException("备件种类与申请不一致: " + part.getSerialNumber());
            }
            if (!SPARE_IN_STOCK.equals(part.getStatus())) {
                throw new IllegalArgumentException("备件不可用（非在库）: " + part.getSerialNumber());
            }
            parts.add(part);
        }

        itemRepository.deleteByRequestId(id);
        for (SparePartEntity part : parts) {
            part.setStatus(SPARE_RESERVED);
            sparePartRepository.save(part);

            AccessoryTransferItemEntity item = new AccessoryTransferItemEntity();
            item.setRequestId(id);
            item.setSparePartId(part.getId());
            item.setSerialNumber(part.getSerialNumber());
            item.setApproverId(user.getId());
            itemRepository.save(item);
        }

        entity.setStatus(STATUS_APPROVED);
        entity.setApproverId(user.getId());
        entity.setApproverName(displayName(user));
        entity.setApprovedAt(LocalDateTime.now());
        entity.setRejectReason(null);
        return toRequestDto(requestRepository.save(entity), itemRepository.findByRequestId(id));
    }

    @Override
    @Transactional
    public AccessoryTransferRequestDTO reject(Long id, String reason) {
        AccessoryTransferRequestEntity entity = findRequestForUpdate(id);
        if (!STATUS_PENDING.equals(entity.getStatus())) {
            throw new IllegalArgumentException("仅待审批申请可驳回");
        }
        PigUser user = requireUser();
        entity.setStatus(STATUS_REJECTED);
        entity.setRejectReason(reason == null || reason.isBlank() ? "驳回" : reason.trim());
        entity.setApproverId(user.getId());
        entity.setApproverName(displayName(user));
        entity.setApprovedAt(LocalDateTime.now());
        return toRequestDto(requestRepository.save(entity), List.of());
    }

    @Override
    @Transactional
    public List<AccessoryOutboundDTO> outbound(Long id, AccessoryOutboundRequest request) {
        AccessoryTransferRequestEntity entity = findRequestForUpdate(id);
        if (!STATUS_APPROVED.equals(entity.getStatus())) {
            throw new IllegalArgumentException("仅审批通过的申请可出库");
        }
        List<AccessoryTransferItemEntity> items = itemRepository.findByRequestId(id);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("申请尚未绑定备件序列号");
        }

        Map<Long, AccessoryOutboundRequest.OutboundSourceItem> sourceMap = Optional.ofNullable(request)
                .map(AccessoryOutboundRequest::getSources)
                .orElse(List.of())
                .stream()
                .filter(s -> s.getSparePartId() != null)
                .collect(Collectors.toMap(AccessoryOutboundRequest.OutboundSourceItem::getSparePartId,
                        Function.identity(), (a, b) -> a));

        PigUser user = requireUser();
        String remark = request == null ? null : trimToNull(request.getRemark());
        List<AccessoryOutboundDTO> result = new ArrayList<>();

        for (AccessoryTransferItemEntity item : items) {
            SparePartEntity part = sparePartRepository.findByIdForUpdate(item.getSparePartId())
                    .orElseThrow(() -> new IllegalArgumentException("备件不存在: " + item.getSparePartId()));

            Long sourceServerId = part.getSourceServerId();
            String sourceServerSerial = part.getSourceServerSerial();
            String sourceServerName = null;

            AccessoryOutboundRequest.OutboundSourceItem override = sourceMap.get(part.getId());
            if (override != null) {
                if (override.getSourceServerId() != null) {
                    sourceServerId = override.getSourceServerId();
                }
                if (override.getSourceServerSerial() != null && !override.getSourceServerSerial().isBlank()) {
                    sourceServerSerial = override.getSourceServerSerial().trim();
                }
                if (override.getSourceServerName() != null && !override.getSourceServerName().isBlank()) {
                    sourceServerName = override.getSourceServerName().trim();
                }
            }

            if (sourceServerSerial == null || sourceServerSerial.isBlank()) {
                throw new IllegalArgumentException(
                        "出库必须选择来源服务器（真实序列号或「无」），备件: " + part.getSerialNumber());
            }

            // 「无」表示无具体来源服务器
            boolean noSource = "无".equals(sourceServerSerial);
            if (noSource) {
                sourceServerId = null;
                sourceServerName = null;
                sourceServerSerial = "无";
            } else if (sourceServerId != null && sourceServerName == null) {
                ServerDTO sourceServer = fetchServer(sourceServerId);
                if (sourceServer != null) {
                    if (sourceServerSerial == null || sourceServerSerial.isBlank()) {
                        sourceServerSerial = sourceServer.getSerialNumber();
                    }
                    sourceServerName = sourceServer.getServerName();
                }
            }

            part.setStatus(SPARE_OUT);
            part.setSourceServerId(sourceServerId);
            part.setSourceServerSerial(sourceServerSerial);
            part.setActiveServerId(entity.getTargetServerId());
            part.setActiveServerSerial(entity.getTargetServerSerial());
            part.setActiveServerName(entity.getTargetServerName());
            sparePartRepository.save(part);

            AccessoryOutboundEntity outbound = new AccessoryOutboundEntity();
            outbound.setRequestId(id);
            outbound.setSparePartId(part.getId());
            outbound.setSerialNumber(part.getSerialNumber());
            outbound.setCategoryId(part.getCategoryId());
            outbound.setSourceServerId(sourceServerId);
            outbound.setSourceServerSerial(sourceServerSerial);
            outbound.setSourceServerName(sourceServerName);
            outbound.setTargetServerId(entity.getTargetServerId());
            outbound.setTargetServerName(entity.getTargetServerName());
            outbound.setTargetServerSerial(entity.getTargetServerSerial());
            outbound.setOperatorId(user.getId());
            outbound.setOperatorName(displayName(user));
            outbound.setRemark(remark);
            outbound.setOutboundTime(LocalDateTime.now());
            result.add(toOutboundDto(outboundRepository.save(outbound)));
        }

        entity.setStatus(STATUS_OUTBOUND);
        requestRepository.save(entity);
        return result;
    }

    @Override
    public List<AccessoryOutboundDTO> listTransfers() {
        PigUser user = requireUser();
        boolean manageAll = canManageAll();
        return outboundRepository.findAllByOrderByOutboundTimeDesc().stream()
                .filter(record -> manageAll || requestRepository.findById(record.getRequestId())
                        .map(request -> Objects.equals(user.getId(), request.getApplicantId()))
                        .orElse(false))
                .map(this::toOutboundDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SparePartDTO> listAvailableSpareParts(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("配件种类不存在: " + categoryId));
        SparePartCategoryEntity category = categoryRepository.findById(categoryId).orElseThrow();
        return sparePartRepository.findByCategoryIdOrderByIdAsc(categoryId).stream()
                .filter(p -> SPARE_IN_STOCK.equals(p.getStatus()))
                .map(p -> toSpareDto(p, category.getName()))
                .collect(Collectors.toList());
    }

    private AccessoryTransferRequestEntity findRequest(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在: " + id));
    }

    private AccessoryTransferRequestEntity findRequestForUpdate(Long id) {
        return requestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在: " + id));
    }

    private void assertCanView(AccessoryTransferRequestEntity entity) {
        PigUser user = requireUser();
        if (canManageAll()) {
            return;
        }
        if (!Objects.equals(user.getId(), entity.getApplicantId())) {
            throw new IllegalArgumentException("无权查看该申请");
        }
    }

    private boolean canManageAll() {
        return hasAuthority(PERM_APPROVE) || hasAuthority(PERM_OUTBOUND) || hasAuthority("ROLE_1");
    }

    private boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority granted : authentication.getAuthorities()) {
            if (authority.equals(granted.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private PigUser requireUser() {
        PigUser user = SecurityUtils.getUser();
        if (user == null) {
            throw new IllegalArgumentException("未登录或令牌无效");
        }
        return user;
    }

    private String displayName(PigUser user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }
        return user.getUsername();
    }

    private ServerDTO fetchServer(Long serverId) {
        if (serverId == null) {
            return null;
        }
        try {
            String url = "http://pig-dc-server-biz/api/servers/" + serverId;
            ResponseEntity<Result<ServerDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                    });
            Result<ServerDTO> body = response.getBody();
            return body == null ? null : body.getData();
        } catch (Exception ex) {
            throw new IllegalArgumentException("查询服务器失败: " + serverId + " — " + ex.getMessage());
        }
    }

    private AccessoryTransferRequestDTO toRequestDto(AccessoryTransferRequestEntity entity,
                                                     List<AccessoryTransferItemEntity> items) {
        AccessoryTransferRequestDTO dto = new AccessoryTransferRequestDTO();
        dto.setId(entity.getId());
        dto.setCategoryId(entity.getCategoryId());
        categoryRepository.findById(entity.getCategoryId())
                .ifPresent(c -> dto.setCategoryName(c.getName()));
        dto.setSpecParams(entity.getSpecParams());
        dto.setQuantity(entity.getQuantity());
        dto.setPurpose(entity.getPurpose());
        dto.setTargetServerId(entity.getTargetServerId());
        dto.setTargetServerName(entity.getTargetServerName());
        dto.setTargetServerSerial(entity.getTargetServerSerial());
        dto.setApplicantId(entity.getApplicantId());
        dto.setApplicantName(entity.getApplicantName());
        dto.setStatus(entity.getStatus());
        dto.setRejectReason(entity.getRejectReason());
        dto.setApproverId(entity.getApproverId());
        dto.setApproverName(entity.getApproverName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setApprovedAt(entity.getApprovedAt());

        Map<Long, SparePartEntity> parts = items.stream()
                .map(AccessoryTransferItemEntity::getSparePartId)
                .distinct()
                .map(sparePartRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(SparePartEntity::getId, Function.identity()));

        dto.setItems(items.stream().map(item -> {
            AccessoryTransferItemDTO itemDto = new AccessoryTransferItemDTO();
            itemDto.setId(item.getId());
            itemDto.setRequestId(item.getRequestId());
            itemDto.setSparePartId(item.getSparePartId());
            itemDto.setSerialNumber(item.getSerialNumber());
            SparePartEntity part = parts.get(item.getSparePartId());
            if (part != null) {
                itemDto.setManufacturer(part.getManufacturer());
                itemDto.setSpecSummary(part.getSpecSummary());
                itemDto.setSourceServerId(part.getSourceServerId());
                itemDto.setSourceServerSerial(part.getSourceServerSerial());
            }
            return itemDto;
        }).collect(Collectors.toList()));
        return dto;
    }

    private AccessoryOutboundDTO toOutboundDto(AccessoryOutboundEntity entity) {
        AccessoryOutboundDTO dto = new AccessoryOutboundDTO();
        dto.setId(entity.getId());
        dto.setRequestId(entity.getRequestId());
        dto.setSparePartId(entity.getSparePartId());
        dto.setSerialNumber(entity.getSerialNumber());
        dto.setCategoryId(entity.getCategoryId());
        if (entity.getCategoryId() != null) {
            categoryRepository.findById(entity.getCategoryId())
                    .ifPresent(c -> dto.setCategoryName(c.getName()));
        }
        dto.setSourceServerId(entity.getSourceServerId());
        dto.setSourceServerSerial(entity.getSourceServerSerial());
        dto.setSourceServerName(entity.getSourceServerName());
        dto.setTargetServerId(entity.getTargetServerId());
        dto.setTargetServerName(entity.getTargetServerName());
        dto.setTargetServerSerial(entity.getTargetServerSerial());
        dto.setOperatorId(entity.getOperatorId());
        dto.setOperatorName(entity.getOperatorName());
        dto.setRemark(entity.getRemark());
        dto.setOutboundTime(entity.getOutboundTime());
        return dto;
    }

    private SparePartDTO toSpareDto(SparePartEntity entity, String categoryName) {
        SparePartDTO dto = new SparePartDTO();
        dto.setId(entity.getId());
        dto.setCategoryId(entity.getCategoryId());
        dto.setCategoryName(categoryName);
        dto.setManufacturer(entity.getManufacturer());
        dto.setSerialNumber(entity.getSerialNumber());
        dto.setSpecSummary(entity.getSpecSummary());
        dto.setLocation(entity.getLocation());
        dto.setRemark(entity.getRemark());
        dto.setStatus(entity.getStatus());
        dto.setSourceServerId(entity.getSourceServerId());
        dto.setSourceServerSerial(entity.getSourceServerSerial());
        return dto;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
