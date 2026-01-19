import java.io.Serializable;

public class Tree extends ResourceEntity implements Serializable{
    public Tree(Enums.Quality quality) {
        super(quality, 2); // Base amount 2
    }
    @Override public String getResourceType() { return "WOOD"; }
}