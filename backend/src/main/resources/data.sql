-- data.sql - kompletne przykładowe dane testowe

-- ============================================
-- Szablon 1: Alert dla złota przy wysokiej cenie
-- ============================================
INSERT INTO notification_templates (id, title, content) VALUES
(1, 'Gold Price Alert - High', 'Gold price has exceeded $2000! Current price: ${price} USD');

INSERT INTO rules (operator, operand, template_id) VALUES
('ITEM_IS', 'gold', 1),
('PRICE_GREATER', '2000', 1);

INSERT INTO recipients (email, template_id) VALUES
('trader1@bank.com', 1),
('risk@bank.com', 1);

-- ============================================
-- Szablon 2: Alert dla srebra przy niskiej cenie
-- ============================================
INSERT INTO notification_templates (id, title, content) VALUES
(2, 'Silver Price Alert - Low', 'Silver price has dropped below $25! Current price: ${price} USD');

INSERT INTO rules (operator, operand, template_id) VALUES
('ITEM_IS', 'silver', 2),
('PRICE_LESS', '25', 2);

INSERT INTO recipients (email, template_id) VALUES
('investor@bank.com', 2),
('analyst@bank.com', 2);

-- ============================================
-- Szablon 3: Alert dla platyny (każda cena)
-- ============================================
INSERT INTO notification_templates (id, title, content) VALUES
(3, 'Platinum Price Update', 'Platinum price changed to: ${price} USD');

INSERT INTO rules (operator, operand, template_id) VALUES
('ITEM_IS', 'platinum', 3);

INSERT INTO recipients (email, template_id) VALUES
('pm@bank.com', 3);

-- ============================================
-- Szablon 4: Alert dla metali NIE będących złotem
-- ============================================
INSERT INTO notification_templates (id, title, content) VALUES
(4, 'Non-Gold Metals Alert', 'A non-gold metal (${itemType}) price is interesting at ${price} USD');

INSERT INTO rules (operator, operand, template_id) VALUES
('ITEM_IS_NOT', 'gold', 4),
('PRICE_GREATER_OR_EQUAL', '100', 4);

INSERT INTO recipients (email, template_id) VALUES
('diversification@bank.com', 4);

-- ============================================
-- Szablon 5: Alert krytyczny dla złota (bardzo niska cena)
-- ============================================
INSERT INTO notification_templates (id, title, content) VALUES
(5, 'GOLD CRASH ALERT', 'GOLD PRICE CRASH! Below $1500! Current: ${price} USD');

INSERT INTO rules (operator, operand, template_id) VALUES
('ITEM_IS', 'gold', 5),
('PRICE_LESS_OR_EQUAL', '1500', 5);

INSERT INTO recipients (email, template_id) VALUES
('emergency@bank.com', 5),
('ceo@bank.com', 5),
('risk@bank.com', 5);

-- ============================================
-- Szablon 6: Alert dla złota (przedział cenowy)
-- ============================================
INSERT INTO notification_templates (id, title, content) VALUES
(6, 'Gold Mid-Range Alert', 'Gold price is in normal range: ${price} USD');

INSERT INTO rules (operator, operand, template_id) VALUES
('ITEM_IS', 'gold', 6),
('PRICE_GREATER_OR_EQUAL', '1800', 6),
('PRICE_LESS_OR_EQUAL', '2200', 6);

INSERT INTO recipients (email, template_id) VALUES
('monitoring@bank.com', 6);

-- ============================================
-- Szablon 7: Cena złota szaleje (przykład z zadania)
-- ============================================
INSERT INTO notification_templates (id, title, content) VALUES
(7, 'Cena złota szaleje', 'Cena złota przekroczyła wartość graniczną, ale nie przekroczyła wartości kosmicznej');

INSERT INTO rules (operator, operand, template_id) VALUES
('ITEM_IS', 'gold', 7),
('PRICE_GREATER_OR_EQUAL', '1350.00', 7),
('PRICE_LESS', '1400.00', 7);

INSERT INTO recipients (email, template_id) VALUES
('andrzej@gdziestam.com', 7),
('anna@gdziestam.com', 7);

-- ============================================
-- Szablon 8: Srebro niebezpiecznie tanie (przykład z zadania)
-- ============================================
INSERT INTO notification_templates (id, title, content) VALUES
(8, 'Srebro niebezpiecznie tanie', 'Uwaga! Srebro bardzo tanie!');

INSERT INTO rules (operator, operand, template_id) VALUES
('ITEM_IS', 'silver', 8),
('PRICE_GREATER_OR_EQUAL', '46.50', 8),
('PRICE_LESS', '60.00', 8);

INSERT INTO recipients (email, template_id) VALUES
('ktos@gdziestam.com', 8),
('nikt@gdziestam.com', 8);

-- ============================================
-- Szablon 9: Srebro rekordowo tanie od roku (przykład z zadania)
-- ============================================
INSERT INTO notification_templates (id, title, content) VALUES
(9, 'Srebro rekordowo tanie od roku', 'Srebro najtańsze od roku!');

INSERT INTO rules (operator, operand, template_id) VALUES
('ITEM_IS', 'silver', 9),
('PRICE_LESS', '59.99', 9);

INSERT INTO recipients (email, template_id) VALUES
('janina@gdziestam.com', 9),
('grazyna@gdziestam.com', 9);

-- ============================================
-- Reset sekwencji dla nowych rekordów
-- ============================================
ALTER TABLE notification_templates ALTER COLUMN id RESTART WITH 100;
ALTER TABLE rules ALTER COLUMN id RESTART WITH 100;
ALTER TABLE recipients ALTER COLUMN id RESTART WITH 100;