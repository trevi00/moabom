import type { NextConfig } from 'next';

// D-02 단일 진입 오리진. 목적지는 모드마다 다르므로 env 로 외부화한다 (RESEARCH §P-5).
//   dev : localhost:8080 / localhost:8081
//   full: app:8080       / nginx:80        (compose 가 environment 로 주입)
// ⚠️ 이 두 값에 "클라이언트로 인라인되는 공개 접두"(Next 의 공개 env prefix)를 붙이지 말 것 —
//    붙이는 순간 서버 전용 목적지가 클라이언트 번들에 박혀 공개 저장소로 영구 유출된다 (T-1-10).
const BACKEND = process.env.BACKEND_ORIGIN ?? 'http://localhost:8080';
const MEDIA = process.env.MEDIA_ORIGIN ?? 'http://localhost:8081';

const nextConfig: NextConfig = {
  output: 'standalone', // D-06 FE 컨테이너 이미지 축소
  async rewrites() {
    return [
      { source: '/api/:path*', destination: `${BACKEND}/api/:path*` },
      { source: '/media/:path*', destination: `${MEDIA}/media/:path*` },
    ];
  },
};

export default nextConfig;
