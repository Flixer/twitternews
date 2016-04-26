# Twitter News

Themenanalyse von Nachrichtendiensten mittels Twitter.

## Setup mit Eclipse
1. Klone das Projekt mit git in ein Verzeichnis: ```git clone http://GITHUB_URL /pfad/zum/projektverzeichnis```
2. Importiere das Projekt in Eclipse mittels __File->Import->Existing Maven Projects__
3. Kopiere __twitter4j.properties_template__ nach __twitter4j.properties__ 
4. Pflege in __twitter4j.properties__ deine Twitter Keys&Tokens ein (erhälst du, wenn du unter https://apps.twitter.com/ eine neue App erstellst)

## Wie importiere ich Tweets?
- Tweets werden in __resources/tweet_storage__ gespeichert
- Zu crawlende Twitter-Accounts kannst du in *de.bigdatapraktikum.twitternews.utils.__AppConfig.java__* anpassen
- Zum starten des Crawlens, führe *de.bigdatapraktikum.twitternews.__TwitterNewsCollector.java__* aus

## Wie lasse ich meine importierten Tweets analysieren??
- Zum starten der Analyse, führe *de.bigdatapraktikum.twitternews.__TwitterNewsTopicAnalysis.java__* aus


--- 

*Ein Projekt im Rahmen des Big Data Praktikums der Universität Leipzig*