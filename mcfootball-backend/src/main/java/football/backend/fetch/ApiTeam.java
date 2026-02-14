package football.backend.fetch;

/**
 * DTO for a team returned by the external football API.
 */
public class ApiTeam {

    private String name;
    private String city;

    public ApiTeam() {}

    public ApiTeam(String name, String city) {
        this.name = name;
        this.city = city;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
