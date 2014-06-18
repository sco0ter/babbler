# Build Instructions
---

If you want to build the source code yourself, this site is for you!

Babbler uses [Maven](http://maven.apache.org/) as build tool and is separated into a few modules. Actually only two, le\t's see...

## Module Overview

Let's start with a short overview over the build structure.
The project is structured into the following Maven modules:

* **xmpp-core**: This is the core module. It contains mainly only XML schema implementations, e.g. for Message, Presence, IQ and extensions, but also core classes, e.g. for a JID implementation. There's not much logic involved here, its main purpose is to map between XML and Java objects.<br/>The idea behind separating this module is, that it could theoretically be used by a client as well as a server implementation.
* **xmpp-client**: This contains the business logic used by a client, e.g. roster management, event handling, client authentication logic, etc.
* **xmpp-server**: This can currently be ignored. The idea is, that it could at some point provide classes for server specific XML schemas, like [XEP-0220](http://xmpp.org/extensions/xep-0220.html) or [XEP-0227](http://xmpp.org/extensions/xep-0227.html) or other logic, which every XMPP server can use, like a SCRAM-SHA-1 SaslServer implementation.

![Module Overview](ModuleOverview.png)

## Building the Project

First you obviously need to get the source code. You can get it either by [downloading the repository](https://bitbucket.org/sco0ter/babbler/downloads) as zip package or cloning it via Mercurial.
In order to build the project you need to run the following common Maven command on the root directory:

> mvn clean package

This will build all the modules, run the tests, merge the core and client module into one jar file (via the maven-assembly-plugin) and create the JavaDoc.
You will find everything you need in the `target` folder of the xmpp-client module. The assembled jar file is called "Babbler-{version}.jar"
