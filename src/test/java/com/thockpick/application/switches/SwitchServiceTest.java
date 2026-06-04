package com.thockpick.application.switches;

import com.thockpick.domain.switches.Switch;
import com.thockpick.domain.switches.SwitchRepository;
import com.thockpick.domain.switches.SwitchType;
import com.thockpick.infrastructure.search.document.SwitchDocument;
import com.thockpick.infrastructure.search.repository.SwitchSearchRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * SwitchService.searchSwitches 의 ES+MariaDB 하이브리드 분기 동작과
 * 단건 조회/카운트 동작을 캡처하는 특성화 테스트.
 */
@ExtendWith(MockitoExtension.class)
class SwitchServiceTest {

    @Mock
    private SwitchRepository switchRepository;

    @Mock
    private SwitchSearchRepository searchRepository;

    @InjectMocks
    private SwitchService switchService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Nested
    class SearchWithoutKeyword {

        @Test
        void 키워드가_null이면_ES를_거치지_않고_MariaDB만_조회() {
            Page<Switch> expected = new PageImpl<>(List.of());
            given(switchRepository.findAll(any(Specification.class), eq(pageable))).willReturn(expected);

            Page<Switch> result = switchService.searchSwitches(SwitchType.LINEAR, "Cherry", null, pageable);

            assertThat(result).isSameAs(expected);
            verify(searchRepository, never()).findByNicknames(any());
            verify(searchRepository, never()).findByName(any());
            verify(searchRepository, never()).findByCategory(any());
        }

        @Test
        void 키워드가_공백문자열이면_ES를_거치지_않는다() {
            given(switchRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of()));

            switchService.searchSwitches(null, null, "   ", pageable);

            verify(searchRepository, never()).findByNicknames(any());
        }
    }

    @Nested
    class SearchWithKeyword {

        @Test
        void ES_결과가_모두_비면_MariaDB를_조회하지_않고_빈페이지() {
            given(searchRepository.findByNicknames("brown")).willReturn(List.of());
            given(searchRepository.findByName("brown")).willReturn(List.of());
            given(searchRepository.findByCategory("brown")).willReturn(List.of());

            Page<Switch> result = switchService.searchSwitches(null, null, "brown", pageable);

            assertThat(result.isEmpty()).isTrue();
            verify(switchRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        void ES_결과가_있으면_해당_ID로_MariaDB를_조회() {
            SwitchDocument doc = SwitchDocument.builder().id(1L).name("Cherry MX Brown").build();
            Page<Switch> dbResult = new PageImpl<>(List.of());
            given(searchRepository.findByNicknames("체갈")).willReturn(List.of(doc));
            given(searchRepository.findByName("체갈")).willReturn(List.of());
            given(searchRepository.findByCategory("체갈")).willReturn(List.of());
            given(switchRepository.findAll(any(Specification.class), eq(pageable))).willReturn(dbResult);

            Page<Switch> result = switchService.searchSwitches(null, null, "체갈", pageable);

            assertThat(result).isSameAs(dbResult);
            verify(switchRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        void 세_소스에서_중복된_문서가_나와도_MariaDB는_한번만_조회() {
            SwitchDocument doc1 = SwitchDocument.builder().id(1L).name("a").build();
            SwitchDocument doc1Dup = SwitchDocument.builder().id(1L).name("a").build();
            SwitchDocument doc2 = SwitchDocument.builder().id(2L).name("b").build();
            given(searchRepository.findByNicknames("체리")).willReturn(List.of(doc1));
            given(searchRepository.findByName("체리")).willReturn(List.of(doc1Dup, doc2));
            given(searchRepository.findByCategory("체리")).willReturn(List.of(doc2));
            given(switchRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of()));

            switchService.searchSwitches(null, null, "체리", pageable);

            verify(switchRepository).findAll(any(Specification.class), eq(pageable));
        }
    }

    @Nested
    class FindById {

        @Test
        void ID로_조회_성공() {
            Switch sw = Switch.builder().name("Cherry MX Brown").type(SwitchType.TACTILE).build();
            given(switchRepository.findById(1L)).willReturn(Optional.of(sw));

            assertThat(switchService.findSwitchById(1L).getName()).isEqualTo("Cherry MX Brown");
        }

        @Test
        void 존재하지_않는_ID는_예외() {
            given(switchRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> switchService.findSwitchById(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    class Counts {

        @Test
        void 전체_개수_조회() {
            given(switchRepository.count()).willReturn(42L);
            assertThat(switchService.countAllSwitches()).isEqualTo(42L);
        }

        @Test
        void 타입별_개수_조회() {
            given(switchRepository.countByType(SwitchType.LINEAR)).willReturn(15L);
            assertThat(switchService.countByType(SwitchType.LINEAR)).isEqualTo(15L);
        }
    }
}
