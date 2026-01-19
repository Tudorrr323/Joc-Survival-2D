import java.io.Serializable;

public class Grain extends ResourceEntity implements Serializable{
    public Grain(Enums.Quality quality) {
        super(quality, 1); // Base amount 1
    }
    @Override public String getResourceType() { return "FOOD"; }
}