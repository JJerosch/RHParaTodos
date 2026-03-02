-- Remove departamento_pai_id column from departamentos table
ALTER TABLE departamentos DROP COLUMN IF EXISTS departamento_pai_id;
