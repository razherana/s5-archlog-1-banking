-- Active: 1738011862925@@127.0.0.1@3306@s5_archlog_1_banking_courant
CREATE TABLE IF NOT EXISTS `compte_courants` (
    `id` INT NOT NULL AUTO_INCREMENT,
    -- Montant - par mois
    `taxe` DECIMAL(10, 2) NOT NULL,
    `user_id` INT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `transaction_courants`;
CREATE TABLE IF NOT EXISTS `transaction_courants` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `special_action` VARCHAR(255) NULL,
    `sender_id` INT NULL,
    `receiver_id` INT NULL,
    `montant` DECIMAL(15, 2) NOT NULL,
    `json_data` TEXT NULL,
    `devise` VARCHAR(10) NULL,
    `validation_date` DATETIME NULL,
    `date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`sender_id`) REFERENCES `compte_courants` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
    FOREIGN KEY (`receiver_id`) REFERENCES `compte_courants` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE
);

DROP TABLE IF EXISTS `configuration_frais`;
CREATE TABLE IF NOT EXISTS `configuration_frais` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `type_compte` VARCHAR(50) NOT NULL, -- Eg: "courant", "depot", "pret"
    `frais_montant` DECIMAL(10, 2) NULL,
    `frais_pourcentage` DECIMAL(5, 2) NULL,
    `montant_minimum` DECIMAL(15, 2) NOT NULL,
    `montant_maximum` DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (`id`)
);

-- Raha misy frais montant et frais pourcentage dia sommena
DROP TABLE IF EXISTS `transaction_etats`;
CREATE TABLE IF NOT EXISTS `transaction_etats` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `transaction_id` INT NOT NULL,
    `etat` INT NOT NULL, -- 0: en attente, ... , 10: validé,
    `user_admin_id` INT NOT NULL,
    `date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`transaction_id`) REFERENCES `transaction_courants` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE
);