CREATE TABLE products
(
    id UUID NOT NULL,

    sku VARCHAR(100) NOT NULL,

    name VARCHAR(255) NOT NULL,

    description TEXT NOT NULL,

    price NUMERIC(19,2) NOT NULL,

    available_quantity INTEGER NOT NULL,

    reserved_quantity INTEGER NOT NULL DEFAULT 0,

    product_status VARCHAR(30) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_products PRIMARY KEY (id),

    CONSTRAINT uk_product_sku UNIQUE (sku)
);

CREATE INDEX idx_products_sku
    ON products(sku);

CREATE INDEX idx_products_product_status
    ON products(product_status);