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
# Invoke the Java X.400 API test/example utility
#
# You will probably want to modify the connection and authentication details.
# To do that edit the file com/isode/x400api/test/config.java (under this directory)
# and then rebuild the Java class with this command 
#
# javac -sourcepath . -verbose -classpath ".:/opt/isode/lib/java/classes/isode-x400.jar:/opt/isode/share/x400sdk/example/java/" \
#	-g X400_mstest.java
#
#


LD_LIBRARY_PATH=/opt/isode/lib:$LD_LIBRARY_PATH java -classpath .:/opt/isode/lib/java/classes/isode-x400.jar -Xms20m X400_mstest
