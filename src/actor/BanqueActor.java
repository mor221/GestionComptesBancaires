package actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.pattern.Patterns;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.CompletionStage;

public class BanqueActor extends AbstractActor {
    private final ActorRef bdd;
    private ActorRef client;
    private ActorRef banquier;

    private ActorRef ihm;
    private ActorRef finTransaction;


    private BanqueActor() {
        // Création d'acteurs enfants
        this.banquier = getContext().actorOf(BanquierActor.props(), "banquier");
        this.ihm = getContext().actorOf(IhmActor.props(),"ihm");
        this.bdd = getContext().actorOf(DonneesActor.props(),"bdd");
        this.finTransaction = null;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InitBanque.class, message -> initBanque(getSender()))
                .match(Transaction.class, message -> transaction(message,getSender()))
                .build();
    }
    private void initBanque(ActorRef sender) {
        String id = "";
        String password = "";
        int montant = 0;
        String typeTransaction="";
        HashMap<String,String> client = null;
        // On recupère l'id du client
        boolean continuer= true;
        do {
            CompletionStage<Object> resutIdClient = Patterns.ask(ihm, new IhmActor.GetID(), Duration.ofSeconds(100));
            id = "";
            try {
                id = (String) resutIdClient.toCompletableFuture().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            CompletionStage<Object> resultBddclient = Patterns.ask(bdd,new DonneesActor.GetClient(id), Duration.ofSeconds(100));
            try {
                client = (HashMap<String,String>) resultBddclient.toCompletableFuture().get();

            } catch (Exception e) {
                e.printStackTrace();
            }
            if ((client.size())==0){
                System.out.println("Cet id n'existe pas");
            }
            else{
                continuer=false;
            }
        }while(continuer);
        // On verifie le mdp
        do {
            CompletionStage<Object> resutpasswordClient = Patterns.ask(ihm, new IhmActor.GetPassword(), Duration.ofSeconds(100));
            password = "";
            try {
                password = (String) resutpasswordClient.toCompletableFuture().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!(password.equals(client.get("password"))))
                System.out.println("Mot de passe incorrect ! ");

        }while (!(password.equals(client.get("password"))));// requete pour vérifier qu'il existe dans la base
        // type de transaction
        CompletionStage<Object> resultTypeTransaction = Patterns.ask(ihm, new IhmActor.GetTypeTransaction(), Duration.ofSeconds(100));
        try {
            typeTransaction = (String) resultTypeTransaction.toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // on recupère le montant
        CompletionStage<Object> resultMontant = Patterns.ask(ihm, new IhmActor.GetMontant(), Duration.ofSeconds(100));
        try {
            montant = Integer.valueOf((String) resultMontant.toCompletableFuture().get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // debut de la transaction
        this.client = getContext().actorOf(ClientActor.props(client.get("nom"),client.get("id"),Integer.valueOf(client.get("solde")),Integer.valueOf(client.get("montantDecouvertAutorise")),Integer.valueOf(client.get("montantPlafond")),client.get("idBanque")), "client");
        this.getContext().getSelf().tell(new Transaction(montant,client,typeTransaction), ActorRef.noSender());

    }
    public void transaction(final Transaction message,ActorRef finTransaction) {
        this.finTransaction = finTransaction;
        CompletionStage<Object> result1 = Patterns.ask(client,new ClientActor.GetInfosClient(),Duration.ofSeconds(10));
        HashMap<String,String> infosClient = null;
        try {
            infosClient = (HashMap<String, String>) result1.toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        CompletionStage<Object> result3 = null;
            switch (message.typeTransaction){
            case "debiter":
                result3 = Patterns.ask(banquier, new BanquierActor.DebiterCompte(message.montant, infosClient), Duration.ofSeconds(10));
                Boolean estDebitable = false;
                try {
                    estDebitable = (Boolean) result3.toCompletableFuture().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (estDebitable){
                    client.tell(new ClientActor.DebiterCompteClient(message.montant), ActorRef.noSender());
                    System.out.println("compte débité avec succès");
                    result1 = Patterns.ask(client,new ClientActor.GetInfosClient(),Duration.ofSeconds(10));
                    infosClient = null;
                    try {
                        infosClient = (HashMap<String, String>) result1.toCompletableFuture().get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    bdd.tell(new DonneesActor.UpdateClient(infosClient.get("id"),Integer.valueOf(infosClient.get("solde"))),ActorRef.noSender());
                    finTransaction.tell("fini",getSelf());
                }
                else {
                    System.out.println("opération impossible");
                }
                break;

            case "crediter":
                result3 = Patterns.ask(banquier,new BanquierActor.CrediterCompte(message.montant,infosClient),Duration.ofSeconds(10));
                Boolean estCreditable = false;
                try {
                    estCreditable = (Boolean) result3.toCompletableFuture().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (estCreditable){
                    client.tell(new ClientActor.CrediterCompteClient(message.montant), ActorRef.noSender());System.out.println("compte crédité avec succès");
                    result1 = Patterns.ask(client,new ClientActor.GetInfosClient(),Duration.ofSeconds(10));
                    infosClient = null;
                    try {
                        infosClient = (HashMap<String, String>) result1.toCompletableFuture().get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    bdd.tell(new DonneesActor.UpdateClient(infosClient.get("id"),Integer.valueOf(infosClient.get("solde"))),ActorRef.noSender());
                    finTransaction.tell("fini",getSelf());

                }
                else {
                    System.out.println("opération impossible");
                }
                break;
        }
        finTransaction.tell("Fini", getSelf());
    }
    public static Props props() {
        return Props.create(BanqueActor.class);
    }

    // Définition des messages en inner classes
    public interface Message {}

    public static class Transaction implements Message {
        int montant;
        String typeTransaction;

        HashMap<String,String> client;
        public Transaction(int montant, HashMap<String, String> client, String typeTransaction) {
            this.montant = montant;
            this.typeTransaction = typeTransaction;
            this.client = client;
        }

    }
    public static class InitBanque implements Message {
        public InitBanque() {
        }

    }

    public static class EndGame implements Message {
        public EndGame() {}
    }
}
