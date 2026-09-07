REM This runs the X.400 Java API test
REM
REM NB installations in non-default places, eg different disk,
REM or none-English language Windows may need to edit this line
REM
REM In order to run this test, you will need to copy the .class file 
REM to the Isode bin directory (C:\Program Files\Isode\bin) and run it
REM it from there, or you will have to edit your PC's PATH environment
REM variable to include C:\Program Files\Isode\bin
REM
java  -Djava.library.path="C:\PROGRA~1\Isode\bin" -classpath .;"c:\progra~1\Isode\bin\java\classes\isode-x400mt.jar;C:\Program Files\Isode\share\x400sdk\example\java" -verbose:jni -Xms20m X400_mttest



REM You will probably want to modify the connection and authentication details.
REM To do that edit the file com/isode/x400api/test/config.java (under this directory)
REM and then rebuild the Java class with this command 
REM E:\j2sdk1.4.2_05\bin\javac -sourcepath . -verbose -classpath ".;c:\progra~1\Isode\bin\java\classes\isode-x400.jar;c:\progra~1\Isode\bin\java\classes\isode-x400mt.jar;C:\Program Files\Isode\share\x400sdk\example\java" -g X400_mttest.java


