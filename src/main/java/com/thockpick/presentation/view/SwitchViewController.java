package com.thockpick.presentation.view;

import com.thockpick.application.switches.SwitchService;
import com.thockpick.domain.switches.Switch;
import com.thockpick.domain.switches.SwitchType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
    public String listSwitches(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String keyword,
            Model model) {

        List<Switch> switches;

        if (keyword != null && !keyword.isEmpty()) {
            switches = switchService.searchSwitches(keyword);
        } else if (type != null && !type.isEmpty()) {
            switches = switchService.findSwitchesByType(SwitchType.valueOf(type));
        } else if (manufacturer != null && !manufacturer.isEmpty()) {
            switches = switchService.findSwitchesByManufacturer(manufacturer);
        } else {
            switches = switchService.findAllSwitches();
        }

        model.addAttribute("switches", switches);
        model.addAttribute("types", SwitchType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedManufacturer", manufacturer);
        model.addAttribute("keyword", keyword);

        return "switches/list";
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
