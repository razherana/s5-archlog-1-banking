-- Active: 1738011862925@@127.0.0.1@3306@s5_archlog_1_banking
CREATE TABLE IF NOT EXISTS `users` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(255) NOT NULL,
	`email` VARCHAR(255) NOT NULL UNIQUE,
	`password` VARCHAR(255) NOT NULL,
	PRIMARY KEY(`id`)
);

CREATE TABLE IF NOT EXISTS `compte_courants` (
	`id` INT NOT NULL AUTO_INCREMENT,
	-- Montant - par mois
	`taxe` DECIMAL(10,2) NOT NULL,
	`user_id` INT NOT NULL,
	`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`),
	FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
	ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `transaction_courants` (
	`id` INT NOT NULL AUTO_INCREMENT,
  `special_action` VARCHAR(255) NULL,
	`sender_id` INT NULL,
	`receiver_id` INT NULL,
	`montant` DECIMAL(15,2) NOT NULL,
	`date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`),
	FOREIGN KEY (`sender_id`) REFERENCES `compte_courants`(`id`)
	ON UPDATE NO ACTION ON DELETE CASCADE,
	FOREIGN KEY (`receiver_id`) REFERENCES `compte_courants`(`id`)
	ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `compte_depots` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`taux` DECIMAL(5,4) NOT NULL,
	`user_id` INT NOT NULL,
	`date_ouverture` DATETIME NOT NULL,
	`date_echeance` DATETIME NOT NULL,
	PRIMARY KEY(`id`),
	FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
	ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `transaction_depots` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`compte_id` INT NOT NULL,
	`montant` DECIMAL(15,2) NOT NULL,
	`date` DATETIME DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`),
	FOREIGN KEY (`compte_id`) REFERENCES `compte_depots`(`id`)
	ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `compte_prets` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`user_id` INT NOT NULL,
	`interet` DECIMAL(5,4) NOT NULL,
	`montant` DECIMAL(15,2) NOT NULL,
	`date_debut` DATETIME NOT NULL,
	`date_fin` DATETIME NOT NULL,
	PRIMARY KEY(`id`),
	FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
	ON UPDATE NO ACTION ON DELETE CASCADE
);

/* Vola tokony aloha par mois fa afaka miakatra */
CREATE TABLE IF NOT EXISTS `echeances` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`compte_id` INT NOT NULL,
	`montant` DECIMAL(15,2) NOT NULL,
	PRIMARY KEY(`id`),
	FOREIGN KEY (`compte_id`) REFERENCES `compte_prets`(`id`)
	ON UPDATE NO ACTION ON DELETE NO ACTION
);
