Hilly Fields
============

This project is a demo application for the [Spring Data Neo4j](https://github.com/SpringSource/spring-data-neo4j)
library which provides convenient access to the [Neo4j](http://neo4j.org) graph database.

This tutorial is a fully functioning micro-service based web-application built using the following components

- Spring Boot
- Spring MVC
- Spring Data Neo4j
- Angular.js
- Twitter Bootstrap UI

The application's domain is a fictitious educational institution - Hilly Fields Technical College - and the application
allows you to manage the College's Departments, Teaching Staff, Subjects, Students and Classes.

It leverages the power of Spring Data Neo4j/Spring MVC and in particular the new Neo4j Object Graph mapping technology
to provide a RESTful interface with which the web client interacts. The application is entirely stateless: every
interaction involves a call to a Neo4j server, hopefully demonstrating the speed of the new technology, even over the
wire.

WARNING
-------
By default, the application will attempt to use a Neo4j instance running on the same machine as the application server, and
on the standard port 7474. *IT WILL DESTROY ALL THE DATA IN THAT DATABASE AT STARTUP*. So if you don't want that to happen
please back up any existing database first.

Pre-requisites
--------------
Before running this application you will need to install node.js if it is not already installed:

- Node.js v0.10x+
- npm (which comes bundled with Node) v2.1.0+

Visit the node website for details of installing node for your particular operating system.

Once node is installed you'll need to grab the following npm packages:

    npm install --global bower grunt-cli

Now start your Neo4j server instance, if its not already running. **You should back up any data you want to keep because
the application will purge any existing data first**

Installing SDN
--------------
If you have not already done so, you'll need to download the latest version of SDN from GitHub:

    git clone https://github.com/neo4j/neo4j-ogm.git
    cd neo4j-ogm
    mvn clean install -DskipTests=true

Once this is done, you can start the Spring-Boot application server.

Starting the application
------------------------

    cd neo4j-spring-examples/sdn-boot
    grunt build
    mvn spring-boot:run

Now point your browser at localhost:8080 and explore.

Note that you can stop the application server at any time by pressing Ctrl-C in the console window from where you
launched it.


