-- Sample data for testing the banking depot module
-- This data is used by the test scripts for verification
-- Prerequisites: Users 1, 2, 3 must exist in the Java current account service

-- Clear existing data first
DELETE FROM `compte_depots`;
DELETE FROM `type_compte_depots`;

-- Reset auto-increment
ALTER TABLE `compte_depots` AUTO_INCREMENT = 1;
ALTER TABLE `type_compte_depots` AUTO_INCREMENT = 1;

-- Insert standard account types for testing
INSERT INTO `type_compte_depots` (`nom`, `taux_interet`) VALUES
    ('Compte Épargne Standard', 0.0200),      -- 2% annual interest
    ('Compte à Terme 6 Mois', 0.0300),       -- 3% annual interest  
    ('Compte à Terme 1 An', 0.0400),         -- 4% annual interest
    ('Compte Jeune', 0.0150),                -- 1.5% annual interest
    ('Compte Senior', 0.0250),               -- 2.5% annual interest
    ('Compte Premium', 0.0500);              -- 5% annual interest

-- Insert sample deposit accounts for testing
-- Note: These dates should be adjusted based on when tests are run
INSERT INTO `compte_depots` (`type_compte_depot_id`, `user_id`, `date_ouverture`, `date_echeance`, `montant`, `est_retire`, `date_retire`) VALUES
    -- Active accounts (not withdrawn) - adjust dates as needed
    (1, 1, '2024-01-15 10:00:00', '2025-07-15 10:00:00', 100000.00, 0, NULL),    -- Future maturity
    (2, 1, '2024-02-01 14:30:00', '2025-08-01 14:30:00', 250000.00, 0, NULL),    -- Future maturity
    (3, 2, '2024-01-01 09:00:00', '2026-01-01 09:00:00', 500000.00, 0, NULL),    -- Long-term
    (4, 2, '2024-03-15 16:45:00', '2025-09-15 16:45:00', 75000.00, 0, NULL),     -- Future maturity
    (5, 3, '2024-02-20 11:20:00', '2026-02-20 11:20:00', 1000000.00, 0, NULL),   -- Long-term
    
    -- Matured accounts (ready for withdrawal testing)
    (1, 1, '2023-01-15 10:00:00', '2024-01-15 10:00:00', 150000.00, 0, NULL),    -- Matured
    (2, 2, '2023-07-01 09:30:00', '2024-01-01 09:30:00', 300000.00, 0, NULL),    -- Matured
    
    -- Historical withdrawn accounts
    (6, 3, '2022-03-01 15:00:00', '2023-03-01 15:00:00', 800000.00, 1, '2023-03-05 16:30:00');

-- Verification query
SELECT 
    'Setup complete' as status,
    (SELECT COUNT(*) FROM type_compte_depots) as account_types,
    (SELECT COUNT(*) FROM compte_depots) as total_accounts,
    (SELECT COUNT(*) FROM compte_depots WHERE est_retire = 0) as active_accounts,
    (SELECT COUNT(*) FROM compte_depots WHERE est_retire = 1) as withdrawn_accounts;