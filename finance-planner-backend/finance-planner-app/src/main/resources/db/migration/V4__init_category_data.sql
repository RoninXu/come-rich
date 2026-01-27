-- Expense categories (type=2)
INSERT INTO category (id, name, type, icon, color, sort_order, is_system) VALUES
(1, '餐饮', 2, 'food', '#FF6B6B', 1, TRUE),
(2, '交通', 2, 'car', '#4ECDC4', 2, TRUE),
(3, '居住', 2, 'home', '#95E1D3', 3, TRUE),
(4, '购物', 2, 'shopping', '#F38181', 4, TRUE),
(5, '娱乐', 2, 'game', '#AA96DA', 5, TRUE),
(6, '学习', 2, 'book', '#FCBAD3', 6, TRUE),
(7, '医疗', 2, 'health', '#A8D8EA', 7, TRUE),
(8, '人情', 2, 'gift', '#FFAAA7', 8, TRUE),
(9, '其他支出', 2, 'other', '#78909C', 99, TRUE);

-- Income categories (type=1)
INSERT INTO category (id, name, type, icon, color, sort_order, is_system) VALUES
(10, '工资', 1, 'salary', '#66BB6A', 1, TRUE),
(11, '副业', 1, 'side-job', '#42A5F5', 2, TRUE),
(12, '投资收益', 1, 'invest', '#FFA726', 3, TRUE),
(13, '其他收入', 1, 'other', '#78909C', 99, TRUE);

-- Expense sub-categories: 餐饮
INSERT INTO category (name, parent_id, type, icon, sort_order, is_system) VALUES
('早餐', 1, 2, 'breakfast', 1, TRUE),
('午餐', 1, 2, 'lunch', 2, TRUE),
('晚餐', 1, 2, 'dinner', 3, TRUE),
('外卖', 1, 2, 'takeout', 4, TRUE),
('零食饮料', 1, 2, 'snack', 5, TRUE);

-- Expense sub-categories: 交通
INSERT INTO category (name, parent_id, type, icon, sort_order, is_system) VALUES
('公交地铁', 2, 2, 'subway', 1, TRUE),
('打车', 2, 2, 'taxi', 2, TRUE),
('加油', 2, 2, 'fuel', 3, TRUE),
('停车', 2, 2, 'parking', 4, TRUE);

-- Expense sub-categories: 居住
INSERT INTO category (name, parent_id, type, icon, sort_order, is_system) VALUES
('房租', 3, 2, 'rent', 1, TRUE),
('水电煤', 3, 2, 'utility', 2, TRUE),
('物业费', 3, 2, 'property', 3, TRUE),
('网费', 3, 2, 'network', 4, TRUE);

-- Expense sub-categories: 购物
INSERT INTO category (name, parent_id, type, icon, sort_order, is_system) VALUES
('日用品', 4, 2, 'daily', 1, TRUE),
('服饰', 4, 2, 'clothes', 2, TRUE),
('数码', 4, 2, 'digital', 3, TRUE),
('美妆', 4, 2, 'beauty', 4, TRUE);

-- Expense sub-categories: 娱乐
INSERT INTO category (name, parent_id, type, icon, sort_order, is_system) VALUES
('电影', 5, 2, 'movie', 1, TRUE),
('游戏', 5, 2, 'game', 2, TRUE),
('旅游', 5, 2, 'travel', 3, TRUE),
('运动', 5, 2, 'sport', 4, TRUE);

-- Reset sequence
SELECT setval('category_id_seq', (SELECT MAX(id) FROM category));
