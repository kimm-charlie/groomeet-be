ALTER TABLE `director_category`
    RENAME TO `director_service`;

ALTER TABLE `director_info_director_category_mapping`
    RENAME COLUMN `directing_category_id` TO `director_service_id`;

ALTER TABLE `director_info_director_category_mapping`
    RENAME TO `director_info_director_service_mapping`;