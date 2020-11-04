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
    -- TODO: Add Uniqueness over combination of all attributes
);

CREATE TABLE IF NOT EXISTS transformation_names (
    transformation_name TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS transformation_categories(
    category_name TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS transformations (
    name_reference INTEGER NOT NULL,
    position_reference INTEGER NOT NULL,
    FOREIGN KEY(name_reference) REFERENCES transformation_names(oid),
    FOREIGN KEY(position_reference) REFERENCES positions(oid)
);

-- 1 to many 
CREATE TABLE IF NOT EXISTS transformation_name_category_mapping ( 
    name_reference INTEGER NOT NULL,
    category_reference INTEGER NOT NULL,
    FOREIGN KEY(name_reference) REFERENCES transformation_names(oid),
    FOREIGN KEY(category_reference) REFERENCES transformation_categories(oid)
);

-- TODO: I do not like this layout lol 
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