-- Clear all test data from banking deposit database
-- Run this script to clean up after testing

-- Clear deposit accounts
DELETE FROM `compte_depots`;

DELETE FROM `type_compte_depots`;

ALTER TABLE `compte_depots` AUTO_INCREMENT = 1;

ALTER TABLE `type_compte_depots` AUTO_INCREMENT = 1;