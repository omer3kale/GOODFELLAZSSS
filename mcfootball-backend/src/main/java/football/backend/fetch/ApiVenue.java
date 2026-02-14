package football.backend.fetch;

/**
 * DTO for a venue/stadium returned by the external football API.
 */
public class ApiVenue {

    private String name;

    public ApiVenue() {}

    public ApiVenue(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
