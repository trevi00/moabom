import HealthProbe from './HealthProbe';

export default function Home() {
  return (
    <main style={{ fontFamily: 'system-ui, sans-serif', padding: '2rem', lineHeight: 1.7 }}>
      <h1>모아봄 (moabom)</h1>
      <p>
        단일 진입 오리진 확인 — 브라우저는 <code>:3000</code> 만 봅니다.
        <code>/api/*</code> 는 Next rewrites 가 백엔드로, <code>/media/*</code> 는 nginx 로 프록시합니다 (D-02).
      </p>
      <h2>GET /api/health</h2>
      <HealthProbe />
      <p style={{ marginTop: '2rem', fontSize: '0.9rem', opacity: 0.7 }}>
        Phase 1 기능 스파이크 페이지입니다. 행(row) UI 는 Phase 5 의 목업-우선 규율 대상입니다.
      </p>
    </main>
  );
}
