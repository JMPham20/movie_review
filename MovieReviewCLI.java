import java.util.Scanner;

public class MovieReviewCLI {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            moviereview app = new moviereview();
            System.out.println("Ready"); // Signal readiness to JavaScript
            
            while (true) {
                System.out.println("Enter Command:");
                String command = scanner.nextLine();
                
                if (command.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting...");
                    break;
                }

                switch (command.toLowerCase()) {
                    case "display":
                        app.displayMovies();
                        break;
                    case "search":
                        System.out.println("Enter keyword:");
                        String keyword = scanner.nextLine();
                        app.searchMovies(keyword);
                        break;
                    case "sort":
                        app.sortMoviesByRating();
                        break;
                    default:
                        System.out.println("Unknown Command");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
