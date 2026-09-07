@REM This runs the High Level X.400 Java Client API test/example utility
@REM
@REM First you will have to modify the connection and authentication details.
@REM It's better to work on copies of the original files, so start by
@REM copying this to the BINDIR area (usually C:\Program Files\Isode\bin)
@REM
@REM NB: installations in non-default places, eg different disk,
@REM or non-English language versions of Windows will use a different path
@REM
@REM Then edit the Java files under this directory 
@REM BINDIR/com/isode/x400/highlevel/test/ 
@REM 
@REM For example, modify SendX400Mail.java, and compile it with this command
@REM
@REM javac.exe -sourcepath . -classpath ".;./com/isode/x400/highlevel/test;%CLASSDIR%\isode-x400.jar;%CLASSDIR%\isode-rbac.jar;%CLASSDIR%\isode-hlxja.jar" -g com/isode/x400/highlevel/test/SendX400Mail.java
@REM
@REM And run it with the commands below

@REM If you haven't done so already, you will need to copy the .class file to
@REM the Isode bin directory (usually C:\Program Files\Isode\bin) and run it
@REM it from there, or you will have to edit your PC's PATH environment
@REM variable to include C:\Program Files\Isode\bin


@REM Setting up the enviornment

@set BINDIR=D:\Archiv~1\Isode\bin
@set SHAREDIR=D:\Archiv~1\Isode\share
@set CLASSDIR=D:\Archiv~1\Isode\bin\java\classes
@set PATH=%PATH%:%BINDIR%

@REM These scripts need to be run from the %BINDIR% directory, so cd first
cd %BINDIR%



@REM To send X.400 mail use this sample program
@REM
java -Djava.library.path=%BINDIR% -classpath ".;./com/isode/x400/highlevel/test;%CLASSDIR%\isode-x400.jar;%CLASSDIR%\isode-rbac.jar;%CLASSDIR%\isode-hlxja.jar" com/isode/x400/highlevel/test/SendX400Mail


@REM To receive X.400 mail use this sample program (uncomment the @REM)
@REM
@REM java -Djava.library.path=%BINDIR% -classpath ".;.\com\isode\x400\highlevel\test;%CLASSDIR%\isode-x400.jar;%CLASSDIR%\isode-rbac.jar;%CLASSDIR%\isode-hlxja.jar" com/isode/x400/highlevel/test/ReceiveX400Mail
