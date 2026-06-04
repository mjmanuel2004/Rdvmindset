-- 1. Création de la table de référence "industries"
CREATE TABLE industries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 2. Création de la table de liaison "company_industries"
CREATE TABLE company_industries (
    company_id UUID REFERENCES companies(id) ON DELETE CASCADE,
    industry_id UUID REFERENCES industries(id) ON DELETE CASCADE,
    PRIMARY KEY (company_id, industry_id)
);

-- 3. Migration des données existantes (pour ne pas perdre les secteurs déjà entrés)
-- On insère d'abord les noms uniques d'industries qui existent déjà dans 'companies'
INSERT INTO industries (name)
SELECT DISTINCT industry FROM companies WHERE industry IS NOT NULL;

-- Puis on fait le lien dans la table de liaison
INSERT INTO company_industries (company_id, industry_id)
SELECT c.id, i.id
FROM companies c
JOIN industries i ON c.industry = i.name;

-- 4. Suppression de l'ancienne colonne "industry" de la table "companies"
ALTER TABLE companies DROP COLUMN industry;
