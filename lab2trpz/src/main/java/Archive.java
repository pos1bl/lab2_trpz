import java.util.List;

public class Archive {
    private String archiveId;
    private String name;
    private String type;
    private List<String> filesOrFolders;

    public Archive() {}

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getFilesOrFolders() {
        return filesOrFolders;
    }

    public void setFilesOrFolders(List<String> filesOrFolders) {
        this.filesOrFolders = filesOrFolders;
    }
}
