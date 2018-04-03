# Open e-Gov - LogChainer

The goal of the logchainer project is to create an unbroken sequence of log files so that any modification is registered. 
The chain works as following : the header of any file contains de hashCode of it's previous version, until there is no such 
version at which point the hashCode is still present but null (we arbitrarly defined such a hashCode as an empty one).

# Table of contents
- [Overview](#overview)
    - [Context](#context)
    - [Content](#content)
- [Running](#running)
	- [Components](#components)
- [Documentation](#documentation)
- [Contact](#contact)

# Overview 

## Context

This project only takes in charge the log chaining of provided files : if a version of the file isn't submited, no error 
will be sent and there will be an undetected breach. Thus it is left to user's discretion to correctly establish the history 
of modifications.

## Content

The project contains a working copy of the log chainer.

# Running

You can run the project from anywhere. However, you will have to write a run.sh or run.bat file to launch it.

## Components

You will have to define : 
*the localisation of all imported maven libraries
*the localisation of the application.properties file (where you define the absolute path of the xml file containing the 
	absolute path of directories to watch) : "-Dapplication.properties=pathToFile/application.properties"

# Documentation

A wiki page is dedicated to this project, for more information go to : https://prod.etat-ge.ch/wikiadm/pages in the "Catalogue 
des Services Transverses et des Composants Réutilisables" section and find 
the Log chainer (logchainer) page.

# Contact

Should you have any question, comment or proposal, please send an email to opensource@etat.ge.ch.

