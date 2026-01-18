package com.thockpick.presentation.api;

import com.thockpick.global.common.ApiResponse;
import com.thockpick.infrastructure.sync.googlesheets.service.SwitchSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 스위치 동기화 API 컨트롤러
 */
@RestController
@RequestMapping("/api/sync/switches")
@RequiredArgsConstructor
public class SwitchSyncController {

    private final SwitchSyncService switchSyncService;

    @PostMapping
    public ResponseEntity<ApiResponse<Integer>> syncSwitches() {
        int count = switchSyncService.syncAllSwitches();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
