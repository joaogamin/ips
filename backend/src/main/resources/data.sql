-- DML: Dados iniciais — Parâmetros de precificação

INSERT INTO cost_parameters (parametro, valor, descricao) VALUES
    ('taxa_devcoin',           3.7259000231, 'Taxa de conversão Devcoin para Real (1 D$ = R$)'),
    ('taxa_importacao',        0.1731000000, 'Taxa de importação sobre o valor convertido (17,31%)'),
    ('icms_sp',                0.1187000000, 'Alíquota ICMS São Paulo (11,87%)'),
    ('icms_go',                0.0701000000, 'Alíquota ICMS Goiás (7,01%)'),
    ('icms_rs',                0.2500000000, 'Alíquota ICMS Rio Grande do Sul (25,00%)'),
    ('icms_am',                0.0876000000, 'Alíquota ICMS Amazonas (8,76%)'),
    ('icms_ba',                0.1455000000, 'Alíquota ICMS Bahia (14,55%)'),
    ('margem_lucro',           0.2500000000, 'Margem de lucro sobre o custo final acumulado (25%)'),
    ('reinvestimento_ti',      0.3333333333, 'Percentual do lucro bruto destinado ao reinvestimento em TI (1/3)'),
    ('frete_faixa2_fixo',      28.5500000000, 'Frete fixo para compras entre R$ 3.500,00 e R$ 4.999,99'),
    ('frete_faixa3_fixo',      36.2100000000, 'Frete fixo para compras entre R$ 1.500,00 e R$ 3.499,99'),
    ('frete_faixa3_percentual', 0.0075000000, 'Percentual adicional de frete na faixa R$ 1.500,00 a R$ 3.499,99 (0,75%)'),
    ('frete_faixa4_fixo',      47.8300000000, 'Frete fixo para compras abaixo de R$ 1.500,00'),
    ('frete_faixa4_percentual', 0.0099000000, 'Percentual adicional de frete para compras abaixo de R$ 1.500,00 (0,99%)'),
    ('icms_na_base_margem',    0.0000000000, 'Modo de cálculo: 0 = margem antes do ICMS (Modo A, padrão); 1 = ICMS na base da margem (Modo B, literal)')
ON CONFLICT (parametro) DO NOTHING;
