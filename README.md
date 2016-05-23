# Twitter News

Themenanalyse von Nachrichtendiensten mittels Twitter.

## Setup mit Eclipse
1. Klone das Projekt mit git in ein Verzeichnis: ```git clone http://GITHUB_URL /pfad/zum/projektverzeichnis```
2. Importiere das Projekt in Eclipse mittels __File->Import->Existing Maven Projects__
3. Kopiere __twitter4j.properties_template__ nach __twitter4j.properties__ 
4. Pflege in __twitter4j.properties__ deine Twitter Keys&Tokens ein (erhälst du, wenn du unter https://apps.twitter.com/ eine neue App erstellst)

## Wie importiere ich Tweets?
- Tweets werden in __web/resources/tweets.txt__ gespeichert
- Zu crawlende Twitter-Accounts kannst du in *de.bigdatapraktikum.twitternews.utils.__AppConfig.java__* anpassen
- Zum starten des Crawlens, führe *de.bigdatapraktikum.twitternews.__TwitterNewsCollector.java__* aus
- Beim erneuten Ausführen des Crawlens werden nur neue Tweets der Datei angehangen

## Wie lasse ich meine importierten Tweets analysieren?
- Stelle eventuelle Filterrestriktionen am Anfang von *de.bigdatapraktikum.twitternews.__TwitterNewsGraphCreator.java__* ein (aktuell noch manuell programmatisch)
- Zum Starten der Analyse, führe *de.bigdatapraktikum.twitternews.__TwitterNewsGraphCreator.java__* aus
- Es werden die Dateien __web/resources/nodes.txt__ und __web/resources/edges.txt__ angelegt, welche Knoten und Kanten des Graphen beinhalten
- Rufe __web/index.html__ in einem Browser auf, um dir den Graphen anzeigen zu lassen


--- 

*Ein Projekt im Rahmen des Big Data Praktikums der Universität Leipzig*