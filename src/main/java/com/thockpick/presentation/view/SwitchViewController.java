package com.thockpick.presentation.view;

import com.thockpick.application.switches.SwitchService;
import com.thockpick.domain.switches.Switch;
import com.thockpick.domain.switches.SwitchType;
import com.thockpick.presentation.dto.SwitchListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 스위치 뷰 컨트롤러 (Thymeleaf)
 */
@Controller
@RequestMapping("/switches")
@RequiredArgsConstructor
public class SwitchViewController {

    private final SwitchService switchService;

    /**
     * 스위치 목록 페이지
     */
    @GetMapping
    public String listSwitches(Model model) {
        model.addAttribute("types", SwitchType.values());
        return "switches/list";
    }

    /**
     * 스위치 목록 API (Ajax용)
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSwitches(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        // String type -> Enum 변환
        SwitchType switchType = null;
        if (type != null && !type.isEmpty()) {
            try {
                switchType = SwitchType.valueOf(type);
            } catch (IllegalArgumentException e) {
                // 유효하지 않은 타입 값이 오면 무시 (null로 처리하여 전체 검색)
            }
        }

        // 엔티티 페이지 조회
        Page<Switch> switchPage = switchService.searchSwitches(switchType, manufacturer, keyword, pageable);

        // 엔티티 -> DTO 변환
        Page<SwitchListResponse> dtoPage = switchPage.map(SwitchListResponse::from);

        Map<String, Object> response = new HashMap<>();
        response.put("switches", dtoPage.getContent());
        response.put("currentPage", dtoPage.getNumber());
        response.put("totalPages", dtoPage.getTotalPages());
        response.put("totalElements", dtoPage.getTotalElements());
        response.put("hasNext", dtoPage.hasNext());
        response.put("hasPrevious", dtoPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * 스위치 상세 페이지
     */
    @GetMapping("/{id}")
    public String switchDetail(@PathVariable Long id, Model model) {
        Switch switchEntity = switchService.findSwitchById(id);
        model.addAttribute("switch", switchEntity);
        return "switches/detail";
    }
}
