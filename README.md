# Open e-Gov - LogChainer

The goal of the logchainer project is to create an unbroken sequence of log files so that any modification is registered. 
The chain works as following : the header of any file contains de hashCode of it's previous version, until there is no such 
version at which point the hashCode is still present but null (we arbitrarly defined such a hashCode as an empty one).

# Table of contents
- [Overview](#overview)
    - [Context](#context)
    - [Content](#content)
- [InstalLation](#installation)
- [Configuration](#configuration)
	- [Running](#running)
	- [Launcher](#launcher)
	- [Properties](#properties)
		- [Application](#application)
		- [Error](#error)
		- [Logback](#logback)
	- [Components](#components)
- [Working](#working)
	- [Process](#process)
	- [Exceptions](#exceptions)
	- [Monitoring](#monitoring)
- [Contact](#contact)

# Overview 

## Context

This project only takes in charge the log chaining of provided files : if a version of the file isn't submited, no error 
will be sent and there will be an undetected breach. Thus it is left to user's discretion to correctly establish the history 
of modifications. If a file does not have an appropriate name, it will be put in the corrupted files directory and the program won't do anything about it.

## Content

The project contains a working copy of the log chainer. The Zip version contains a functionnal launcher, properties file, an xml configuration file and a copy of the log chainer.

# InstalLation

1. Download zip file
2. Unzip the folder locally 
3. Give the launcher using the command : ``chmod +x ./bin/run.sh``
4. External configurations examples are already included in the zip so nothing has to be created in order to launch the project(to custom external configurations, cf §Configuration)
5. From the folder bin, launch the project using : ``./run.sh``

# Configuration

All external configuration files are given in the zip downloaded, these can be used as examples for the variable names (especially in the properties file where it is vital to use the same name) and the structure.

## Running

You can run the project from anywhere. However, you will have to create all necessary directories before launching and the JDK version has to be 1.8 or above.

## Launcher

The launcher must contain : 
- the path to the Java application or the JAVA_HOME system property has to have been set previously.
- the path to the project JAR.
- the paths to the 3 properties files :
	- the project properties (-Dapplication.properties='path')
	- the error messages properties (-DerrorMessages.properties='path')
	- the logback properties (-Dlogback.properties='path')

## Properties

### Application

File is referenced as "application.properties". This properties file should contain the path to the xml file (where the clients are defined).

### Error

File is referenced as "errorMessages.properties". This property file should contain all the messages related to the handled errors which can be thrown by the program. NB : the use of {} can seem stranged but it is because these messages are directly used as written in the logger from the Logback library, therefore the messages should comply to logback syntax.

### Logback

File is referenced as "logback.properties". This property file specifies the logging level of the project.

## Components

The components are defined for each client on which the logchainer will be plugged, these clients have to be defined in the xml file : (in brackets is the type in which the caracteristic will refered to in the xsd file interpreting the xml file)
- ClientId : (String) name of the client.
- InputDir : (path as a String but it has to be created previously to the launch by the user) this directory will be where the user put the files to be processed. They will only stay in this directory until they are processed. In the end no file should stay in this directory.
- OutputDir : (path as a String but it has to be created previously to the launch by the user) this directory will be where the user will get the files once processed.
- WorkingDir : (path as a String but it has to be created previously to the launch by the user) this directory is only used by the program itself has an temporary directory for the files being processed. Once it is done, a the file is transfered to the output directory but a copy stays here, to establish a chain. The chain is only created from the files already contained in this directory. Meaning, if one file is missing, the chain won't notice the hole and continue as if nothing wrong happened as the program is written to chain the previous file from the flux by taking the one present in this directory. (No file should be deleted from this directory in any other circonstances than the end of the flux chain).
- CorruptedFilesDir : (path as a String but it has to be created previously to the launch by the user) files with an invalid name will be transfered in this directory without process.
- Separator : (String) character marking the separation between the flux name and the sorting stamp. (Default is "_").
- SortingType : (String) type of sorting requested, 2 types possible : 
	- "alphabetical" , for the alphabetical order.
	- "numerical" , for the numerical order (Default).
- StampPosition : (String) postion of the sorting stamp in the filename, relatively to the flux. 2 positions possible :
	- "before" , when the sorting stamp is before the flux name in the file name.
	- "after" , when the sorting stamp is after the flux name in the file name (Default).
- FileEncoding : (String) encoding of the files in a client. The string given should comply with a Java Charset to be valid. (Default is "UTF-8").

# Working

Once the program is launched, the referenced clients in the xml file will be used to create a client list to which will be attached a WatchService (directory surveillance). Each client directory should contain an input directory where files will be put by the user and then detected, the WatchService will be plugged on this directory. A working directory where each file will be transfered once detected to be processed, a copy of each flux' last file will be kept in this directory to establish a history of the flux' files and thus to chain them. An output directory where each file will be transfered to once processed, it is left to user's discretion to get the files from this directory. In the case of an invalid filename, the file won't be processed and transfered to the corrupted files directory where it will be left for the user to take.
NB :
- If the user suppresses files from the working directory the chain will be broken but no error will be thrown.
- No file with the same name can be contained in the working directory when a file arrives in it (the stamp should prevent those kind of error).
- When a new file arrives in the output directory, if a file with the same name already exists, it will be overwritten by the new one.

## Process

- Before file process
1. Clients are instanciated with their attributs, a watcher and a watchKey. Then they are placed in a list that will be run through endlessly.
2. For each iteration on a client, we detect the arrival of a file(maximum on detection by iteration so in case of grouped arrival, the detection will take as many iteration on the client as there are files).
3. The file name is checked and if invalid, it is put in the corrupted directory witout being processed.
4. The caracteristics of the flux (name, stamp, arrival date) are then determined. We then map the flux name to the list of all it's files.
5. For each iteration on a client, we iterate on the list of it's flux. And for each iteration on a flux we iterate on all it's files.
6. A file is said to be ready to be processed after an arbitrary lap of time after it's detection. The flux is processed once all files are ready.
7. The flux process then launches and we iterate over all files for their process using the xml file defined sorting way with their stamp.

- File process
1. We check in the working directory if a same flux file is already present, in which case we get it's hashCode and suppress it (it will be the previous file in the chain).
2. We transfer the file being processed in the working directory.
3. We insert the previous file's hashCode if it exists, if it doesn't we insert an empty hash (arbitrary convention), as well as the previous file's name and the chaining date.

- After file process
1. We put the file in the output directory (keeping a copy in the working directory for history).
2. If the file was the last of it's flux, the flux is deleted from the client's flux list.
3. We go to the next iteration over the flux and over the clients, same as the beginning.

## Exceptions

These are the listed exceptions, other exceptions can still occure but won't be handled. The following abreviation will be used : if the exception leads to a program interruption (I) if not (NI).

- NameException : (class ClientService - method registerEvent) pattern of the name is incorrect. (NI)
- FileAlreadyExistsException : (class FolderService - method moveFileInDirWithNoSameNameFile) a same name file already exists in the directory.
- FileNotFoundException/NoSuchFileException : 
	- (class FolderService - method moveFileInDirWithNoSameNameFile) file couldn't be found in the input directory (possibly was detected but taken away before process).
	- (class LogChainerService - method chainingLogFile) (2 occurences) files were not found in the working directory.
	- (class LogWatcherService - method getPreviousFileHash) previous file wasn't found in the working directory when opening it as a stream.
- IOException : 
	- (class FolderService) 
		- (method moveFileInDirWithNoSameNameFile) when an IOException is thrown by the Files.move method.
		- (method copyFileToDirByReplacingExisting) when an IOException is thrown by the Files.copy method.
	- (class HashService - method getLogHash) exception when reading the given stream.
	- (class LogChainerService)
		- (method chainingLogFile) exception when reading the stream opened on the file to process.
		- (method accessToTmpFile) exception when reading the stream opened on the temp file.
		- (method insertionOfMessage) exception while manipulating the streams.
	- (class LogWatcherService)
		- (method initializeFileWatcherByClient) exception while initializing the watchKey.
		- (method getPreviousFileHash) exception when reading the stream from the previous file in the working directory.
- UnsupportedEncodingException : (class LogWatcherService - method newFileTreatment) encoding type isn't a valid Charset.
- JAXBException : (class LogChainer - method loadConfiguration) exception while loading the configurations from the xml file.
- CorruptedKeyException : (class LogWatcherService - method processEvents) directory isn't accessible anymore or key has gotten corrupted.
- WatchServiceError : (class Client - constructor) exception while creating the watchService.

## Monitoring

The monitoring of the application can be done at the address : ``localhost:8080`` .
You can monitor the following elements in addition to the default ones :
- The status of the programm's health which will only tell you if the application is still running (UP) or not (DOWN), to access this information add ``/actuator/health`` to the monitoring address.
- The informations of all detected corrupted files, which give you the total number of corrupted files that have been transfered by the programm and a recap for all clients corresponding to the number of corrupted files, their name and their size. Add ``/actuator/global/`` to the monitoring address.
- The informations of the detected corrupted files for a particular client. This will give you the number of corrupted files for the sepcified client and a recap of all these files with their name and size. Add ``/actuator/{clientName}`` to the monitoring address.

# Contact

Should you have any question, comment or proposal, please send an email to opensource@etat.ge.ch.

