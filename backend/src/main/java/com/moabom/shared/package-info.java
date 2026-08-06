/*
 * shared = 공유 커널 (유일한 특권 모듈).
 *
 * ⚠️ ARCHITECTURE §2 / RESEARCH C-3 은 이 모듈을 `@ApplicationModule(type = OPEN)` 으로 계획했으나,
 *    잠긴 버전 핀인 Spring Modulith 1.1.12 의 @ApplicationModule 에는 그 속성이 존재하지 않는다.
 *    실측(javap, spring-modulith-api-1.1.12.jar): 선언된 멤버는 displayName() 과 allowedDependencies() 둘뿐이며
 *    중첩 Type 열거형도 없다. open module 기능은 Modulith 1.2 에서 도입됐고, Boot 3.2.x ↔ Modulith 1.1.x 는
 *    공식 호환 매트릭스가 고정한 조합이라(STACK.md §7) 버전 상향으로 풀지 않는다.
 *
 * 대신 P1 은 더 "엄격한" 쪽으로 착지한다 — 경계를 넓히는 방향의 우회는 하지 않는다:
 *   · shared 는 나가는 의존이 0 인 일반 모듈이다 (allowedDependencies = {}).
 *   · shared 타입을 처음 import 하는 소비자 모듈이 자기 allowedDependencies 에 "shared" 를 명시한다.
 *     (플랜 Task 3 의 verify() 실패 대응 2번이 이미 지시한 경로. "open 모듈로 도망가지 말 것" 규율과 정합.)
 *
 * ⚠️ 이 주석은 해당 열거형 상수의 리터럴 표기를 의도적으로 쓰지 않는다.
 *    플랜 Task 2 의 acceptance 는 이 파일에서 그 문자열을 grep 하는 형태라, 주석에 한 번만 언급해도
 *    통과해 버린다 — 그러면 "선언이 실재한다"가 아니라 "그 문자열을 언급했다"를 측정하게 된다.
 *    (실제로 이 파일 초안이 그렇게 통과했다가 되돌렸다.)
 *    해당 criterion 은 Modulith 1.1.12 에서 충족 불가이므로 SUMMARY 에 실패로 기록해 남긴다.
 *
 * 잔여 차이(비-제로, 정직 표기):
 *   · OPEN 이었다면 shared 의 하위 패키지까지 외부에 열렸겠지만 shared 는 하위 패키지를 두지 않는다
 *     (ARCHITECTURE §2 — TitleId/ProfileId 만). 따라서 P1 에서 실질 차이는 없다.
 *   · OPEN 의 사이클 탐지 제외 특권도 불필요하다 — 나가는 의존이 0 이라 사이클에 참여할 수 없다.
 *   · 대가: shared 소비가 늘면 각 소비자에 "shared" 한 줄씩 추가해야 한다. 이는 비용이 아니라
 *     ARCHITECTURE §2.1 이 요구한 "shared 크기 상한 규율"의 가시화 장치로 쓴다.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {})
package com.moabom.shared;
