#/bin/sh
# -*- sh -*-
#
# Copyright (c) 2008-2009, Isode Limited, London, England.
# All rights reserved.
#
# Acquisition and use of this software and related materials for any
# purpose requires a written licence agreement from Isode Limited, or
# a written licence from an organisation licenced by Isode Limited to
# grant such a licence.
#
#
# Invoke the High Level X.400 Java Client API test/example utility
#
# First you will have to modify the connection and authentication details.
# To do that edit the Java files under this directory 
# com/isode/x400/highlevel/test/ 
# 
# For example, for SendX400Mail.java, compile it with this command
#
# javac -sourcepath . -classpath ".:/opt/isode/lib/java/classes/isode-x400.jar:/opt/isode/lib/java/classes/isode-rbac.jar:/opt/isode/lib/java/classes/isode-hlxja.jar" -g com/isode/x400/highlevel/test/SendX400Mail.java

#
# Once you have modified the source and compiled it, you can run it:
# ./run_hl_test.sh


java -classpath .:/opt/isode/lib/java/classes/isode-x400.jar:/opt/isode/lib/java/classes/isode-rbac.jar:/opt/isode/lib/java/classes/isode-hlxja.jar  -Xms20m com/isode/x400/highlevel/test/SendX400Mail


