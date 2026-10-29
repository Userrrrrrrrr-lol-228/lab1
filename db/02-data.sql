INSERT INTO users (name, username, password, role, enabled) VALUES
    ('Иван Петров',   'ivan',    '$2a$10$b/IeF0sTIbnKXazrzfMdOOranHVo3lJWT9m9e1uk6DRh3NaPiKNU.', 'CUSTOMER', TRUE),
    ('Анна Смирнова', 'anna',    '$2a$10$LCUIQ4LVUG..Ixda/9SS..UtrIZiiGQ9NTf1us6bw410xqcWSFXFy', 'CUSTOMER', TRUE),
    ('Лаборант',      'lab',     '$2a$10$hMuFRszk5GdotQGpDQ/tvuXVSxfp8P6QpXGHr2H3C0R8MDC8q2Wq2', 'LAB',      TRUE),
    ('Заблокирован',  'blocked', '$2a$10$bXlkJQEvUsnaZtAXnBt2TO57mD0dKCbWTRN2GeUfO.U.Wap/ii00m', 'CUSTOMER', FALSE);

INSERT INTO analysis_methods (name, allowed_material, min_value, max_value, max_spread) VALUES
    ('pH', 'water', 0, 14, 0.2),
    ('Moisture', 'soil', 0, 100, 2);

INSERT INTO samples (material, customer_id, received_date, status) VALUES
    ('water', 1, CURRENT_DATE, 'NEW'),
    ('soil',  2, CURRENT_DATE, 'NEW');

INSERT INTO analyses (sample_id, method_id, status) VALUES
    (1, 1, 'NEW'),
    (2, 2, 'NEW');
