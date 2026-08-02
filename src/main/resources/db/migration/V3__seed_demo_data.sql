-- Demo/seed data so there's something to weigh right after `docker compose up`, without
-- needing to register cadastros by hand first. Lets the scale-simulator (and manual testing)
-- exercise the reading/stabilization pipeline immediately, with enough variety (10 rows per
-- table) to exercise round-robin scale/truck/grain-type pairing in the simulator too.

INSERT INTO branch (code, name, city, state)
VALUES
    ('BR-01', 'Filial Central', 'Sorriso', 'MT'),
    ('BR-02', 'Filial Norte', 'Rondonopolis', 'MT'),
    ('BR-03', 'Filial Sul', 'Cascavel', 'PR'),
    ('BR-04', 'Filial Oeste', 'Dourados', 'MS'),
    ('BR-05', 'Filial Leste', 'Luis Eduardo Magalhaes', 'BA'),
    ('BR-06', 'Filial Cerrado', 'Rio Verde', 'GO'),
    ('BR-07', 'Filial Vale', 'Balsas', 'MA'),
    ('BR-08', 'Filial Planalto', 'Palmas', 'TO'),
    ('BR-09', 'Filial Norte Paulista', 'Ribeirao Preto', 'SP'),
    ('BR-10', 'Filial Fronteira', 'Passo Fundo', 'RS');

INSERT INTO grain_type (name, purchase_price_per_ton, available_quantity_tons)
VALUES
    ('Soja', 180.00, 300.000),
    ('Milho', 95.00, 600.000),
    ('Trigo', 150.00, 200.000),
    ('Sorgo', 110.00, 150.000),
    ('Arroz', 220.00, 180.000),
    ('Feijao', 400.00, 90.000),
    ('Aveia', 130.00, 120.000),
    ('Cevada', 140.00, 100.000),
    ('Centeio', 160.00, 80.000),
    ('Girassol', 250.00, 60.000);

INSERT INTO truck (plate, tare_weight_kg, cargo_capacity_tons)
VALUES
    ('ABC1D23', 9000.00, 35.000),
    ('XYZ9E88', 8500.00, 32.000),
    ('BRA1F23', 9200.00, 36.000),
    ('DEF2G34', 8800.00, 34.000),
    ('GHI3H45', 9100.00, 35.500),
    ('JKL4I56', 8600.00, 33.000),
    ('MNO5J67', 9300.00, 37.000),
    ('PQR6K78', 8900.00, 34.500),
    ('STU7L89', 9000.00, 35.000),
    ('VWX8M90', 8700.00, 33.500);

-- Fewer scales than trucks on purpose: each scale ends up with a pool of ~3-4 trucks to
-- rotate through (queue-like), instead of a 1:1 scale-to-truck pairing.
INSERT INTO scale (code, branch_id, location, active)
SELECT 'SCALE-01', id, 'Patio 1', TRUE FROM branch WHERE code = 'BR-01'
UNION ALL
SELECT 'SCALE-02', id, 'Patio 1', TRUE FROM branch WHERE code = 'BR-02'
UNION ALL
SELECT 'SCALE-03', id, 'Patio 1', TRUE FROM branch WHERE code = 'BR-03';