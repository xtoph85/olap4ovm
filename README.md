olap4ovm-UI
==============

This is a simple UI to perform OLAP functions on a graph.


Workflow
========

Setup prerequisites for the OLAP API:

1) Download Eclipse and install the Maven Plugin (http://www.eclipse.org/m2e/)
 2) Import the projects by using Import->Existing Maven Projects
 3) Download and extract the CKR Framework (https://dkm.fbk.eu/ckr-tourismdemo) and the Fuseki Server (http://jena.apache.org/)
    The API has been tested using following versions: CKR [last change on 23/04/2013] Fuseki [2.0.0]
 4) Add --set tdb:unionDefaultGraph=true to the java call of the fuseki-server.jar in the file fuseki-server.bat so it looks like this:
    java -Xmx1200M -jar fuseki-server.jar --set tdb:unionDefaultGraph=true %*
 5) Start up both CKR Framework and the Fuseki Server
 6) Open the Webinterface of the CKR Framework (http://localhost:50000/admin/)
 7) Select "System" Repository first. Create the ruleset by selecting "Rulesets". Insert the name "ckr:ckr-olap-rdfs" into the field for the URI
    and paste the content of the file "ckr-olap_ruleset_rdfs.ttl" (located in the folder "resources") into the lower text area.
    Click "update" to create the ruleset.
 8) Create the Base repository by selecting "New repository". Select "Springles" as Type and enter "Base" as ID and Title. Ensure that the ruleset
    created earlier is selected. Click "Create". T
 9) Import the dimensional model "dimensional_model.trig" (located in the folder "resources") by selecting "Add". First load the RDF Data File
   by clicking the button near "RDF Data File". Then select "TriG" for the Data Format. Click Upload. This may take a while.
 10) Open the Webinterface of the Fuseki Server (http://localhost:3030/)
 11) Create the Temp repository by selecting "Manage Datasets"->"Add new Dataset". Insert "Temp" for the dataset name.
    Make sure to select "Persistent" as Dataset type. Otherwise the interpretation of the default graph
     (configured in point 4) will be ignored by the fuseki server
 12) Now you are able to run the different JUnit test classes. Notice to first delete all content of the Temp repository after executing a testcase.

ii)Afterwards package the projecte (mvn package)
iii)Run the webserver (i.e. mvn jetty:run) and move to lovalhost/<projectname>

