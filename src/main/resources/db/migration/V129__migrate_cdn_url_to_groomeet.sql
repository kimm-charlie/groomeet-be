-- CDN 도메인 변경: file.manotd.com -> file.groomeet.com

-- BaseFile 상속 테이블 (cdn_url 컬럼)
UPDATE service_request_file     SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';
UPDATE service_estimate_file    SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';
UPDATE review_file              SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';
UPDATE report_file              SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';
UPDATE profile_file             SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';
UPDATE portfolio_file           SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';
UPDATE chat_file                SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';
UPDATE business_registration_file SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';
UPDATE director_service_file    SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';
UPDATE director_profile_detail_file SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';
UPDATE custom_banner_item_file  SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';
UPDATE banner_file              SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';
UPDATE popup_file               SET cdn_url = REPLACE(cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_url LIKE '%file.manotd.com%';

-- 직접 CDN URL 컬럼을 가진 테이블
UPDATE member           SET cdn_profile_image_url   = REPLACE(cdn_profile_image_url,   'file.manotd.com', 'file.groomeet.com') WHERE cdn_profile_image_url   LIKE '%file.manotd.com%';
UPDATE story            SET thumbnail_image_cdn_url = REPLACE(thumbnail_image_cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE thumbnail_image_cdn_url LIKE '%file.manotd.com%';
UPDATE story            SET content_image_cdn_url   = REPLACE(content_image_cdn_url,   'file.manotd.com', 'file.groomeet.com') WHERE content_image_cdn_url   LIKE '%file.manotd.com%';
UPDATE banner           SET content_image_cdn_url   = REPLACE(content_image_cdn_url,   'file.manotd.com', 'file.groomeet.com') WHERE content_image_cdn_url   LIKE '%file.manotd.com%';
UPDATE banner           SET thumbnail_image_cdn_url = REPLACE(thumbnail_image_cdn_url, 'file.manotd.com', 'file.groomeet.com') WHERE thumbnail_image_cdn_url LIKE '%file.manotd.com%';
UPDATE popup            SET cdn_thumbnail_image_url = REPLACE(cdn_thumbnail_image_url, 'file.manotd.com', 'file.groomeet.com') WHERE cdn_thumbnail_image_url LIKE '%file.manotd.com%';
UPDATE director_service SET cdn_image_url           = REPLACE(cdn_image_url,           'file.manotd.com', 'file.groomeet.com') WHERE cdn_image_url           LIKE '%file.manotd.com%';
UPDATE director_service SET popular_cdn_image_url   = REPLACE(popular_cdn_image_url,   'file.manotd.com', 'file.groomeet.com') WHERE popular_cdn_image_url   LIKE '%file.manotd.com%';
UPDATE custom_banner_item SET cdn_image_url         = REPLACE(cdn_image_url,           'file.manotd.com', 'file.groomeet.com') WHERE cdn_image_url           LIKE '%file.manotd.com%';
