This is a version of javarosa that has been modified to NOT run on J2ME devices.
It consists of the core from the javarosa 1.0 codebase, modified with:
 -- KoBo additional instance defn. and filter paths
 -- remember all bind attributes and any additional attributes on <input>, <select>, <group>, etc. statements
 -- numerous enhancements and contributions from SurveyCTO and others.
 -- j2me sub-projects removed

 The requires the following dependencies before it can be built:

Download: javarosa-dependencies-r3073.rar ( https://bitbucket.org/javarosa/javarosa/downloads )
 
 This contains:
 
	j2me/buildfiles/tools/ant-contrib.jar
	j2me/buildfiles/tools/javarosa-ant-libs.jar
	j2me/buildfiles/tools/UmlGraph.jar
	lib/bouncycastle-lw.jar
	lib/j2meunit-javarosa.jar
	lib/kxml2-2.3.0.jar
	lib/regexp-me.jar
	util/schema-gen/lib/xpp3-1.1.4.jar

Go to http://bitbucket.org/javarosa/javarosa/wiki/GettingStarted to learn how to build JavaRosa.

The Ant tasks have been trimmed to exclude j2me tests and builds.

From your eclipse workspace, go to j2me-build project, select build.xml, right-click, Ant Build...

Select either
- clean
- package  (will build javarosa library)
- RunUnitTests  (will run unit tests in core)
- CreateJavadoc

The 'package' target is the official way to build the javarosa jar.


