package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;

public class BanquierActor extends AbstractActor {
    String nom;
    String number;
    HashMap<String, DonneesActor> mesClients;
    private BanquierActor() {
        this.nom = "Mor";
        this.number = "monId";
        //this = "banquier1";
    }


    // Méthode servant à déterminer le comportement de l'acteur lorsqu'il reçoit un message
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CrediterCompte.class, message -> estCreditable(message.montant,message.client,getSender()))
                .match(DebiterCompte.class, message -> estDebitable(message.montant,message.client,getSender()))
                //.match(SayBye.class, message -> sayBye(message))
                .build();
    }

    private void estDebitable(int montant, HashMap<String, String> client, ActorRef actor) {
        System.out.println("Etat solde client:"+client.get("solde"));
        int solde = Integer.valueOf(client.get("solde"));
        int soldeMontantDecouvert = Integer.valueOf(client.get("soldeMontantDecouvert"));
        actor.tell((solde-montant)>soldeMontantDecouvert,this.getSelf());
    }

    private void estCreditable(int montant, HashMap<String,String> client, ActorRef actor) {
        int solde = Integer.valueOf(client.get("solde"));
        int soldePlafondCompte = Integer.valueOf(client.get("soldePlafondCompte"));
        System.out.println("Etat solde client:"+client.get("solde"));
        actor.tell((solde+montant)<soldePlafondCompte,this.getSelf());
    }


    // Méthode servant à la création d'un acteur
    public static Props props() {
        return Props.create(BanquierActor.class);
    }

    // Définition des messages en inner classes
    public interface Message {}

    public static class CrediterCompte implements Message {
        int montant;
        HashMap<String,String> client;
        public CrediterCompte(int montant,HashMap<String,String> client) {
            this.montant = montant;
            this.client = client;
        }
    }

    public static class DebiterCompte implements Message {
        int montant;
        HashMap<String,String> client;
        public DebiterCompte(int montant,HashMap<String,String> client) {
            this.montant = montant;
            this.client = client;
        }
    }
}
