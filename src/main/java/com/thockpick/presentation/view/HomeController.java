package com.thockpick.presentation.view;

import com.thockpick.application.switches.SwitchService;
import com.thockpick.domain.switches.SwitchType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 홈 화면 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SwitchService switchService;

    @GetMapping("/")
    public String home(Model model) {
        // 전체 스위치 개수
        long totalCount = switchService.countAllSwitches();

        // 타입별 개수
        Map<SwitchType, Long> typeCountMap = new LinkedHashMap<>();
        for (SwitchType type : SwitchType.values()) {
            typeCountMap.put(type, switchService.countByType(type));
        }

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("typeCountMap", typeCountMap);
        model.addAttribute("types", SwitchType.values());

        return "index";
    }
}
