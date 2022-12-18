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
        this.client = getContext().actorOf(DonneesActor.props(), "client");
        this.banquier = getContext().actorOf(BanquierActor.props(), "banquier");
        this.ihm = getContext().actorOf(IhmActor.props(),"ihm");
        this.bdd = getContext().actorOf(DonneesActor.props(),"bdd");
        this.finTransaction = null;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InitBanque.class, message -> initBanque(getSender()))
                .match(Transaction.class, message -> transaction(message.montant,message.typeTransaction,getSender()))
                .build();
    }
    private void initBanque(ActorRef sender) {
        String id = "";
        String password = "";
        int montant = 0;
        String typeTransaction="";
        // On recupère l'id du client
        boolean continuer= true;
        do {
            CompletionStage<Object> resutIdClient = Patterns.ask(ihm, new IhmActor.GetID(), Duration.ofSeconds(10));
            id = "";
            try {
                id = (String) resutIdClient.toCompletableFuture().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            CompletionStage<Object> resultBddclient = Patterns.ask(bdd, new DonneesActor.GetClient(id), Duration.ofSeconds(100));
           // HashMap<String,String> client = null;
            HashMap<String,String> client = null;
            try {
                System.out.println("bef");
                client = (HashMap<String,String>) resultBddclient.toCompletableFuture().get();
                System.out.println("af");

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("nombre de rep: "+client.size());
            if ((client.size())==0){
                System.out.println("Cet id n'existe pas");
            }
            else{
                continuer=false;
            }
        }while(continuer);
        // On verifie le mdp
        do {
            CompletionStage<Object> resutpasswordClient = Patterns.ask(ihm, new IhmActor.GetPassword(), Duration.ofSeconds(10));
            password = "";
            try {
                password = (String) resutpasswordClient.toCompletableFuture().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String result = "";// requete pour vérifier que le mot de passe correspond dans la base
        }while (!(password.equals("password")));// requete pour vérifier qu'il existe dans la base
        // on recupère le montant
        CompletionStage<Object> resultMontant = Patterns.ask(banquier, new IhmActor.GetMontant(), Duration.ofSeconds(10));
        try {
            montant = (int) resultMontant.toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // type de transaction
        CompletionStage<Object> resultTypeTransaction = Patterns.ask(banquier, new IhmActor.GetTypeTransaction(), Duration.ofSeconds(10));
        try {
            typeTransaction = (String) resultTypeTransaction.toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // debut de la transaction
        this.getContext().getSelf().tell(new Transaction(montant,typeTransaction), ActorRef.noSender());
    }
        // avec cet ID on va faire une recherche sur la base de donnée pour voir si elle existe et fournir le client qu'il faut   }

    // non finalement le client sera initialieser à la création de la classe banque via le constructeur
    //la somme à créditer
    public void transaction(int montant,String typeTransaction,ActorRef finTransaction) {
        this.finTransaction = finTransaction;
        CompletionStage<Object> result1 = Patterns.ask(client,new ClientActor.GetInfosClient(),Duration.ofSeconds(10));
        HashMap<String,String> infosClient = null;
        try {
            infosClient = (HashMap<String, String>) result1.toCompletableFuture().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(infosClient);
        CompletionStage<Object> result3 = null;
            switch (typeTransaction){
            case "debiter":
                result3 = Patterns.ask(banquier, new BanquierActor.DebiterCompte(montant, infosClient), Duration.ofSeconds(10));
                Boolean estDebitable = false;
                try {
                    estDebitable = (Boolean) result3.toCompletableFuture().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (estDebitable){
                    client.tell(new ClientActor.CrediterCompteClient(montant), ActorRef.noSender());
                    System.out.println("compte débité avec succès");
                    finTransaction.tell("fini",getSelf());
                }
                else {
                    System.out.println("opération impossible");
                }

            case "crediter":
                result3 = Patterns.ask(banquier,new BanquierActor.CrediterCompte(montant,infosClient),Duration.ofSeconds(10));
                Boolean estCreditable = false;
                try {
                    estCreditable = (Boolean) result3.toCompletableFuture().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (estCreditable){
                    client.tell(new ClientActor.CrediterCompteClient(montant), ActorRef.noSender());
                    System.out.println("compte crédité avec succès");
                    finTransaction.tell("fini",getSelf());
                }
                else {
                    System.out.println("opération impossible");
                }
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
        public Transaction(int montant,String typeTransaction) {
            this.montant = montant;
            this.typeTransaction = typeTransaction;
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
