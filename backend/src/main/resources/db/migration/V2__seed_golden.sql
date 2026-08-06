-- V2__seed_golden.sql
-- D-13: 시드는 본선 V 마이그레이션이다 (프로필 분기 없음). 모든 환경이 데모 성격인 포트폴리오라
--       "시드 없는 환경"이 존재하지 않는다 -> SC3 "Flyway 만 돌려도 재현" 이 문자 그대로 성립한다.
--
-- 두 종류의 행이 섞여 있다:
--   (A) 한글 골든 5건 — MySQL 한국어 검색 함정(PITFALLS Pitfall 10) 검증용 메타데이터 전용.
--       재생 자산과 아트워크가 없고 앞으로도 만들지 않는다. license 값이 그 사실을 스스로 말한다.
--   (B) Blender 오픈무비 5건 — 실 재생 자산의 메타 행. CC BY 3.0 이라 attribution 이 라이선스 의무다.
--       에셋 경로 컬럼은 P1 에 없다 — P2 배치 스크립트가 형식을 확정한 뒤 그 phase 가 추가한다 (D-12).

INSERT INTO genre (code, name_ko) VALUES
  ('sf',        'SF'),
  ('animation', '애니메이션'),
  ('drama',     '드라마'),
  ('action',    '액션'),
  ('thriller',  '스릴러');

-- (A) 한글 골든 — 검색 파서 픽스처
INSERT INTO title (title_ko, title_en, synopsis, release_year, runtime_sec, attribution, license) VALUES
  ('인터스텔라', 'Interstellar',  '검색 파서 테스트용 한글 골든 픽스처입니다.', 2014, 10140,
   '검색 파서 테스트 픽스처 — 재생 자산 및 아트워크 없음', 'FIXTURE-METADATA-ONLY'),
  ('신세계',     'New World',     '검색 파서 테스트용 한글 골든 픽스처입니다.', 2013,  7740,
   '검색 파서 테스트 픽스처 — 재생 자산 및 아트워크 없음', 'FIXTURE-METADATA-ONLY'),
  ('기생충',     'Parasite',      '검색 파서 테스트용 한글 골든 픽스처입니다.', 2019,  7920,
   '검색 파서 테스트 픽스처 — 재생 자산 및 아트워크 없음', 'FIXTURE-METADATA-ONLY'),
  ('부산행',     'Train to Busan','검색 파서 테스트용 한글 골든 픽스처입니다.', 2016,  6960,
   '검색 파서 테스트 픽스처 — 재생 자산 및 아트워크 없음', 'FIXTURE-METADATA-ONLY'),
  ('올드보이',   'Oldboy',        '검색 파서 테스트용 한글 골든 픽스처입니다.', 2003,  7200,
   '검색 파서 테스트 픽스처 — 재생 자산 및 아트워크 없음', 'FIXTURE-METADATA-ONLY');

-- (B) Blender 오픈무비 — 실 재생 자산의 메타 행 (CC BY 3.0)
INSERT INTO title (title_ko, title_en, synopsis, release_year, runtime_sec, attribution, license) VALUES
  ('빅 벅 버니',         'Big Buck Bunny',    '거대한 토끼가 숲의 불량배들에게 복수하는 단편 애니메이션.', 2008, 596,
   '© Blender Foundation | blender.org', 'CC-BY-3.0'),
  ('신텔',               'Sintel',            '잃어버린 새끼 용을 찾아 떠나는 소녀의 여정.',               2010, 888,
   '© Blender Foundation | blender.org', 'CC-BY-3.0'),
  ('티어스 오브 스틸',    'Tears of Steel',    '로봇과 인간의 갈등을 다룬 SF 단편.',                       2012, 734,
   '© Blender Foundation | blender.org', 'CC-BY-3.0'),
  ('엘리펀츠 드림',       'Elephants Dream',   '기묘한 기계 세계를 걷는 두 사람의 이야기.',                2006, 654,
   '© Blender Foundation | blender.org', 'CC-BY-3.0'),
  ('코스모스 런드로매트', 'Cosmos Laundromat', '섬에서 만난 낯선 이가 건넨 선택지.',                       2015, 738,
   '© Blender Foundation | blender.org', 'CC-BY-3.0');

-- 장르 연결 (BROW/RECO 가 쓸 최소 관계)
INSERT INTO title_genre (title_id, genre_id)
SELECT t.id, g.id FROM title t JOIN genre g ON g.code = 'sf'
 WHERE t.title_en IN ('Interstellar', 'Tears of Steel');

INSERT INTO title_genre (title_id, genre_id)
SELECT t.id, g.id FROM title t JOIN genre g ON g.code = 'animation'
 WHERE t.title_en IN ('Big Buck Bunny', 'Sintel', 'Elephants Dream', 'Cosmos Laundromat');

INSERT INTO title_genre (title_id, genre_id)
SELECT t.id, g.id FROM title t JOIN genre g ON g.code = 'thriller'
 WHERE t.title_en IN ('New World', 'Oldboy', 'Parasite');

INSERT INTO title_genre (title_id, genre_id)
SELECT t.id, g.id FROM title t JOIN genre g ON g.code = 'action'
 WHERE t.title_en IN ('Train to Busan');
