-- Important note: 
-- If not specified differently, all tables come with a "ROWID" which is a inherent, auto-incremented not null primary key.
-- Hence, there is no need for a special, selfdefined ID
-- It can be accessed with "rowid" or "oid"
-- In this file, "oid" is used

CREATE TABLE IF NOT EXISTS positions (
    -- must have for every position, can never be null
    simple_class_name TEXT NOT NULL,                    -- the name of the class
    fully_qualified_class_name TEXT NOT NULL,           -- fully qualified class name including package information
    -- Optional
    file_name TEXT ,                                    -- path/name of the file if there is any
    method_name TEXT,                                   -- simple name of the method
    full_method_name TEXT                               -- including parameters / signature
);
CREATE UNIQUE INDEX IF NOT EXISTS iPositions 
    ON positions(simple_class_name,fully_qualified_class_name,file_name,method_name,full_method_name);

CREATE TABLE IF NOT EXISTS transformation_names (
    transformation_name TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS transformation_categories(
    category_name TEXT UNIQUE NOT NULL
);

-- Note: 
-- No Uniqueness over transformations - some transformations can be applied multiple times at the same method
CREATE TABLE IF NOT EXISTS transformations (
    name_reference INTEGER NOT NULL,
    position_reference INTEGER NOT NULL,
    FOREIGN KEY(name_reference) REFERENCES transformation_names(oid),
    FOREIGN KEY(position_reference) REFERENCES positions(oid)
);

CREATE TABLE IF NOT EXISTS transformation_name_category_mapping ( 
    name_reference INTEGER NOT NULL,
    category_reference INTEGER NOT NULL,
    FOREIGN KEY(name_reference) REFERENCES transformation_names(oid),
    FOREIGN KEY(category_reference) REFERENCES transformation_categories(oid)
);
CREATE UNIQUE INDEX IF NOT EXISTS iTransformation_name_category_mapping 
    ON transformation_name_category_mapping(name_reference,category_reference);

-- TODO: This layout looks like a massaker
CREATE VIEW  IF NOT EXISTS transformations_resolved 
AS 
SELECT * 
    FROM transformations as t 
    JOIN transformation_names as t_names 
    JOIN positions as p 
WHERE 
    t.name_reference = t_names.oid
AND 
    t.position_reference = p.oid
;