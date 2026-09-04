-- Registros de exemplo para demonstração e para os cenários de E2E que validam FRAUD_REJECTED.
INSERT INTO fraud_blocklist (id, customer_email, reason, created_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'blocked.customer@example.com',
        'Seed de demonstração — chargeback recorrente', now());

INSERT INTO fraud_blocklist (id, customer_id, reason, created_at)
VALUES ('22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333',
        'Seed de demonstração — conta associada a fraude confirmada', now());
