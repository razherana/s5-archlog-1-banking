-- Active: 1738011862925@@127.0.0.1@3306@s5_archlog_1_banking_depot
CREATE DATABASE s5_archlog_1_banking_depot;

CREATE TABLE IF NOT EXISTS `type_compte_depots` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `nom` VARCHAR(255) NOT NULL,
    `taux_interet` DECIMAL(5, 4) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `compte_depots` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `type_compte_depot_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `date_ouverture` DATETIME NOT NULL,
    `date_echeance` DATETIME NOT NULL,
    `montant` DECIMAL(15, 2) NOT NULL,
    `est_retire` INT NOT NULL DEFAULT 0, -- 0: non, 1: oui
    `date_retire` DATETIME NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`type_compte_depot_id`) REFERENCES `type_compte_depots` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE
);