package com.thockpick.infrastructure.sync.googlesheets.service;

import com.thockpick.application.switches.SwitchNicknameService;
import com.thockpick.domain.switches.Switch;
import com.thockpick.domain.switches.SwitchRepository;
import com.thockpick.infrastructure.search.document.SwitchDocument;
import com.thockpick.infrastructure.search.repository.SwitchSearchRepository;
import com.thockpick.infrastructure.sync.googlesheets.dto.SwitchSheetRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Google Sheets 데이터를 Switch 엔티티와 동기화하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SwitchSyncService {

    private final GoogleSheetsService googleSheetsService;
    private final SwitchRepository switchRepository;

    // ES 저장소 & 별명 서비스 주입
    private final SwitchSearchRepository switchSearchRepository;
    private final SwitchNicknameService switchNicknameService;

    /**
     * Google Sheets의 모든 스위치 데이터를 DB와 동기화
     * 
     * @return 동기화된 스위치 개수 (신규 생성 + 업데이트)
     */
    public int syncAllSwitches() {
        log.info("Google Sheets 스위치 데이터 동기화 시작");

        List<SwitchSheetRow> sheetRows = googleSheetsService.readSwitchData();
        
        if (sheetRows.isEmpty()) {
            log.warn("동기화할 데이터가 없습니다.");
            return 0;
        }

        int createdCount = 0;
        int updatedCount = 0;

        for (SwitchSheetRow row : sheetRows) {
            try {
                // 1. 필수값 검증 로직 추가 (DB 에러 방지)
                if (row.getName() == null || row.getType() == null) {
                    log.warn("필수 데이터 누락으로 스킵 (행: {}): 이름 또는 타입 없음", row.getRowNumber());
                    continue;
                }

                // 2. 개별 처리 (트랜잭션이 각각 동작함)
                processSingleSwitch(row);

                // 성공 시 카운트 (업데이트인지 생성인지는 processSingleSwitch 안에서 판단하거나 단순 합산)
                createdCount++;

            } catch (Exception e) {
                // 이제 여기서 에러가 나도 다음 행에는 영향 없음
                log.error("스위치 동기화 실패 (행: {}): {}", row.getRowNumber(), e.getMessage());
            }
        }

        return createdCount;
    }

    /**
     * 개별 스위치 처리 로직 분리
     * (private 메서드라 @Transactional이 안 먹히지만, repository.save()가 트랜잭션을 가짐)
     */
    private void processSingleSwitch(SwitchSheetRow row) {
        Switch savedSwitch;     // 저장된 스위치를 담을 변수

        // 1. 행 번호 + 카테고리(탭 이름) 조합으로 정확한 데이터 찾기
        Optional<Switch> existingSwitch = switchRepository.findByGoogleSheetsRowAndCategory(
                row.getRowNumber(),
                row.getCategory()
        );

        // 2. 없으면 이름으로 백업 검색 (혹시 모를 중복 방지)
        if (existingSwitch.isEmpty() && row.getName() != null) {
            existingSwitch = switchRepository.findByName(row.getName());
        }

        if (existingSwitch.isPresent()) {
            Switch switchEntity = existingSwitch.get();
            switchEntity.updateFromGoogleSheets(
                    row.toEntity().getName(),
                    row.toEntity().getType(),
                    row.toEntity().getCategory(),
                    // ... 필드들 ...
                    row.toEntity().getWeight(),
                    row.toEntity().getManufacturer(),
                    row.toEntity().getPrice(),
                    row.toEntity().getActuationForce(),
                    row.toEntity().getBottomOutForce(),
                    row.toEntity().getTravelDistance(),
                    row.toEntity().getPreTravel(),
                    row.toEntity().getSpringType(),
                    row.toEntity().getStemMaterial(),
                    row.toEntity().getHousingMaterial(),
                    row.toEntity().getSoundProfile(),
                    row.toEntity().getIsLubed(),
                    row.toEntity().getDescription()
            );
            savedSwitch = switchRepository.save(switchEntity);  // 업데이트된 엔티티 저장
        } else {
            Switch newSwitch = row.toEntity();
            savedSwitch = switchRepository.save(newSwitch);     // 신규 엔티티 저장
        }

        // Elasticsearch 에도 저장 (동기화)
        saveToElasticsearch(savedSwitch);
    }

    /**
     * ES 저장 로직 분리
     * @param switchEntity
     */
    private void saveToElasticsearch(Switch switchEntity) {
        try {
            String originalName = switchEntity.getName();

            // 이름에서 한글 분리 (예: "MX Brown(체갈)" → name: "MX Brown", korean: ["체갈"])
            List<String> koreanFromName = new ArrayList<>();
            String cleanedName = extractKoreanFromName(originalName, koreanFromName);

            // JSON 기반 별명 + 이름에서 추출한 한글 합치기
            List<String> nicknames = new ArrayList<>(switchNicknameService.getNicknames(originalName));
            nicknames.addAll(koreanFromName);

            SwitchDocument doc = SwitchDocument.builder()
                    .id(switchEntity.getId())
                    .name(cleanedName)
                    .brand(switchEntity.getManufacturer())
                    .category(switchEntity.getCategory())
                    .nicknames(nicknames)
                    .build();

            switchSearchRepository.save(doc);

        } catch (Exception e) {
            log.error("Elasticsearch 인덱싱 실패 (ID: {}): {}", switchEntity.getId(), e.getMessage());
        }
    }

    /**
     * 스위치 이름에서 한글 토큰을 분리
     * 예: "MX Brown(체갈)" → cleanedName: "MX Brown", koreanTokens: ["체갈"]
     * 예: "Beary 새끼곰" → cleanedName: "Beary", koreanTokens: ["새끼곰"]
     * 예: "Silent Red" → cleanedName: "Silent Red", koreanTokens: []
     */
    private String extractKoreanFromName(String name, List<String> koreanTokens) {
        if (name == null) return null;

        // 특수문자를 공백으로 치환
        String cleaned = name.replaceAll("[()\\[\\]{},/]", " ");

        String[] tokens = cleaned.split("\\s+");
        StringBuilder englishName = new StringBuilder();

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            if (containsKorean(token)) {
                koreanTokens.add(token);
            } else {
                if (!englishName.isEmpty()) englishName.append(" ");
                englishName.append(token);
            }
        }

        return englishName.toString().trim();
    }

    private boolean containsKorean(String text) {
        for (char c : text.toCharArray()) {
            if ((c >= '\uAC00' && c <= '\uD7AF') || (c >= '\u3130' && c <= '\u318F')) {
                return true;
            }
        }
        return false;
    }

    /**
     * 특정 행의 스위치 데이터만 동기화
     * 
     * @param rowNumber Google Sheets 행 번호
     */
    @Transactional
    public void syncSwitchByRow(int rowNumber) {
        log.info("Google Sheets 행 {} 동기화 시작", rowNumber);

        List<SwitchSheetRow> sheetRows = googleSheetsService.readSwitchData();
        Optional<SwitchSheetRow> targetRow = sheetRows.stream()
                .filter(row -> row.getRowNumber() == rowNumber)
                .findFirst();

        if (targetRow.isEmpty()) {
            log.warn("행 {}에 데이터가 없습니다.", rowNumber);
            return;
        }

        SwitchSheetRow row = targetRow.get();
        Optional<Switch> existingSwitch = switchRepository.findByGoogleSheetsRow(rowNumber);

        if (existingSwitch.isPresent()) {
            Switch switchEntity = existingSwitch.get();
            switchEntity.updateFromGoogleSheets(
                    row.toEntity().getName(),
                    row.toEntity().getType(),
                    row.toEntity().getCategory(),
                    row.toEntity().getWeight(),
                    row.toEntity().getManufacturer(),
                    row.toEntity().getPrice(),
                    row.toEntity().getActuationForce(),
                    row.toEntity().getBottomOutForce(),
                    row.toEntity().getTravelDistance(),
                    row.toEntity().getPreTravel(),
                    row.toEntity().getSpringType(),
                    row.toEntity().getStemMaterial(),
                    row.toEntity().getHousingMaterial(),
                    row.toEntity().getSoundProfile(),
                    row.toEntity().getIsLubed(),
                    row.toEntity().getDescription()
            );
            switchRepository.save(switchEntity);
            log.info("스위치 업데이트 완료: {}", switchEntity.getName());
        } else {
            Switch newSwitch = row.toEntity();
            switchRepository.save(newSwitch);
            log.info("스위치 생성 완료: {}", newSwitch.getName());
        }
    }
}
