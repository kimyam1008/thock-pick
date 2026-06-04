package com.thockpick.infrastructure.sync.googlesheets.service;

import com.thockpick.application.switches.SwitchNicknameService;
import com.thockpick.domain.switches.Switch;
import com.thockpick.domain.switches.SwitchRepository;
import com.thockpick.domain.switches.SwitchType;
import com.thockpick.infrastructure.search.document.SwitchDocument;
import com.thockpick.infrastructure.search.repository.SwitchSearchRepository;
import com.thockpick.infrastructure.sync.googlesheets.dto.SwitchSheetRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * SwitchSyncService.syncAllSwitches 의 동기화 흐름(스킵/생성/업데이트/부분실패)과
 * ES 인덱싱 시 이름에서 한글을 분리해 별명에 합치는 동작을 캡처하는 특성화 테스트.
 */
@ExtendWith(MockitoExtension.class)
class SwitchSyncServiceTest {

    @Mock
    private GoogleSheetsService googleSheetsService;

    @Mock
    private SwitchRepository switchRepository;

    @Mock
    private SwitchSearchRepository switchSearchRepository;

    @Mock
    private SwitchNicknameService switchNicknameService;

    @InjectMocks
    private SwitchSyncService switchSyncService;

    private SwitchSheetRow row(int rowNumber, String name, String type, String category) {
        return SwitchSheetRow.builder()
                .rowNumber(rowNumber).name(name).type(type).category(category)
                .build();
    }

    @Test
    void 시트가_비어있으면_0을_반환하고_아무것도_저장하지_않는다() {
        given(googleSheetsService.readSwitchData()).willReturn(List.of());

        assertThat(switchSyncService.syncAllSwitches()).isZero();
        verify(switchRepository, never()).save(any());
    }

    @Test
    void 이름이_null인_행은_스킵() {
        given(googleSheetsService.readSwitchData())
                .willReturn(List.of(row(2, null, "LINEAR", "체리")));

        assertThat(switchSyncService.syncAllSwitches()).isZero();
        verify(switchRepository, never()).save(any());
    }

    @Test
    void 타입이_null인_행은_스킵() {
        given(googleSheetsService.readSwitchData())
                .willReturn(List.of(row(2, "Cherry MX Brown", null, "체리")));

        assertThat(switchSyncService.syncAllSwitches()).isZero();
        verify(switchRepository, never()).save(any());
    }

    @Test
    void 신규_행은_생성_저장하고_ES에도_색인한다() {
        Switch saved = Switch.builder().name("Cherry MX Brown").type(SwitchType.TACTILE).build();
        given(googleSheetsService.readSwitchData())
                .willReturn(List.of(row(2, "Cherry MX Brown", "TACTILE", "체리")));
        given(switchRepository.findByGoogleSheetsRowAndCategory(2, "체리")).willReturn(Optional.empty());
        given(switchRepository.findByName("Cherry MX Brown")).willReturn(Optional.empty());
        given(switchRepository.save(any())).willReturn(saved);
        given(switchNicknameService.getNicknames(any())).willReturn(List.of());

        assertThat(switchSyncService.syncAllSwitches()).isEqualTo(1);
        verify(switchRepository).save(any());
        verify(switchSearchRepository).save(any());
    }

    @Test
    void 기존_행은_업데이트_경로를_탄다() {
        Switch existing = Switch.builder().name("Cherry MX Brown").type(SwitchType.TACTILE).build();
        given(googleSheetsService.readSwitchData())
                .willReturn(List.of(row(2, "Cherry MX Brown", "TACTILE", "체리")));
        given(switchRepository.findByGoogleSheetsRowAndCategory(2, "체리")).willReturn(Optional.of(existing));
        given(switchRepository.save(existing)).willReturn(existing);
        given(switchNicknameService.getNicknames(any())).willReturn(List.of());

        assertThat(switchSyncService.syncAllSwitches()).isEqualTo(1);
        verify(switchRepository).save(existing);
        // 행+카테고리로 찾았으므로 이름 백업검색은 호출되지 않아야 한다.
        verify(switchRepository, never()).findByName(any());
    }

    @Test
    void 한_행이_실패해도_나머지_행은_계속_처리된다() {
        Switch saved = Switch.builder().name("Cherry MX Red").type(SwitchType.LINEAR).build();
        given(googleSheetsService.readSwitchData()).willReturn(List.of(
                row(2, "Fail Row", "TACTILE", "체리"),
                row(3, "Cherry MX Red", "LINEAR", "체리")
        ));
        given(switchRepository.findByGoogleSheetsRowAndCategory(2, "체리"))
                .willThrow(new RuntimeException("DB 오류"));
        given(switchRepository.findByGoogleSheetsRowAndCategory(3, "체리")).willReturn(Optional.empty());
        given(switchRepository.findByName("Cherry MX Red")).willReturn(Optional.empty());
        given(switchRepository.save(any())).willReturn(saved);
        given(switchNicknameService.getNicknames(any())).willReturn(List.of());

        // 실패한 행은 카운트에서 제외, 성공한 행만 집계
        assertThat(switchSyncService.syncAllSwitches()).isEqualTo(1);
    }

    @Test
    void ES_색인시_이름의_한글은_분리되어_별명에_합쳐지고_name은_영문만_남는다() {
        Switch saved = Switch.builder().name("MX Brown(체갈)").type(SwitchType.TACTILE).build();
        given(googleSheetsService.readSwitchData())
                .willReturn(List.of(row(2, "MX Brown(체갈)", "TACTILE", "체리")));
        given(switchRepository.findByGoogleSheetsRowAndCategory(2, "체리")).willReturn(Optional.empty());
        given(switchRepository.findByName("MX Brown(체갈)")).willReturn(Optional.empty());
        given(switchRepository.save(any())).willReturn(saved);
        given(switchNicknameService.getNicknames("MX Brown(체갈)")).willReturn(List.of("갈축"));

        switchSyncService.syncAllSwitches();

        ArgumentCaptor<SwitchDocument> captor = ArgumentCaptor.forClass(SwitchDocument.class);
        verify(switchSearchRepository).save(captor.capture());
        SwitchDocument doc = captor.getValue();

        assertThat(doc.getName()).isEqualTo("MX Brown");
        assertThat(doc.getNicknames()).containsExactlyInAnyOrder("갈축", "체갈");
    }
}
