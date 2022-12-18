package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;

public class ClientActor extends AbstractActor {
    String nom;
    String id;
    int solde;

    int soldeMontantDecouvert;

    int soldePlafondCompte;
    String idBanquier;

    private ClientActor() {
        // reuête sql recuperant un client au hasard dans la base
        this.nom = "Mor";
        this.id = "monId";
        this.solde = 1300;
        this.soldeMontantDecouvert = 400;
        this.soldePlafondCompte = 20000;
    }


    // Méthode servant à déterminer le comportement de l'acteur lorsqu'il reçoit un message
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetInfosClient.class, message -> sendInfo(getSender()))
                .match(CrediterCompteClient.class, message -> crediterCompte(message.montant))
                .match(DebiterCompteClient.class, message -> debiterCompte(message.montant))
                .build();
    }

    private void sendInfo(ActorRef actor) {
        HashMap<String,String> infosClient = new HashMap<>();
        infosClient.put("nom",this.nom);
        infosClient.put("id",this.id);
        infosClient.put("soldeMontantDecouvert",String.valueOf(this.soldeMontantDecouvert));
        infosClient.put("soldePlafondCompte",String.valueOf(this.soldePlafondCompte));
        infosClient.put("idBanquier",this.idBanquier);
        infosClient.put("solde",String.valueOf(this.solde));
        actor.tell(infosClient,this.getSelf());
    }

    public void crediterCompte(int montant) {
        solde = solde+montant;
        System.out.println("solde après crédit "+solde);
    }
    public void debiterCompte(int montant) {
        solde = solde+montant;
        System.out.println("solde après crédit "+solde);
    }

    // Méthode servant à la création d'un acteur
    public static Props props() {
        return Props.create(ClientActor.class);
    }

    // Définition des messages en inner classes
    public interface Message {}
    public static class CrediterCompteClient implements Message {
        int montant;
        public CrediterCompteClient(int montant) {
            this.montant = montant;
        }

    }
    public static class DebiterCompteClient implements Message {
        int montant;
        public DebiterCompteClient(int montant) {
            this.montant = montant;
        }

    }
    public static class GetInfosClient implements Message {
        public GetInfosClient() {
        }

    }
}
