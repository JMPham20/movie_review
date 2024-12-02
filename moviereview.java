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

    private static final String DB_URL = "jdbc:mysql://localhost/movie_review";
    private static final String USER = "root"; 
    private static final String PASSWORD = "password";
    
    private Connection connection;
     
    public moviereview() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    //Methods used for searching/sorting tables

    //Display all movies with their rating and year
    public void displayMovies() throws SQLException {
        String query = """
                SELECT IMDB_Title.title, IMDB_Title.year, Rating.rating
                FROM IMDB_Title
                LEFT JOIN Rating ON IMDB_Title.title_id = Rating.title_id;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            
            System.out.println("Movies with their release year and rating:");
            while (rs.next()) {
                System.out.println("Title: " + rs.getString("title") +
                                   ", Year: " + rs.getInt("year") +
                                   ", Rating: " + rs.getFloat("rating"));
            }
        }
    }

    //Search for movies or genres and their year/rating.
    public void searchMovies(String keyword) throws SQLException {
        String query = """
                SELECT IMDB_Title.title, IMDB_Title.year, Rating.rating
                FROM IMDB_Title
                LEFT JOIN Rating ON IMDB_Title.title_id = Rating.title_id
                WHERE IMDB_Title.title LIKE ? OR IMDB_Title.genres LIKE ?;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            
            System.out.println("Movies Found:");
            while (rs.next()) {
                System.out.println("Title: " + rs.getString("title") +
                                   ", Year: " + rs.getInt("year") +
                                   ", Rating: " + rs.getFloat("rating"));
            }
        }
    }

    //Sort movies by rating from highest to lowest
    public void sortMoviesByRating() throws SQLException {
        String query = """
                SELECT IMDB_Title.title, Rating.rating, Rating.num_votes
                FROM IMDB_Title
                INNER JOIN Rating ON IMDB_Title.title_id = Rating.title_id
                ORDER BY Rating.rating DESC, Rating.num_votes DESC;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            //return the results
            System.out.println("Movies Sorted by Rating:");
            while (rs.next()) {
                System.out.println("Title: " + rs.getString("title") +
                                   ", Rating: " + rs.getFloat("rating") +
                                   ", Votes: " + rs.getInt("num_votes"));
            }
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
            System.out.println("Actors/Directors Found:");
            while (rs.next()) {
                System.out.println("Name: " + rs.getString("name") +
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
    public void searchTvSeriesDetails(String seriesTitle) throws SQLException {
        String query = """
                SELECT TV_Series.title, TV_Series.start_year, TV_Series.end_year, 
                       COUNT(DISTINCT TV_Episode.season_number) AS total_seasons, 
                       COUNT(TV_Episode.title_id) AS total_episodes
                FROM TV_Series
                LEFT JOIN TV_Episode ON TV_Series.title_id = TV_Episode.tv_series_id
                WHERE TV_Series.title LIKE ?
                GROUP BY TV_Series.title, TV_Series.start_year, TV_Series.end_year;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + seriesTitle + "%");
            ResultSet rs = stmt.executeQuery();
    
            System.out.println("TV Series Details:");
            if (rs.next()) {
                String title = rs.getString("title");
                int startYear = rs.getInt("start_year");
                int endYear = rs.getObject("end_year") != null ? rs.getInt("end_year") : -1; // Check for null
                int totalSeasons = rs.getInt("total_seasons");
                int totalEpisodes = rs.getInt("total_episodes");
    
                System.out.println("Title: " + title);
                System.out.println("Start Year: " + startYear);
                System.out.println("End Year: " + (endYear == -1 ? "Ongoing" : endYear));
                System.out.println("Total Seasons: " + totalSeasons);
                System.out.println("Total Episodes: " + totalEpisodes);
            } else {
                System.out.println("No TV series found matching the title: " + seriesTitle);
            }
        }
    }

    //Search for Shorts
    public void searchShorts(String keyword) throws SQLException {
        String query = """
                SELECT IMDB_Title.title, IMDB_Title.year, Person.name, Collaborated_On.professions
                FROM Short
                INNER JOIN IMDB_Title ON Short.title_id = IMDB_Title.title_id
                LEFT JOIN Collaborated_On ON IMDB_Title.title_id = Collaborated_On.title_id
                LEFT JOIN Person ON Collaborated_On.person_id = Person.person_id
                WHERE IMDB_Title.title LIKE ? OR IMDB_Title.genres LIKE ?
                ORDER BY IMDB_Title.year DESC, IMDB_Title.title ASC;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
    
            System.out.println("Shorts Found:");
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String name = rs.getString("name");
                String professions = rs.getString("professions");
    
                System.out.println("Title: " + title +
                                   ", Year: " + year +
                                   ", Actor/Director: " + (name != null ? name : "N/A") +
                                   ", Profession: " + (professions != null ? professions : "N/A"));
            }
    
            if (!found) {
                System.out.println("No shorts found matching the keyword: " + keyword);
            }
        }
    }

    // Search for feature films
    public void searchFeatureFilms(String keyword) throws SQLException {
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
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String name = rs.getString("name");
                String professions = rs.getString("professions");
    
                System.out.println("Title: " + title +
                                   ", Year: " + year +
                                   ", Actor/Director: " + (name != null ? name : "N/A") +
                                   ", Profession: " + (professions != null ? professions : "N/A"));
            }
    
            if (!found) {
                System.out.println("No feature films found matching the keyword: " + keyword);
            }
        }
    }

    //Search for all of the related data of a Movie
    public void searchTmdbTitle(String keyword) throws SQLException {
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
    
            System.out.println("TMDB Titles Found:");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("TMDB ID: " + rs.getInt("tmdb_id"));
                System.out.println("Title: " + rs.getString("title"));
                System.out.println("Vote Average: " + rs.getFloat("vote_average"));
                System.out.println("Vote Count: " + rs.getInt("vote_count"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Release Date: " + rs.getDate("release_date"));
                System.out.println("Revenue: " + rs.getLong("revenue"));
                System.out.println("Runtime: " + rs.getInt("runtime"));
                System.out.println("Adult: " + rs.getBoolean("adult"));
                System.out.println("Backdrop Path: " + rs.getString("backdrop_path"));
                System.out.println("Budget: " + rs.getLong("budget"));
                System.out.println("Homepage: " + rs.getString("homepage"));
                System.out.println("IMDb ID: " + rs.getString("imdb_id"));
                System.out.println("Original Language: " + rs.getString("original_language"));
                System.out.println("Original Title: " + rs.getString("original_title"));
                System.out.println("Overview: " + rs.getString("overview"));
                System.out.println("Popularity: " + rs.getDouble("popularity"));
                System.out.println("Poster Path: " + rs.getString("poster_path"));
                System.out.println("Tagline: " + rs.getString("tagline"));
                System.out.println("Genres: " + rs.getString("genres"));
                System.out.println("Production Companies: " + rs.getString("production_companies"));
                System.out.println("Production Countries: " + rs.getString("production_countries"));
                System.out.println("Spoken Languages: " + rs.getString("spoken_languages"));
                System.out.println("Keywords: " + rs.getString("keywords"));
                System.out.println("---------------------------------------------------");
            }
    
            if (!found) {
                System.out.println("No TMDB titles found matching the keyword: " + keyword);
            }
        }
    }

    // Sort Movies Alphabetically
    public void sortMoviesAlphabetically() throws SQLException {
        String query = """
                SELECT IMDB_Title.title, IMDB_Title.year, Rating.rating
                FROM IMDB_Title
                LEFT JOIN Rating ON IMDB_Title.title_id = Rating.title_id
                ORDER BY IMDB_Title.title ASC;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
    
            System.out.println("Movies Sorted Alphabetically:");
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String rating = rs.getObject("rating") != null ? String.valueOf(rs.getFloat("rating")) : "N/A";
    
                System.out.println("Title: " + title + ", Year: " + year + ", Rating: " + rating);
            }
    
            if (!found) {
                System.out.println("No movies found in the database.");
            }
        }
    }

    //Sort Movies by year (ASC)
    public void sortMoviesByYearAscending() throws SQLException {
        String query = """
                SELECT IMDB_Title.title, IMDB_Title.year, Rating.rating
                FROM IMDB_Title
                LEFT JOIN Rating ON IMDB_Title.title_id = Rating.title_id
                ORDER BY IMDB_Title.year ASC, IMDB_Title.title ASC;
                """;
    
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
    
            System.out.println("Movies Sorted by Year (Ascending):");
            boolean found = false;
            while (rs.next()) {
                found = true;
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String rating = rs.getObject("rating") != null ? String.valueOf(rs.getFloat("rating")) : "N/A";
    
                System.out.println("Year: " + year + ", Title: " + title + ", Rating: " + rating);
            }
    
            if (!found) {
                System.out.println("No movies found in the database.");
            }
        }
    }
    
    
    
    
    
    
    
    

// Main

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            moviereview app = new moviereview();
            System.out.println("Welcome to Movie Database App");
            System.out.println(" ");

            app.displayMovies();

            while (true) {
                System.out.println("\nChoose an option:");
                System.out.println("1. Search Movies");
                System.out.println("2. Sort Movies by Rating");
                System.out.println("3. Search Actors/Directors by Name");
                System.out.println("4. Search for TV Series");
                System.out.println("5. Search for Shorts");
                System.out.println("6. Search for Feature Films");
                System.out.println("7. Search for a specific Movie's data");
                System.out.println("8. Sort Movies by Alphabetical Order");
                System.out.println("9. Sort Movies by Year (ASC)");
                System.out.println("10. Exit");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1 -> {
                        System.out.print("Enter movie keyword: ");
                        String keyword = scanner.nextLine();
                        app.searchMovies(keyword);
                    }
                    case 2 -> app.sortMoviesByRating();
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
                        app.searchTvSeriesDetails(keyword);
                    }
                    case 5 -> {
                        System.out.println("Search for Shorts:");
                        String keyword = scanner.nextLine();
                        app.searchShorts(keyword);
                    }
                    case 6 -> {
                        System.out.println("Search for Feature Films:");
                        String keyword = scanner.nextLine();
                        app.searchFeatureFilms(keyword);
                    }
                    case 7 -> {
                        System.out.println("Enter Movie Title:");
                        String keyword = scanner.nextLine();
                        app.searchTmdbTitle(keyword);
                    }
                    case 8 -> app.sortMoviesAlphabetically();
                    case 9 -> app.sortMoviesByYearAscending();
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
