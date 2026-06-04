package com.thockpick;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// 전체 컨텍스트 로딩에 MariaDB/Elasticsearch/Google Sheets 설정이 필요하다.
// 통합 테스트 인프라(Testcontainers 등) 도입 전까지 비활성화한다.
@Disabled("전체 컨텍스트 로딩에 외부 인프라(MariaDB/ES/Sheets)가 필요 — 통합 테스트 인프라 도입 후 활성화")
@SpringBootTest
class ThockPickServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
