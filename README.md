olap4ovm-UI
==============

This is a simple UI to perform OLAP functions on graphs, written with VaadinFramework.


Workflow
========

i) Setup prerequisites for the OLAP API:

 1) Download and extract the CKR Framework (https://dkm.fbk.eu/ckr-tourismdemo) and the Fuseki Server (http://jena.apache.org/)
    The API has been tested using following versions: CKR [last change on 23/04/2013] Fuseki [2.0.0]
 2) Add --set tdb:unionDefaultGraph=true to the java call of the fuseki-server.jar in the file fuseki-server.bat so it looks like this:
    java -Xmx1200M -jar fuseki-server.jar --set tdb:unionDefaultGraph=true %*
    For Unix-based System change file fuseki-server so it starts with:
     java -Xmx1200M -jar fuseki-server.jar --set tdb:unionDefaultGraph=true 
 3) Start up both CKR Framework and the Fuseki Server
 4) Open the Webinterface of the CKR Framework (http://localhost:50000/admin/)
 5) Select "System" Repository first. Create the ruleset by selecting "Rulesets". Insert the name "ckr:ckr-olap-rdfs" into the field for the URI
    and paste the content of the file "ckr-olap_ruleset_rdfs.ttl" (located in the folder "resources") into the lower text area.
    Click "update" to create the ruleset.
 6) Create the Base repository by selecting "New repository". Select "Springles" as Type and enter "Base" as ID and Title. Ensure that the ruleset
    created earlier is selected. Click "Create". T
 7) Import the dimensional model "dimensional_model.trig" (located in the folder "resources") by selecting "Add". First load the RDF Data File
    by clicking the button near "RDF Data File". Then select "TriG" for the Data Format. Click Upload. This may take a while.
 8) Open the Webinterface of the Fuseki Server (http://localhost:3030/)
 9) Create the Temp repository by selecting "Manage Datasets"->"Add new Dataset". Insert "Temp" for the dataset name.
     Make sure to select "Persistent" as Dataset type. Otherwise the interpretation of the default graph
     (configured in point 4) will be ignored by the fuseki server

ii) Make sure maven is installed on the system and package the project (i.e. wihtin the project-folder type in console: mvn package)
iii) Run a webserver (i.e. mvn jetty:run) and move to localhost<:port> (likely localhost or localhost:8080)

