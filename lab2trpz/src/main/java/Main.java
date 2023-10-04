import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        ArchiveRepository archiveRepository = new MongoDBArchiveRepository();

        // Create an Archive object
        Archive newArchive = new Archive();
        newArchive.setName("I LOVE JAVA");
        newArchive.setType("zip");
        newArchive.setFilesOrFolders(Arrays.asList("vlad.txt", "pos1bl.txt"));

        // Create the archive in MongoDB
//        archiveRepository.createArchive(newArchive);
//        archiveRepository.deleteArchive("123456");
//          archiveRepository.editArchive("2", newArchive);
//        archiveRepository.splitArchive("2", 2);
    }
}