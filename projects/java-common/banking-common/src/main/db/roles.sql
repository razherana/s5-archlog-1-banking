-- Active: 1738011862925@@127.0.0.1@3306@s5_archlog_1_banking_interface

-- ActionRoles
-- Full Authority for Admin Role (role = 3)
INSERT INTO `action_roles` (`role`, `table_name`, `action`) VALUES
(3, 'users', 'READ'),
(3, 'users', 'CREATE'),
(3, 'users', 'UPDATE'),
(3, 'users', 'DELETE'),

(3, 'compte_courants', 'READ'),
(3, 'compte_courants', 'CREATE'),
(3, 'compte_courants', 'UPDATE'),
(3, 'compte_courants', 'DELETE'),

(3, 'transaction_courants', 'READ'),
(3, 'transaction_courants', 'CREATE'),
(3, 'transaction_courants', 'UPDATE'),
(3, 'transaction_courants', 'DELETE'),

(3, 'type_compte_depots', 'READ'),
(3, 'type_compte_depots', 'CREATE'),
(3, 'type_compte_depots', 'UPDATE'),
(3, 'type_compte_depots', 'DELETE'),

(3, 'compte_depots', 'READ'),
(3, 'compte_depots', 'CREATE'),
(3, 'compte_depots', 'UPDATE'),
(3, 'compte_depots', 'DELETE'),

(3, 'compte_prets', 'READ'),
(3, 'compte_prets', 'CREATE'),
(3, 'compte_prets', 'UPDATE'),
(3, 'compte_prets', 'DELETE'),

(3, 'type_compte_prets', 'READ'),
(3, 'type_compte_prets', 'CREATE'),
(3, 'type_compte_prets', 'UPDATE'),
(3, 'type_compte_prets', 'DELETE'),

(3, 'echeances', 'READ'),
(3, 'echeances', 'CREATE'),
(3, 'echeances', 'UPDATE'),
(3, 'echeances', 'DELETE')
;

-- Inserter (role = 2)
INSERT INTO `action_roles` (`role`, `table_name`, `action`) VALUES
(2, 'users', 'READ'),
(2, 'users', 'CREATE'),

(2, 'compte_courants', 'READ'),
(2, 'compte_courants', 'CREATE'),

(2, 'transaction_courants', 'READ'),
(2, 'transaction_courants', 'CREATE'),

(2, 'type_compte_depots', 'READ'),
(2, 'type_compte_depots', 'CREATE'),

(2, 'compte_depots', 'READ'),
(2, 'compte_depots', 'CREATE'),

(2, 'type_compte_prets', 'READ'),
(2, 'type_compte_prets', 'CREATE'),

(2, 'compte_prets', 'READ'),
(2, 'compte_prets', 'CREATE'),

(2, 'echeances', 'READ'),
(2, 'echeances', 'CREATE')
;

-- Viewer (role = 1)
INSERT INTO `action_roles` (`role`, `table_name`, `action`) VALUES
(1, 'users', 'READ'),

(1, 'compte_courants', 'READ'),

(1, 'transaction_courants', 'READ'),

(1, 'type_compte_depots', 'READ'),

(1, 'compte_depots', 'READ'),

(1, 'type_compte_prets', 'READ'),

(1, 'compte_prets', 'READ'),

(1, 'echeances', 'READ')
;