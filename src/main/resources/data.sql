INSERT INTO ingredient (id, name) VALUES
(10, '토마토'),
(11, '달걀'),
(12, '양파')
ON DUPLICATE KEY UPDATE name = VALUES(name);

