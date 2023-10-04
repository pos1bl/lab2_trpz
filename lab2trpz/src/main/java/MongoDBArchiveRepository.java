import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDBArchiveRepository implements ArchiveRepository {
    private final MongoCollection<Document> archiveCollection;

    public MongoDBArchiveRepository() {
        // Initialize MongoDB connection and collection here
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("archives");
        archiveCollection = database.getCollection("archives");
    }

    public void createArchive(Archive archive) {
        // Find the maximum archiveId in the collection
        Document maxIdDoc = archiveCollection.find()
                .sort(Sorts.descending("archiveId"))
                .limit(1)
                .first();

        int newId = 1; // Default new ID if the collection is empty

        if (maxIdDoc != null) {
            String maxId = maxIdDoc.getString("archiveId");
            // Parse the maximum ID and increment it by 1
            try {
                newId = Integer.parseInt(maxId) + 1;
            } catch (NumberFormatException e) {
                // Handle the case where the existing IDs cannot be parsed as integers
                // You can choose to throw an exception or handle this differently based on your requirements
            }
        }

        // Set the new ID in the archive object
        archive.setArchiveId(String.valueOf(newId));

        // Convert Archive object to a MongoDB Document and insert it into the collection
        Document archiveDocument = new Document()
                .append("archiveId", archive.getArchiveId())
                .append("name", archive.getName())
                .append("type", archive.getType())
                .append("filesOrFolders", archive.getFilesOrFolders());
        archiveCollection.insertOne(archiveDocument);
    }

    @Override
    public void editArchive(String archiveId, Archive updatedArchive) {
        Document filter = new Document("archiveId", archiveId);
        Document update = new Document("$set",
                new Document("name", updatedArchive.getName())
                        .append("type", updatedArchive.getType())
                        .append("filesOrFolders", updatedArchive.getFilesOrFolders()));

        archiveCollection.updateOne(filter, update);
    }

    @Override
    public void deleteArchive(String archiveId) {
        Document filter = new Document("archiveId", archiveId);
        archiveCollection.deleteOne(filter);
    }

    @Override
    public Archive checkArchive(String archiveId) {
        Document filter = new Document("archiveId", archiveId);
        Document archiveDocument = archiveCollection.find(filter).first();

        if (archiveDocument != null) {
            Archive archive = new Archive();
            archive.setArchiveId(archiveDocument.getString("archiveId"));
            archive.setName(archiveDocument.getString("name"));
            archive.setType(archiveDocument.getString("type"));
            archive.setFilesOrFolders(archiveDocument.getList("filesOrFolders", String.class));
            return archive;
        } else {
            return null; // Archive not found
        }
    }

    @Override
    public boolean testArchive(String archiveId) {
        Document filter = new Document("archiveId", archiveId);
        Document archiveDocument = archiveCollection.find(filter).first();
        return archiveDocument != null;
    }

    @Override
    public List<Archive> splitArchive(String archiveId, int numberOfParts) {
        // Find the original archive by archiveId
        Document filter = new Document("archiveId", archiveId);
        Document originalArchiveDocument = archiveCollection.find(filter).first();

        if (originalArchiveDocument != null) {
            Archive originalArchive = new Archive();
            originalArchive.setArchiveId(originalArchiveDocument.getString("archiveId"));
            originalArchive.setName(originalArchiveDocument.getString("name"));
            originalArchive.setType(originalArchiveDocument.getString("type"));
            originalArchive.setFilesOrFolders(originalArchiveDocument.getList("filesOrFolders", String.class));

            List<Archive> splitArchives = new ArrayList<>();

            // Calculate the size of each part
            int totalFiles = originalArchive.getFilesOrFolders().size();
            int filesPerPart = totalFiles / numberOfParts;
            int remainingFiles = totalFiles % numberOfParts;

            // Split the files into parts
            int startIndex = 0;
            for (int partIndex = 1; partIndex <= numberOfParts; partIndex++) {
                int endIndex = startIndex + filesPerPart;
                if (partIndex <= remainingFiles) {
                    endIndex++;
                }

                List<String> partFiles = originalArchive.getFilesOrFolders().subList(startIndex, endIndex);

                Document maxIdDoc = archiveCollection.find()
                        .sort(Sorts.descending("archiveId"))
                        .limit(1)
                        .first();

                int newId = 1; // Default new ID if the collection is empty

                if (maxIdDoc != null) {
                    String maxId = maxIdDoc.getString("archiveId");
                    // Parse the maximum ID and increment it by 1
                    try {
                        newId = Integer.parseInt(maxId) + 1;
                    } catch (NumberFormatException e) {
                        // Handle the case where the existing IDs cannot be parsed as integers
                        // You can choose to throw an exception or handle this differently based on your requirements
                    }
                }

                // Create a new Archive for the part
                Archive partArchive = new Archive();
                partArchive.setArchiveId(String.valueOf(newId)); // Generate a new unique ID for the part
                partArchive.setName(originalArchive.getName() + "_part" + partIndex); // Adjust the name
                partArchive.setType(originalArchive.getType());
                partArchive.setFilesOrFolders(partFiles);

                // Insert the part into MongoDB
                Document partDocument = new Document()
                        .append("archiveId", partArchive.getArchiveId())
                        .append("name", partArchive.getName())
                        .append("type", partArchive.getType())
                        .append("filesOrFolders", partArchive.getFilesOrFolders());
                archiveCollection.insertOne(partDocument);

                splitArchives.add(partArchive);

                startIndex = endIndex;
            }

            return splitArchives;
        } else {
            return null; // Archive not found
        }
    }
}
