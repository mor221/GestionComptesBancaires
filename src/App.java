import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import actor.BanqueActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;

public class App {

    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create();
        // Jeu de ping pong (communication entre acteurs, et attente d'une réponse)
        ActorRef banqueActor = actorSystem.actorOf(BanqueActor.props(), "banque");
        CompletionStage<Object> result = Patterns.ask(banqueActor, new BanqueActor.InitBanque(), Duration.ofSeconds(1000));
        try {
            result.toCompletableFuture().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("bieng");
        // Arrêt du système d'acteurs
        actorSystem.terminate();
    }
}
