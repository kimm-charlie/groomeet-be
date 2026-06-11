---
name: be-project-structure
description: >
  프로젝트 디렉토리 구조, 모듈 레이아웃, 빌드 명령어 안내.
  This skill should be used when creating new files, checking where files should go, or running build commands.
---

# Project Structure

## Overview
프로젝트 디렉토리 구조와 빌드 명령어를 안내한다.

## 핵심 규칙
- Source root: `src/main/java/com/motd/be`
- `module/`: 도메인별 코드 (역할 기반)
- `src/main/resources/db/migration/`: Flyway SQL 마이그레이션

- 상세 구조는 `references/project-structure-detail.md`를 확인한다.
