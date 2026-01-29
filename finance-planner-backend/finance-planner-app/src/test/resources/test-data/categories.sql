-- Test seed data for categories (H2 compatible)
-- Expense parent categories
INSERT INTO category (id, name, parent_id, type, icon, color, sort_order, is_system, created_at) VALUES
(1, '餐饮', NULL, 2, 'food', '#FF6B6B', 1, TRUE, CURRENT_TIMESTAMP),
(2, '交通', NULL, 2, 'transport', '#4ECDC4', 2, TRUE, CURRENT_TIMESTAMP),
(3, '居住', NULL, 2, 'house', '#45B7D1', 3, TRUE, CURRENT_TIMESTAMP),
(4, '购物', NULL, 2, 'shopping', '#96CEB4', 4, TRUE, CURRENT_TIMESTAMP),
(5, '娱乐', NULL, 2, 'entertainment', '#FFEAA7', 5, TRUE, CURRENT_TIMESTAMP);

-- Income parent categories
INSERT INTO category (id, name, parent_id, type, icon, color, sort_order, is_system, created_at) VALUES
(10, '工资', NULL, 1, 'salary', '#2ECC71', 1, TRUE, CURRENT_TIMESTAMP),
(11, '副业', NULL, 1, 'side-job', '#3498DB', 2, TRUE, CURRENT_TIMESTAMP),
(12, '投资收益', NULL, 1, 'investment', '#E74C3C', 3, TRUE, CURRENT_TIMESTAMP);

-- Expense subcategories
INSERT INTO category (id, name, parent_id, type, icon, color, sort_order, is_system, created_at) VALUES
(20, '早餐', 1, 2, 'breakfast', '#FF6B6B', 1, TRUE, CURRENT_TIMESTAMP),
(21, '午餐', 1, 2, 'lunch', '#FF6B6B', 2, TRUE, CURRENT_TIMESTAMP),
(22, '公交地铁', 2, 2, 'bus', '#4ECDC4', 1, TRUE, CURRENT_TIMESTAMP);
