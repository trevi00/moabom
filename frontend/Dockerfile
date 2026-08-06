# syntax=docker/dockerfile:1
FROM node:24-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

FROM node:24-alpine AS build
WORKDIR /app
# ⚠️ 실측(01-03, 반사실 2-arm 프로브): rewrites 의 destination 은 `next build` 시점에
#    .next/routes-manifest.json 으로 구워진다. 런타임 env(BACKEND_ORIGIN)는 무시된다
#    — RESEARCH §Assumptions A3 은 거짓으로 판명. 그래서 목적지를 build arg 로 받는다.
#    기본값 = full 프로필 목적지 (D-06: 이 이미지는 full 전용, dev 는 npm run dev).
#    compose 가 build.args 로 덮어쓸 수 있다.
ARG BACKEND_ORIGIN=http://app:8080
ARG MEDIA_ORIGIN=http://nginx:80
ENV BACKEND_ORIGIN=$BACKEND_ORIGIN
ENV MEDIA_ORIGIN=$MEDIA_ORIGIN
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

FROM node:24-alpine AS runtime
WORKDIR /app
# PORT/HOSTNAME 은 런타임에 유효하다(standalone server.js 가 직접 읽는다).
# BACKEND_ORIGIN/MEDIA_ORIGIN 은 여기서 주면 아무 효과가 없다 — 위 build 스테이지 참조.
ENV NODE_ENV=production
ENV PORT=3000
ENV HOSTNAME=0.0.0.0
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static
COPY --from=build /app/public ./public
EXPOSE 3000
CMD ["node", "server.js"]
