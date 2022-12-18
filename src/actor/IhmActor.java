package actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.pattern.Patterns;

import java.sql.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;

public class IhmActor extends AbstractActor {


    private IhmActor() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetID.class, message -> sendId(getSender()))
                .match(GetPassword.class, message -> sendPassword(getSender()))
                .match(GetMontant.class, message -> sendMontant(getSender()))
                .match(GetTypeTransaction.class, message -> choixTypeTransaction(getSender()))
                .build();
    }

    private void sendId(ActorRef actor) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Veuillez entrer votre identifiant client: ");
        String id = sc.nextLine();
        actor.tell(id,this.getSelf());
    }
    private void sendPassword(ActorRef actor) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Veuillez entrer votre mot de passe: ");
        String mdp = sc.nextLine();
        actor.tell(mdp,this.getSelf());
    }
    private void sendMontant(ActorRef actor) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Veuillez entrer le montant de la transaction: ");
        String mdp = sc.nextLine();
        actor.tell(mdp,this.getSelf());
    }
    private void choixTypeTransaction(ActorRef actor) {
        int choix = 0;
        Scanner sc = new Scanner(System.in);
        boolean erreur = true;
        do {
            System.out.print("Quel type de transaction souhaitez vous effectuer: \n"+
                    "1 : Debiter votre compte \n"+
                    "2 : Crediter votre compte \n");
            try {
                choix = sc.nextInt();
                sc.nextLine();
                if (choix>=1 && choix <= 2){
                    erreur = false;
                }
                else
                    System.out.println("Oops choix impossible");
            }catch (InputMismatchException e ) {
                sc.nextLine();
                System.out.println("Oops choix impossible");
            }
        }while(erreur);
        String reponse = choix==1?"debiter":"crediter";
        actor.tell(reponse,this.getSelf());
    }

    public static Props props() {
        return Props.create(IhmActor.class);
    }

    // Définition des messages en inner classes
    public interface Message {}

    public static class GetID implements Message {
        public GetID() {
        }

    }
    public static class GetPassword implements Message {
        public GetPassword() {
        }

    }
    public static class GetMontant implements Message {
        public GetMontant() {
        }

    }
    public static class GetTypeTransaction implements Message {
        public GetTypeTransaction() {
        }

    }

}
