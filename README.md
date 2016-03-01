# Bitwise-file-operations
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%              readme.txt for MyDatabase project     %%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


MyDatabase java project has one .java file => MyDatabase.java
Name of the .jar file is MyDatabase.jar

Folder uploaded : MyDatabase.rar

The program shows the follwoing options on execution:

Enter Your Choice, any of the following:
Import
Query
Insert
Delete
Exit

Valid inputs are:

import <filename>.csv

select * from <table name> where <field name> [NOT] [=|>|=|<|=] <value>;

insert into <table name> values ('900', 'abcdfghjkkll', 'HR-112', '15', '2012', '110', '10.3', 'true', 'false', true', 'false');

delete from <PHARMA_TRIALS_1000B> where id = <value>;

%%%%%%%%%%
  IMPORT
%%%%%%%%%%

1)  when import command is executed a <filename>.db file is created 
2)  11 inex files are created with names like, <filename>.<field name>.ndx

%%%%%%%%%%
  SELECT
%%%%%%%%%%

1) when a select command is executed record(s) are fetched from the table and displayed
2) a message is also displayed if the records are actually present in the table or not


%%%%%%%%%%
  INSERT
%%%%%%%%%%

1) on insertion the .db file is modified 
2) all the inex files are also modified

%%%%%%%%%%%
  DELETE
%%%%%%%%%%%

1) on deletion the delete indicator ( 1st 4 bits of the last byte of each record) is made to zero
   initially it is 1
2) on successful deletion a message is displayed
3) when a select query is executed on the deleted rows the rows are displyed with a message that 
   those rows are marked as deleted 



