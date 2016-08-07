# Twitter News

Themenanalyse von Nachrichtendiensten mittels Twitter.

## Setup mit STS (Spring Tool Suite)
1. Klone das Projekt mit git in ein Verzeichnis: ```git clone http://GITHUB_URL /pfad/zum/projektverzeichnis```
2. Importiere das Projekt in Eclipse mittels __File->Import->Existing Maven Projects__
3. Kopiere __twitter4j.properties_template__ nach __twitter4j.properties__ 
4. Pflege in __twitter4j.properties__ deine Twitter Keys&Tokens ein (erhälst du, wenn du unter https://apps.twitter.com/ eine neue App erstellst)
5. Starte das Projekt mit Rechtsklick -> Run As -> Spring Boot App

## Wie importiere ich Tweets?
- Tweets werden in __src/main/resources/web/resources/tweets.txt__ gespeichert
- Zu crawlende Twitter-Accounts kannst du in *de.bigdatapraktikum.twitternews.config.__AppConfig.java__* anpassen
- Zum starten des Crawlens, rufe __http://localhost:8082/collect__ auf
- Beim erneuten Ausführen des Crawlens werden nur neue Tweets der Datei angehangen

## Wie lasse ich meine importierten Tweets analysieren?
- Rufe __http://localhost:8082/web/index.html__ im Browser auf
- In der oberen rechten Ecke können Filterrestriktionen eingeblendet werden und eine neue Analyse gestartet werden
- Die letzte Analyse wird gespeichert und nach Neuladen der Webseite sofort angezeigt


--- 

*Ein Projekt im Rahmen des Big Data Praktikums der Universität Leipzig*