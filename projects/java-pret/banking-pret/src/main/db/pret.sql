-- Active: 1738011862925@@127.0.0.1@3306@s5_archlog_1_banking_pret
CREATE DATABASE IF NOT EXISTS `s5_archlog_1_banking_pret`;

CREATE TABLE IF NOT EXISTS `type_compte_prets` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `nom` VARCHAR(100) NOT NULL,
    `interet` DECIMAL(5, 4) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `compte_prets` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `type_compte_pret_id` INT NOT NULL,
    `montant` DECIMAL(15, 2) NOT NULL,
    `date_debut` DATETIME NOT NULL,
    `date_fin` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`type_compte_pret_id`) REFERENCES `type_compte_prets` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE
);

/* Vola tokony aloha par mois fa afaka miakatra */
CREATE TABLE IF NOT EXISTS `echeances` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `compte_id` INT NOT NULL,
    `montant` DECIMAL(15, 2) NOT NULL,
    `date_echeance` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`compte_id`) REFERENCES `compte_prets` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
);