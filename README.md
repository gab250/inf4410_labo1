inf4410_labo1
=============

Pour l'ex�cution,

1) compiler le code avec

$/opt/netbeans-7.3.1/java/ant/bin/ant

2) 

Ex�cuter RMI registry dans ./bin avec:

$/opt/jdk.x86_64/jre/bin/rmiregistry &

3) 

Ex�cuter le script sever avec : 

$./server &

4) On peut ensuite ex�cuter le client avec les commande suivante

$./client create NomDunFichier

$./client list

$./client sync NomDunFichier

$./client push NomDunFichier

