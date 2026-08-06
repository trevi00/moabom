package com.moabom;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * SC2 의 게이트. ApplicationModules.of(...).verify() 는 Spring 컨텍스트를 띄우지 않는
 * 정적 분석이라 DB·Docker 없이 수 초에 끝난다 → 커밋 단위 quick gate 로 적합하다.
 * PITFALLS Pitfall 5: 위반 1건에서 고치는 비용과 40건에서 고치는 비용은 다른 종이다.
 */
class ModularityTests {

    static final ApplicationModules modules = ApplicationModules.of(MoabomApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void detectsEightModules() {
        List<String> names = new ArrayList<>();
        modules.forEach(m -> names.add(m.getName()));
        assertThat(names).hasSize(8)
            .containsExactlyInAnyOrder("bff", "catalog", "playback", "profile",
                                       "recommendation", "experiment", "ingest", "shared");
    }

    @Test
    void writesDocumentation() {
        new Documenter(modules).writeDocumentation();
    }

    /**
     * RESEARCH P-3: "allowedDependencies 를 선언하지 않은 모듈은 제약이 없다 — 다른 모듈의 루트 패키지 +
     * 모든 named interface 에 접근 가능. 경계는 명시적으로 선언해야만 존재한다."
     *
     * 즉 verify() 가 green 인 것만으로는 경계가 있다는 증거가 되지 않는다 — 선언이 하나도 없어도 green 이다
     * (실측: 이 플랜 Task 3 이전 상태에서 위 3개 테스트가 전부 통과했다). 그래서 "8모듈 전부가 자기 경계를
     * 명시 선언했다"를 별도로 기계 검사한다. 이것이 없으면 누군가 package-info 를 지워도 아무도 붉어지지 않는다.
     */
    @Test
    void everyModuleExplicitlyDeclaresItsBoundary() throws Exception {
        List<String> moduleNames = new ArrayList<>();
        modules.forEach(m -> moduleNames.add(m.getName()));

        for (String name : moduleNames) {
            Class<?> packageInfo = Class.forName("com.moabom." + name + ".package-info");
            var declaration = packageInfo.getPackage()
                .getAnnotation(org.springframework.modulith.ApplicationModule.class);

            assertThat(declaration)
                .as("모듈 '%s' 에 @ApplicationModule 선언이 없다 (package-info.java 누락/삭제)", name)
                .isNotNull();

            // allowedDependencies 미선언 시 기본값은 OPEN_TOKEN 한 개 = "제약 없음".
            assertThat(declaration.allowedDependencies())
                .as("모듈 '%s' 가 allowedDependencies 를 명시하지 않았다 = 다른 모듈 전부에 열려 있다", name)
                .doesNotContain(org.springframework.modulith.ApplicationModule.OPEN_TOKEN);
        }
    }
}
