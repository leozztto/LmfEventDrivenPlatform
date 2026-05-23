CREATE TABLE payment
(
    id UUID PRIMARY KEY,

    order_id UUID NOT NULL,

    amount NUMERIC(19,2) NOT NULL,

    payment_method VARCHAR(50) NOT NULL,

    installments INTEGER,

    status VARCHAR(50) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);