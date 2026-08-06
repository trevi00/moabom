import type { Metadata } from 'next';
import './globals.css';

// 구글 폰트 로더는 두지 않는다 (create-next-app 생성값의 Geist import 제거) —
// 빌드 시점에 폰트를 내려받아 컨테이너/CI 빌드가 네트워크에 의존하게 된다.
// 시스템 폰트로 간다 (D-05 "얇게").

export const metadata: Metadata = {
  title: '모아봄 (moabom)',
  description: '넷플릭스 클론 포트폴리오 — Phase 1 기능 스파이크',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
