
drop table if exists beer_order_line;
drop table if exists beer_order;
drop table if exists customer;

create table customer (
       id varchar(36) not null,
        created_date datetime(6),
        name varchar(255),
        update_date datetime(6),
        email varchar(255),
        version integer,
        primary key (id)
    ) engine=InnoDB;

CREATE TABLE `beer_order`
(
    id                 varchar(36) NOT NULL,
    created_date       datetime(6)  DEFAULT NULL,
    customer_ref       varchar(255) DEFAULT NULL,
    last_modified_date datetime(6)  DEFAULT NULL,
    version            bigint       DEFAULT NULL,
    customer_id        varchar(36)  DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FOREIGN KEY (customer_id) REFERENCES customer (id)
) ENGINE = InnoDB;

CREATE TABLE `beer_order_line`
(
    id                 varchar(36) NOT NULL,
    beer_id            varchar(36) DEFAULT NULL,
    created_date       datetime(6) DEFAULT NULL,
    last_modified_date datetime(6) DEFAULT NULL,
    order_quantity     int         DEFAULT NULL,
    quantity_allocated int         DEFAULT NULL,
    version            bigint      DEFAULT NULL,
    beer_order_id      varchar(36) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FOREIGN KEY (beer_order_id) REFERENCES beer_order (id),
    CONSTRAINT FOREIGN KEY (beer_id) REFERENCES beer (id)
) ENGINE = InnoDB;
