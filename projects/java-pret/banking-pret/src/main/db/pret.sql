CREATE TABLE IF NOT EXISTS `compte_prets` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `interet` DECIMAL(5, 4) NOT NULL,
    `montant` DECIMAL(15, 2) NOT NULL,
    `date_debut` DATETIME NOT NULL,
    `date_fin` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE
);

/* Vola tokony aloha par mois fa afaka miakatra */
CREATE TABLE IF NOT EXISTS `echeances` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `compte_id` INT NOT NULL,
    `montant` DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`compte_id`) REFERENCES `compte_prets` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
);