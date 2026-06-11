-- Portfolio 테이블에 is_popular, popular_at 컬럼 추가
ALTER TABLE portfolio ADD COLUMN is_popular BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE portfolio ADD COLUMN popular_at DATETIME NULL;

-- 기존 popular_portfolio 데이터를 portfolio로 이관
UPDATE portfolio p
    INNER JOIN popular_portfolio pp ON p.id = pp.portfolio_id
SET p.is_popular = TRUE,
    p.popular_at = pp.created_at
WHERE pp.is_deleted = FALSE;

-- popular_portfolio 테이블 삭제
DROP TABLE IF EXISTS popular_portfolio;
