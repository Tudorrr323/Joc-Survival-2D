import java.io.Serializable;

public class Rock extends ResourceEntity implements Serializable{
    public Rock(Enums.Quality quality) {
        super(quality, 2); // Base amount 2
    }
    @Override public String getResourceType() { return "STONE"; }
}