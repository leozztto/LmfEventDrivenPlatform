-- Usuário administrador de conveniência para o ambiente local / e2e.
-- Credenciais: admin / admin123  (hash BCrypt custo 10, gerado offline).
-- Idempotente: não recria se já existir.
INSERT INTO users (id, username, email, password_hash, enabled, created_at)
VALUES ('00000000-0000-0000-0000-000000000001',
        'admin',
        'admin@lmf.local',
        '$2a$10$7O3Q5Ipu.YqHAWwMQAC.teBstTKbWMngQrW9isMgIUOKJJlC28/JG',
        TRUE,
        now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role)
VALUES ('00000000-0000-0000-0000-000000000001', 'ROLE_ADMIN'),
       ('00000000-0000-0000-0000-000000000001', 'ROLE_USER')
ON CONFLICT (user_id, role) DO NOTHING;
