# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ESG T3 — an ESG (Environmental, Social, Governance) application built on the [T3 Stack](https://create.t3.gg/): Next.js, TypeScript, tRPC, Prisma, Tailwind CSS, and NextAuth.js.

## 언어 정책

- **기본 언어: 한국어**
- UI 텍스트(버튼, 레이블, 메시지, 오류 문구 등) 모두 한국어로 작성
- 코드 주석도 한국어로 작성
- Next.js `<html lang="ko">`, `next/font` 또는 `next-i18next` 설정 시 `ko` 로케일을 기본값으로 지정
- `next.config.js`에 `i18n.defaultLocale: 'ko'` 설정 (다국어 지원 시)

## Common Commands

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Run type checking
npm run typecheck

# Lint
npm run lint

# Format with Prettier
npm run format

# Run database migrations
npx prisma migrate dev

# Open Prisma Studio (DB GUI)
npx prisma studio

# Push schema changes without migration (dev only)
npx prisma db push

# Generate Prisma client after schema changes
npx prisma generate
```

## Architecture

This project follows the standard T3 Stack layout:

- **`src/server/api/`** — tRPC routers. Each domain (e.g., `esg`, `reports`, `companies`) gets its own router file, combined in `root.ts`.
- **`src/server/db.ts`** — Prisma client singleton.
- **`src/server/auth.ts`** — NextAuth.js configuration.
- **`src/pages/api/auth/[...nextauth].ts`** — NextAuth handler.
- **`src/pages/api/trpc/[trpc].ts`** — tRPC HTTP handler.
- **`src/utils/api.ts`** — tRPC client and React Query hooks (use `api.<router>.<procedure>.useQuery/useMutation`).
- **`prisma/schema.prisma`** — Database schema.

### Data Flow

Client components call tRPC procedures via `api.*` hooks → tRPC router validates input with Zod → Prisma queries the database → typed response returns to the client.

### Auth Pattern

Protect pages with `getServerSideProps` + `getServerAuthSession`, or protect tRPC procedures with the `protectedProcedure` helper in `src/server/api/trpc.ts`.

## Environment Variables

Managed via `.env`. Required variables are validated at build time in `src/env.mjs` (fail-fast on missing/wrong types). Copy `.env.example` → `.env` and fill in:
- `DATABASE_URL` — Prisma connection string
- `NEXTAUTH_SECRET` and `NEXTAUTH_URL`
- Any OAuth provider credentials
