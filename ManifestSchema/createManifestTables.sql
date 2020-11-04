CREATE TABLE transformations (
    int id
    -- ID 
    -- name reference key
    -- position reference key
);

CREATE TABLE positions (
    int id
    -- ID 
    -- simple class name 
    -- fully qualified class name
    -- identifiers of file
    -- identifiers of method if any
    -- (method fully qualified, including params)
);

CREATE TABLE transformation_names (
    int id 
    -- ID 
    -- Name
);

CREATE TABLE transformation_categories(
    int id
    -- ID
    -- String name
);

-- 1 to many 
CREATE TABLE transformation_name_category_mapping ( 
    int id
    -- transformationName ID
    -- Category ID
);

CREATE VIEW transformations_resolved 
AS 
SELECT * FROM transformations_resolved;