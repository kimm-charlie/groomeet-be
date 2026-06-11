ALTER TABLE `popup`
    ADD COLUMN `cdn_thumbnail_image_url` VARCHAR(1000) NOT NULL,
  ADD COLUMN `cdn_content_image_url` VARCHAR(1000) NULL;