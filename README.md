# MoCCasIN
<img src="https://github.com/LarsHadidi/ResourcesRepository/blob/master/Feather.png" width="90" align="left" hspace="10" vspace="8" alt="Logo"/>

The **M**edical **C**lassification **C**ode F**in**der (*for research purposes only*)


**MoCCasIN** is a [Oracle Helidon](https://github.com/oracle/helidon) based microservice, and java library and a js library to visualize and search of the following two medical code catalogs:

- [International Statistical Classification of Diseases and Related Health Problems](https://en.wikipedia.org/wiki/International_Statistical_Classification_of_Diseases_and_Related_Health_Problems)
- [Anatomical Therapeutic Chemical Classification System](https://en.wikipedia.org/wiki/Anatomical_Therapeutic_Chemical_Classification_System)

Those are controlled by the [World Health Organization](http://www.who.int/).

This microservice serves as an open source service to include those catalogs into any server application. 

MoCCasIn relies on CSV versions of those catalogs, which are to be obtained via the homepage of the [German Institute for Medical Documentation and Information](https://www.dimdi.de/dynamic/en/homepage/index.html). 
- *ICD* Head over to the downloads page and get the ICD-10-GM Metadaten TXT (CSV).
- *ATC* Follow the WIdo link to the Excel list of ATC codes.

## Disclaimer
**No warranty for correctness or completenes of MoCCasIN is provided.**

**See the license document for further information.**

## Description

The MoCCasIN backend-library consists of three parts:

- **The MoCCasIN Tree generator:** An [Apache Maven](https://maven.apache.org/) PlugIn consisting of one MoJo: generate. It generates a tree graph representation of the CSV Data provided by DIMDI stored into a file for ICD and ATC respectively.

- **The MoCCasIN Java Library:** A Java 8 library which loads and queries the files generated by the MoCCasIN tree generator.

- **The MoCCasIN Microservice:** A Java microservice powered by [Oracle Helidon](https://github.com/oracle/helidon) utilizing the MoCCasIN library to provide REST endpoints for the frontend.

The MoCCasIN frontend-library consists of one JS library:

- **The MoCCasIN JS Library:** A single JS file providing a pop-up window which enables users to view and search the medical catalog trees. Compatible with Internet Explorer.

## Getting started

First, download the repository to your local filesystem.

### MoCCasIN Tree generator

Within the folder `moccasin-generator` run `mvn install`. You may need to set maven's platform encoding to `UTF-8`.

In your project, like in the MoCCasIN microservice, add the plug-in into your pom.xml:

```xml
<plugin>
    <groupId>MoCCasIN.moccasin</groupId>
    <artifactId>moccasin-generator</artifactId>
    <version>1.0</version>
    <configuration>
        <targetPath>libs/trees</targetPath>
        <sourcePath>libs/trees</sourcePath>
        <icdChaptersFile>icd10gm2018syst_kapitel.txt</icdChaptersFile>
        <icdGroupsFile>icd10gm2018syst_gruppen.txt</icdGroupsFile>
        <icdCodesFile>icd10gm2018syst_kodes.txt</icdCodesFile>
        <atcCodesFile>Amtlicher_ATC_Index_2017_.CSV</atcCodesFile>
        <icdCsvSeparator>;</icdCsvSeparator>
        <atcCsvSeparator>;</atcCsvSeparator>
    </configuration>
</plugin>
```
containing the following values for the nodes:
- targetPath: Path to the folder where the tree files should be generated into. 
- sourcePath: Path to the folder containing the CSV data from DIMDI.
- icdChaptersFile: Name of the file within *sourcePath* holding the ICD chapters.
- icdGroupsFile: Name of the file within *sourcePath* holding the ICD groups.
- icdCodesFile: Name of the file within *sourcePath* holding the ICD codes.
- atcCodesFile: Name of the file within *sourcePath* holding the ATC codes.
- icdCsvSeparator: CSV separator symbol used in the files for the icd catalog.
- atcCsvSeparator: ATC separator symbol used in the files for the atc catalog.

### MoCCasIN Java Library

Within the folder `moccasin-library` run `mvn install`. You may need to set maven's platform encoding to `UTF-8`.

In your project, like in the MoCCasIN microservice, add the dependency into your pom.xml:

```xml
<dependency>
  <groupId>MoCCasIN.moccasin</groupId>
  <artifactId>moccasin-library</artifactId>
  <version>1.0</version>
</dependency>
```

You can then use the class `moccasin.moccasin.controller.TreeBuilder` within your code. See the JavaDoc for the functionality descriptions of its methods.

### MoCCasIN Microservice

Inside folder `moccasin-microservice` run `mvn package`. You may need to set maven's platform encoding to `UTF-8`.

It expects the tree files to be located in the folder `libs/trees` within its working directory. This can be set in the `microprofile-config.properties` file in `src/main/resources/META-INF`. You can also specify host and port there. To start the mircoservice, run `java -jar moccasin-microservice.jar`

### MoCCasIN JS Library

The JavaScript library can be found in `moccasin-library/src/main/js/`. Include the script into your HTML markup. The library has the following JS dependencies:
- [jsTree](https://github.com/vakata/jstree)
- [jQuery](https://jquery.com/)
- [Bootstrap](https://getbootstrap.com/)

The **M**edical **C**lassification **C**ode F**in**der UI element needs to be created on an `input` element which has the following attributes:
- type: `text`
- class: `moccasin`
- data-uuid: an universally unique identifier
- data-catalog: a string denoting the type of catalog to use, either `"icd"` or `"atc"`

The MoCCasIN UI element will be initialised using the following constructor:

```js
new MoccasinDialog(dom-input-element, url-to-microservice);
```

The following example illustrates its usage:

```html
<head>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.3.5/jstree.min.js"></script>
    <script src="moccasin-library/src/main/js/moccasinDialog.js"></script>

    <script>
           $(".moccasin").each(function (index) {
               new MoccasinDialog($(this)[0], "http://localhost/moccasin");
            });
    </script>
</head>
<body>
    <input type="text" class="moccasin" name="ICD Catalog" data-uuid="1b64534e-b164-4505-8fac-20f6a30d0661" data-catalog="icd"/>
</body>

```


