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
                .match(CrediterCompte.class, message -> estCreditable(message,getSender()))
                .match(DebiterCompte.class, message -> estDebitable(message,getSender()))
                //.match(SayBye.class, message -> sayBye(message))
                .build();
    }

    private void estDebitable(final DebiterCompte message, ActorRef actor) {
        int solde = Integer.valueOf(message.client.get("solde"));
        int soldeMontantDecouvert = Integer.valueOf(message.client.get("soldeMontantDecouvert"));
        actor.tell((solde-message.montant)>soldeMontantDecouvert,this.getSelf());
    }

    private void estCreditable(final CrediterCompte message, ActorRef actor) {
        int solde = Integer.valueOf(message.client.get("solde"));
        int soldePlafondCompte = Integer.valueOf(message.client.get("soldePlafondCompte"));
        actor.tell((solde+message.montant)<soldePlafondCompte,this.getSelf());
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
