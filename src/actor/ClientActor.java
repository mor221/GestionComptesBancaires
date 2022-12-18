package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.sql.*;
import java.util.HashMap;

public class ClientActor extends AbstractActor {
    String nom;
    String id;
    int solde;

    int soldeMontantDecouvert;

    int soldePlafondCompte;
    String idBanquier;

    private ClientActor(String nom,String id,int solde,int soldeMontantDecouvert,int soldePlafondCompte,String idBanquier) {
        // reuête sql recuperant un client au hasard dans la base
        this.nom = nom;
        this.id = id;
        this.solde = solde;
        this.soldeMontantDecouvert = soldeMontantDecouvert;
        this.soldePlafondCompte = soldePlafondCompte;
        this.idBanquier = idBanquier;
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
        System.out.println("solde avant crédit "+solde);
        solde = solde+montant;
        System.out.println("solde après crédit "+solde);
    }
    public void debiterCompte(int montant) {
        System.out.println("solde avant débit "+solde);
        solde = solde-montant;
        System.out.println("solde après débit "+solde);
    }
    // Méthode servant à la création d'un acteur
    public static Props props(String nom,String id,int solde,int soldeMontantDecouvert,int soldePlafondCompte,String idBanquier) {
        return Props.create(ClientActor.class,nom,id,solde,soldeMontantDecouvert,soldePlafondCompte,idBanquier);
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
