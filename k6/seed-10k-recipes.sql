-- ===================================================================
-- 더미 데이터 삽입 스크립트
-- 목적: 레시피 10,000개 + 재료 100개 + 레시피-재료 연결 50,000개
--       → findTopByLikeCount / findTopByMatchCount 풀스캔 병목을 눈에 띄게 만들기
--
-- 대상 DB: MySQL (naenggu)
-- 실행 시간: 약 2~5분
--
-- [실행 방법]
--   mysql -h 127.0.0.1 -P 3306 -u root -p naenggu < k6/seed-10k-recipes.sql
--   또는 MySQL Workbench에서 전체 선택 후 실행
--
-- [삭제 방법]
--   DELETE FROM recipe_ingredient WHERE recipe_id IN (SELECT id FROM recipe WHERE title LIKE '더미레시피_%');
--   DELETE FROM recipe_with_text WHERE id IN (SELECT id FROM recipe WHERE title LIKE '더미레시피_%');
--   DELETE FROM recipe WHERE title LIKE '더미레시피_%';
--   DELETE FROM ingredient WHERE name LIKE '더미재료_%';
-- ===================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS seed_dummy_data$$

CREATE PROCEDURE seed_dummy_data()
BEGIN
    DECLARE i          INT DEFAULT 1;
    DECLARE j          INT DEFAULT 1;
    DECLARE recipe_id  BIGINT;
    DECLARE ing_id     BIGINT;
    DECLARE ing_count  INT DEFAULT 0;

    -- ---------------------------------------------------------------
    -- Step 1. 재료 100개 삽입
    --   ingredient.name 에 unique 제약이 있으므로 INSERT IGNORE 사용
    -- ---------------------------------------------------------------
    SET i = 1;
    WHILE i <= 100 DO
        INSERT IGNORE INTO ingredient (name)
        VALUES (CONCAT('더미재료_', LPAD(i, 4, '0')));
        SET i = i + 1;
    END WHILE;

    -- ---------------------------------------------------------------
    -- Step 2. 방금 삽입한 재료 ID를 임시 테이블에 수집
    --   AUTO_INCREMENT 로 인해 ID가 불연속할 수 있으므로 임시 테이블로 관리
    -- ---------------------------------------------------------------
    DROP TEMPORARY TABLE IF EXISTS tmp_ing_ids;
    CREATE TEMPORARY TABLE tmp_ing_ids (
        rn     INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        ing_id BIGINT NOT NULL
    );
    INSERT INTO tmp_ing_ids (ing_id)
    SELECT id FROM ingredient
    WHERE name LIKE '더미재료_%'
    ORDER BY id;

    SELECT COUNT(*) INTO ing_count FROM tmp_ing_ids;

    -- ---------------------------------------------------------------
    -- Step 3. 레시피 10,000개 + recipe_ingredient 연결
    -- ---------------------------------------------------------------
    SET i = 1;
    WHILE i <= 10000 DO

        -- 3-a. recipe (부모 테이블) 삽입
        INSERT INTO recipe (
            title,
            servings,
            difficulty,
            cooking_time,
            description,
            recipe_image_id,
            created_at,
            recipe_type
        ) VALUES (
            CONCAT('더미레시피_', LPAD(i, 5, '0')),
            FLOOR(1 + RAND() * 4),                              -- 1~4인분
            ELT(1 + FLOOR(RAND() * 3), 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'),
            FLOOR(10 + RAND() * 50),                            -- 10~59분
            CONCAT('더미 레시피 설명입니다. (', i, ')'),
            NULL,                                               -- 이미지 없음
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 730) DAY), -- 최근 2년 내 랜덤
            'TEXT'
        );

        SET recipe_id = LAST_INSERT_ID();

        -- 3-b. recipe_with_text (자식 테이블) 삽입 — id 컬럼만 존재
        INSERT INTO recipe_with_text (id) VALUES (recipe_id);

        -- 3-c. recipe_ingredient: 레시피당 재료 5개 연결
        --   (재료 100개를 순환하며 할당)
        SET j = 1;
        WHILE j <= 5 DO
            SELECT ing_id INTO ing_id
            FROM tmp_ing_ids
            WHERE rn = ((i + j - 2) MOD ing_count) + 1;

            -- unique 제약 위반 방어: INSERT IGNORE
            INSERT IGNORE INTO recipe_ingredient (recipe_id, ingredient_id, amount)
            VALUES (
                recipe_id,
                ing_id,
                CONCAT(FLOOR(1 + RAND() * 10), '개')
            );

            SET j = j + 1;
        END WHILE;

        SET i = i + 1;
    END WHILE;

    DROP TEMPORARY TABLE IF EXISTS tmp_ing_ids;

    SELECT '완료' AS status,
           (SELECT COUNT(*) FROM recipe      WHERE title LIKE '더미레시피_%') AS recipes_inserted,
           (SELECT COUNT(*) FROM ingredient  WHERE name  LIKE '더미재료_%')   AS ingredients_inserted,
           (SELECT COUNT(*) FROM recipe_ingredient ri
                JOIN recipe r ON ri.recipe_id = r.id
                WHERE r.title LIKE '더미레시피_%')                              AS links_inserted;
END$$

DELIMITER ;

-- 실행
CALL seed_dummy_data();
DROP PROCEDURE IF EXISTS seed_dummy_data;
