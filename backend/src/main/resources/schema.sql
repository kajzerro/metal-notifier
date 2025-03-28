CREATE TYPE operator_enum AS ENUM (
    'ITEM_IS',
    'ITEM_IS_NOT',
    'PRICE_IS_EQUAL_TO',
    'PRICE_IS_GREATER_THAN',
    'PRICE_IS_GREATER_THAN_OR_EQUAL_TO',
    'PRICE_IS_LESS_THAN',
    'PRICE_IS_LESS_THAN_OR_EQUAL_TO'
);

CREATE TABLE notification_template (
                                       id BIGSERIAL PRIMARY KEY,
                                       title VARCHAR(255) NOT NULL,
                                       content TEXT
);

CREATE TABLE recipient (
                           id BIGSERIAL PRIMARY KEY,
                           email VARCHAR(255),
                           template_id BIGINT,
                           CONSTRAINT fk_recipient_template
                               FOREIGN KEY (template_id)
                                   REFERENCES notification_template (id)
                                   ON DELETE CASCADE
);

CREATE TABLE rule (
                      id BIGSERIAL PRIMARY KEY,
                      operator operator_enum NOT NULL,
                      operand VARCHAR(255),
                      template_id BIGINT,
                      CONSTRAINT fk_rule_template
                          FOREIGN KEY (template_id)
                              REFERENCES notification_template (id)
                              ON DELETE CASCADE
);
