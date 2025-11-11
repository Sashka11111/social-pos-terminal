DROP TABLE IF EXISTS Users;
CREATE TABLE Users (
    user_id VARCHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER' NOT NULL CHECK(role IN ('USER', 'ADMIN')),
    email VARCHAR(100) UNIQUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS MenuItems;
CREATE TABLE MenuItems (
    item_id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    price REAL NOT NULL,
    calories INTEGER,
    image BLOB,
    ingredients TEXT
);

DROP TABLE IF EXISTS Categories;
CREATE TABLE Categories (
    category_id VARCHAR(36) NOT NULL PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL
);

DROP TABLE IF EXISTS Orders;
CREATE TABLE Orders (
    order_id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36),
    order_date DATETIME NOT NULL,
    total_amount REAL NOT NULL,
    bonuses_earned REAL DEFAULT 0 NOT NULL,
    bonuses_used REAL DEFAULT 0 NOT NULL,
    status VARCHAR(50) NOT NULL CHECK(status IN ('PENDING', 'CONFIRMED', 'DELIVERED', 'CANCELLED')),
    notes VARCHAR(255),
    is_social BOOLEAN DEFAULT 0 NOT NULL,
    table_number INTEGER,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE SET NULL
);

DROP TABLE IF EXISTS Cart;
CREATE TABLE Cart (
    cart_id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    item_id VARCHAR(36) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal REAL NOT NULL,
    is_ordered BOOLEAN DEFAULT 0 NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (item_id) REFERENCES MenuItems(item_id)
);

DROP TABLE IF EXISTS ItemCategories;
CREATE TABLE ItemCategories (
    item_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NOT NULL ,
    PRIMARY KEY (item_id, category_id),
    FOREIGN KEY (item_id) REFERENCES MenuItems(item_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES Categories(category_id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS OrderCartItems;
CREATE TABLE OrderCartItems (
    order_id VARCHAR(36) NOT NULL,
    cart_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (order_id, cart_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (cart_id) REFERENCES Cart(cart_id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS LoyaltyCards;
CREATE TABLE LoyaltyCards (
    card_id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    card_number VARCHAR(20) UNIQUE NOT NULL,
    balance REAL NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS BonusTransactions;
CREATE TABLE BonusTransactions (
    transaction_id VARCHAR(36) NOT NULL PRIMARY KEY,
    card_id VARCHAR(36) NOT NULL,
    order_id VARCHAR(36),
    amount REAL NOT NULL,
    type VARCHAR(20) NOT NULL CHECK(type IN ('ACCRUAL', 'REDEMPTION', 'ADJUSTMENT')),
    transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255),
    FOREIGN KEY (card_id) REFERENCES LoyaltyCards(card_id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE SET NULL
);