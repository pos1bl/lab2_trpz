import java.util.List;

public interface ArchiveRepository {
    void createArchive(Archive archive);
    void editArchive(String archiveId, Archive updatedArchive);
    void deleteArchive(String archiveId);
    Archive checkArchive(String archiveId);
    boolean testArchive(String archiveId);
    List<Archive> splitArchive(String archiveId, int numberOfParts);
}