import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class moviereview {
    // Database connection

    private static final String DB_URL = "jdbc:mysql://localhost:3306/movie_review?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; 
    private static final String PASSWORD = "As25021!";
    
    private Connection connection;
     
    public moviereview() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    //Methods used for searching/sorting tables

    //Display all movies with their rating and year
    public String displayMovies() throws SQLException {
        String query = "SELECT IMDB_Title.title, IMDB_Title.year, Rating.rating FROM IMDB_Title LEFT JOIN Rating ON IMDB_Title.title_id = Rating.title_id;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            sb.append("Movies with their release year and rating:");
            while (rs.next()) {
                sb.append("Title: " + rs.getString("title") +
                                   ", Year: " + rs.getInt("year") +
                                   ", Rating: " + rs.getFloat("rating"));
            }
            return sb.toString();
        }
    }

    //Search for movies or genres and their year/rating.
    public String searchMovies(String keyword) throws SQLException {
        String query = "SELECT IMDB_Title.title, IMDB_Title.year, Rating.rating FROM IMDB_Title LEFT JOIN Rating ON IMDB_Title.title_id = Rating.title_id WHERE IMDB_Title.title LIKE ? OR IMDB_Title.genres LIKE ?;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            sb.append("Movies Found:");
            while (rs.next()) {
                sb.append("Title: " + rs.getString("title") +
                                   ", Year: " + rs.getInt("year") +
                                   ", Rating: " + rs.getFloat("rating") +"\n");
            }
            return sb.toString();
        }
    }

    //Sort movies by rating from highest to lowest
    public String sortMoviesByRating() throws SQLException {
        String query = "SELECT IMDB_Title.title, Rating.rating, Rating.num_votes FROM IMDB_Title INNER JOIN Rating ON IMDB_Title.title_id = Rating.title_id ORDER BY Rating.rating DESC, Rating.num_votes DESC;";
        try (PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            StringBuilder sb = new StringBuilder();
            //return the results
            sb.append("Movies Sorted by Rating:");
            while (rs.next()) {
                sb.append("Title: " + rs.getString("title") +
                                   ", Rating: " + rs.getFloat("rating") +
                                   ", Votes: " + rs.getInt("num_votes\n"));
            }
            return sb.toString();
        }
    }

    /* WIP

    public void searchActorsAndDirectors(String name) throws SQLException {
        String query = """
                SELECT Person.name, Collaborated_On.professions, IMDB_Title.title
                FROM Person
                INNER JOIN Collaborated_On ON Person.person_id = Collaborated_On.person_id
                INNER JOIN IMDB_Title ON Collaborated_On.title_id = IMDB_Title.title_id
                WHERE Person.name LIKE ?;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
            
            //return the results
            sb.append("Actors/Directors Found:");
            while (rs.next()) {
                sb.append("Name: " + rs.getString("name") +
                                   ", Profession: " + rs.getString("professions") +
                                   ", Movie: " + rs.getString("title"));
            }
        }
    }

    */

    //Search for the movies/titles from a specific actor
    public List<String> getKnownForTitles(String name) throws SQLException {
        List<String> movies = new ArrayList<>();
        String query = "SELECT known_for_titles FROM Person WHERE name LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                String knownForTitles = rs.getString("known_for_titles");
                if (knownForTitles != null && !knownForTitles.isEmpty()) {
                    String[] titleIds = knownForTitles.split(",");
                    for (String titleId : titleIds) {
                        String movieQuery = "SELECT title, year, genres FROM IMDB_Title WHERE title_id = ?";
                        try (PreparedStatement movieStmt = connection.prepareStatement(movieQuery)) {
                            movieStmt.setString(1, titleId.trim());
                            ResultSet movieRs = movieStmt.executeQuery();
                            if (movieRs.next()) {
                                String movieTitle = movieRs.getString("title");
                                int year = movieRs.getInt("year");
                                String genres = movieRs.getString("genres");
                                movies.add(String.format("Title: %s, Year: %d, Genres: %s", movieTitle, year, genres));
                            }
                        }
                    }
                }
            }
        }
        return movies;
    }

    //Search for TV Series and display their data such as season count and episode count
    public String searchTvSeriesDetails(String seriesTitle) throws SQLException {
        String query = "SELECT TV_Series.title, TV_Series.start_year, TV_Series.end_year, COUNT(DISTINCT TV_Episode.season_number) AS total_seasons, COUNT(TV_Episode.title_id) AS total_episodes FROM TV_Series LEFT JOIN TV_Episode ON TV_Series.title_id = TV_Episode.tv_series_id WHERE TV_Series.title LIKE ? GROUP BY TV_Series.title, TV_Series.start_year, TV_Series.end_year;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + seriesTitle + "%");
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            System.out.println("TV Series Details:");
            if (rs.next()) {
                String title = rs.getString("title");
                int startYear = rs.getInt("start_year");
                int endYear = rs.getObject("end_year") != null ? rs.getInt("end_year") : -1; // Check for null
                int totalSeasons = rs.getInt("total_seasons");
                int totalEpisodes = rs.getInt("total_episodes");
    
                sb.append("Title: " + title);
                sb.append("\nStart Year: " + startYear);
                sb.append("\nEnd Year: " + (endYear == -1 ? "Ongoing" : endYear));
                sb.append("\nTotal Seasons: " + totalSeasons);
                sb.append("\nTotal Episodes: " + totalEpisodes);
                return sb.toString();
            } else {
                return ("No TV series found matching the title: " + seriesTitle);
            }
        }
    }

    //Search for Shorts
    public String searchShorts(String keyword) throws SQLException {
        String query = "SELECT IMDB_Title.title, IMDB_Title.year, Person.name, Collaborated_On.professions FROM Short INNER JOIN IMDB_Title ON Short.title_id = IMDB_Title.title_id LEFT JOIN Collaborated_On ON IMDB_Title.title_id = Collaborated_On.title_id LEFT JOIN Person ON Collaborated_On.person_id = Person.person_id WHERE IMDB_Title.title LIKE ? OR IMDB_Title.genres LIKE ? ORDER BY IMDB_Title.year DESC, IMDB_Title.title ASC;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
    
            System.out.println("Shorts Found:");
            boolean found = false;
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                found = true;
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String name = rs.getString("name");
                String professions = rs.getString("professions");
    
                sb.append("Title: " + title +
                                   ", Year: " + year +
                                   ", Actor/Director: " + (name != null ? name : "N/A") +
                                   ", Profession: " + (professions != null ? professions : "N/A"));
            }
    
            if (!found) {
                return ("No shorts found matching the keyword: " + keyword);
            }
            return sb.toString();
        }
    }

    // Search for feature films
    public String searchFeatureFilms(String keyword) throws SQLException {
        String query = """
                SELECT IMDB_Title.title, IMDB_Title.year, Person.name, Collaborated_On.professions
                FROM Feature_Film
                INNER JOIN IMDB_Title ON Feature_Film.title_id = IMDB_Title.title_id
                LEFT JOIN Collaborated_On ON IMDB_Title.title_id = Collaborated_On.title_id
                LEFT JOIN Person ON Collaborated_On.person_id = Person.person_id
                WHERE IMDB_Title.title LIKE ? OR IMDB_Title.genres LIKE ?
                ORDER BY IMDB_Title.year DESC, IMDB_Title.title ASC;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
    
            System.out.println("Feature Films Found:");
            StringBuilder sb = new StringBuilder();
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String name = rs.getString("name");
                String professions = rs.getString("professions");
    
                sb.append("Title: " + title +
                                   ", Year: " + year +
                                   ", Actor/Director: " + (name != null ? name : "N/A") +
                                   ", Profession: " + (professions != null ? professions : "N/A"));
            }
    
            if (!found) {
                sb.append("No feature films found matching the keyword: " + keyword);
            }
            return sb.toString();
        }
    }

    //Search for all of the related data of a Movie
    public String searchTmdbTitle(String keyword) throws SQLException {
        String query = """
                SELECT tmdb_id, title, vote_average, vote_count, status, release_date, revenue, runtime, adult,
                       backdrop_path, budget, homepage, imdb_id, original_language, original_title, overview,
                       popularity, poster_path, tagline, genres, production_companies, production_countries,
                       spoken_languages, keywords
                FROM TMDB_Title
                WHERE title LIKE ? OR genres LIKE ? OR tagline LIKE ?;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            stmt.setString(3, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            System.out.println("TMDB Titles Found:");
            boolean found = false;
            while (rs.next()) {
                found = true;
                sb.append("TMDB ID: " + rs.getInt("tmdb_id"));
                sb.append("Title: " + rs.getString("title"));
                sb.append("Vote Average: " + rs.getFloat("vote_average"));
                sb.append("Vote Count: " + rs.getInt("vote_count"));
                sb.append("Status: " + rs.getString("status"));
                sb.append("Release Date: " + rs.getDate("release_date"));
                sb.append("Revenue: " + rs.getLong("revenue"));
                sb.append("Runtime: " + rs.getInt("runtime"));
                sb.append("Adult: " + rs.getBoolean("adult"));
                sb.append("Backdrop Path: " + rs.getString("backdrop_path"));
                sb.append("Budget: " + rs.getLong("budget"));
                sb.append("Homepage: " + rs.getString("homepage"));
                sb.append("IMDb ID: " + rs.getString("imdb_id"));
                sb.append("Original Language: " + rs.getString("original_language"));
                sb.append("Original Title: " + rs.getString("original_title"));
                sb.append("Overview: " + rs.getString("overview"));
                sb.append("Popularity: " + rs.getDouble("popularity"));
                sb.append("Poster Path: " + rs.getString("poster_path"));
                sb.append("Tagline: " + rs.getString("tagline"));
                sb.append("Genres: " + rs.getString("genres"));
                sb.append("Production Companies: " + rs.getString("production_companies"));
                sb.append("Production Countries: " + rs.getString("production_countries"));
                sb.append("Spoken Languages: " + rs.getString("spoken_languages"));
                sb.append("Keywords: " + rs.getString("keywords\n"));
                sb.append("---------------------------------------------------");
            }
    
            if (!found) {
                sb.append("No TMDB titles found matching the keyword: " + keyword);
            }
            return sb.toString();
        }
    }

    // Sort Movies Alphabetically
    public String sortMoviesAlphabetically() throws SQLException {
        String query = """
                SELECT IMDB_Title.title, IMDB_Title.year, Rating.rating
                FROM IMDB_Title
                LEFT JOIN Rating ON IMDB_Title.title_id = Rating.title_id
                ORDER BY IMDB_Title.title ASC;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            System.out.println("Movies Sorted Alphabetically:");
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String rating = rs.getObject("rating") != null ? String.valueOf(rs.getFloat("rating")) : "N/A";
    
                sb.append("Title: " + title + ", Year: " + year + ", Rating: " + rating + "\n");
            }
    
            if (!found) {
                sb.append("No movies found in the database.");
            }
            return sb.toString();
        }
    }

    //Sort Movies by year (ASC)
    public String sortMoviesByYearAscending() throws SQLException {
        String query = "SELECT IMDB_Title.title, IMDB_Title.year, Rating.rating FROM IMDB_Title LEFT JOIN Rating ON IMDB_Title.title_id = Rating.title_id ORDER BY IMDB_Title.year ASC, IMDB_Title.title ASC;";
    
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            System.out.println("Movies Sorted by Year (Ascending):");
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String rating = rs.getObject("rating") != null ? String.valueOf(rs.getFloat("rating")) : "N/A";
    
                sb.append("Year: " + year + ", Title: " + title + ", Rating: " + rating + "\n");
            }
    
            if (!found) {
                sb.append("No movies found in the database.");
            }
            return sb.toString();
        }
    }
    
    
    
    
    
    
    
    

// Main

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.class.path"));
        try (Scanner scanner = new Scanner(System.in)) {
            moviereview app = new moviereview();
            System.out.println("Welcome to Movie Database App");
            System.out.println(" ");

            app.displayMovies();

            while (true) {
                System.out.println("\nChoose an option:\n");
                System.out.println("1. Search Movies\n");
                System.out.println("2. Sort Movies by Rating\n");
                System.out.println("3. Search Actors/Directors by Name\n");
                System.out.println("4. Search for TV Series\n");
                System.out.println("5. Search for Shorts\n");
                System.out.println("6. Search for Feature Films\n");
                System.out.println("7. Search for a specific Movie's data\n");
                System.out.println("8. Sort Movies by Alphabetical Order\n");
                System.out.println("9. Sort Movies by Year (ASC)\n");
                System.out.println("10. Exit\n");
             
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1 -> {
                        System.out.print("Enter movie keyword: ");
                        String keyword = scanner.nextLine();
                        System.out.println(app.searchMovies(keyword));
                    }
                    case 2 -> System.out.println(app.sortMoviesByRating());
                    case 3 -> {
                        System.out.print("Enter actor/director name: ");
                        String name = scanner.nextLine();
                        // WIP: app.searchActorsAndDirectors(name);
                        List<String> movies = app.getKnownForTitles(name);
                        System.out.println("Known For Titles:");
                        for (String movie : movies) {
                            System.out.println(movie);
                        }
                    }
                    case 4 -> {
                        System.out.println("Search for TV Series");
                        String keyword = scanner.nextLine();
                        System.out.println(app.searchTvSeriesDetails(keyword));
                    }
                    case 5 -> {
                        System.out.println("Search for Shorts:");
                        String keyword = scanner.nextLine();
                        System.out.println(app.searchShorts(keyword));
                    }
                    case 6 -> {
                        System.out.println("Search for Feature Films:");
                        String keyword = scanner.nextLine();
                        System.out.println(app.searchFeatureFilms(keyword));
                    }
                    case 7 -> {
                        System.out.println("Enter Movie Title:");
                        String keyword = scanner.nextLine();
                        System.out.println(app.searchTmdbTitle(keyword));
                    }
                    case 8 -> System.out.println(app.sortMoviesAlphabetically());
                    case 9 -> System.out.println(app.sortMoviesByYearAscending());
                    case 10 -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice, please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
