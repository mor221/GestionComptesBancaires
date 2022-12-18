package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.sql.*;
import java.util.HashMap;

public class DonneesActor extends AbstractActor {
    private DonneesActor() {
        // reuête sql recuperant un client au hasard dans la base

    }


    // Méthode servant à déterminer le comportement de l'acteur lorsqu'il reçoit un message
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetClient.class, message -> sendClient(message,getSender()))
                .match(UpdateClient.class, message -> updateSolde(message,getSender()))
                .build();
    }

    private void sendClient(final GetClient message,ActorRef actor) {
        int i = 0;
        HashMap<String,String> row = new HashMap<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/banque","root","root");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select  * from client where id='"+message.id+"'");
            while (resultSet.next()){
                row.put("id",resultSet.getString("id"));
                row.put("nom",resultSet.getString("nom"));
                row.put("password",resultSet.getString("password"));
                row.put("solde",resultSet.getString("solde"));
                row.put("montantDecouvertAutorise",resultSet.getString("montantDecouvertAutorise"));
                row.put("montantPlafond",resultSet.getString("montantPlafond"));
                row.put("idBanquier",resultSet.getString("idBanquier"));
                i++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        actor.tell(row,getSelf());
    }
    private void updateSolde(final UpdateClient message,ActorRef actor) {
        HashMap<String,String> row = new HashMap<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/banque","root","root");
            PreparedStatement statement = connection.prepareStatement("UPDATE client SET solde = "+message.solde+" where id='"+message.id+"'");
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // Méthode servant à la création d'un acteur
    public static Props props() {
        return Props.create(DonneesActor.class);
    }
    public static class GetClient implements Message {
        String id;
        public GetClient(String id) {
            this.id = id;
        }

    }
    public static class UpdateClient implements Message {
        String id;
        int solde;
        public UpdateClient(String id,int solde) {
            this.id = id;
            this.solde = solde;
        }

    }
    public static class GetBanquier implements Message {
        String id;
        public GetBanquier(String id) {
            this.id = id;
        }

    }

    // Définition des messages en inner classes
    public interface Message {}
}
