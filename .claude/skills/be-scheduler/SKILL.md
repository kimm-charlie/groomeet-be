---
name: be-scheduler
description: >
  스케줄러 구현 규칙. 레이어 구조, 위치, 네이밍, 중복 방지, 테스트를 포함한다.
  This skill should be used when creating or modifying scheduled tasks.
---

# Scheduler

## Overview
스케줄러 구현 시 레이어 구조, 네이밍, 중복 실행 방지 규칙을 정의한다.

## 핵심 규칙
- 레이어: Scheduler → Facade → Service → Repository
- 위치: `com.motd.be.common.scheduler`
- 클래스명: `{feature}Scheduler`, 메서드: 동사+목적어
- Profile 어노테이션, 외부 cron 설정, try-catch, 중복 방지 필수

- 상세 규칙은 `references/scheduler-detail.md`를 확인한다.
