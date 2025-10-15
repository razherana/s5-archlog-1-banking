CREATE TABLE IF NOT EXISTS `compte_courants` (
    `id` INT NOT NULL AUTO_INCREMENT,
    -- Montant - par mois
    `taxe` DECIMAL(10, 2) NOT NULL,
    `user_id` INT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `transaction_courants` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `special_action` VARCHAR(255) NULL,
    `sender_id` INT NULL,
    `receiver_id` INT NULL,
    `montant` DECIMAL(15, 2) NOT NULL,
    `date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`sender_id`) REFERENCES `compte_courants` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
    FOREIGN KEY (`receiver_id`) REFERENCES `compte_courants` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE
);