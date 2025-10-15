-- Insert standard account types for testing
INSERT INTO `type_compte_depots` (`nom`, `taux_interet`) VALUES
    ('Compte Épargne Standard', 0.0200),      -- 2% annual interest
    ('Compte à Terme 6 Mois', 0.0300),       -- 3% annual interest  
    ('Compte à Terme 1 An', 0.0400),         -- 4% annual interest
    ('Compte Jeune', 0.0150),                -- 1.5% annual interest
    ('Compte Senior', 0.0250),               -- 2.5% annual interest
    ('Compte Premium', 0.0500);              -- 5% annual interest
